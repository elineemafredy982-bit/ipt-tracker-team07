package tz.ac.dit.ipttracker.service;

import tz.ac.dit.ipttracker.dto.CreatePlacementRequest;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.dto.PlacementResponse;
import tz.ac.dit.ipttracker.dto.PlacementSummary;
import tz.ac.dit.ipttracker.entity.EntryStatus;
import tz.ac.dit.ipttracker.entity.Organisation;
import tz.ac.dit.ipttracker.entity.Placement;
import tz.ac.dit.ipttracker.entity.Role;
import tz.ac.dit.ipttracker.entity.User;
import tz.ac.dit.ipttracker.exception.BusinessRuleException;
import tz.ac.dit.ipttracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipttracker.mapper.PlacementMapper;
import tz.ac.dit.ipttracker.repository.LogbookEntryRepository;
import tz.ac.dit.ipttracker.repository.LogbookEntryRepository.StatusStat;
import tz.ac.dit.ipttracker.repository.OrganisationRepository;
import tz.ac.dit.ipttracker.repository.PlacementRepository;
import tz.ac.dit.ipttracker.repository.UserRepository;
import tz.ac.dit.ipttracker.security.AppUserDetails;
import tz.ac.dit.ipttracker.security.CurrentUser;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlacementService {

    private final PlacementRepository placements;
    private final UserRepository users;
    private final OrganisationRepository organisations;
    private final LogbookEntryRepository entries;
    private final AccessPolicy access;
    private final Clock clock;

    public PlacementResponse create(CreatePlacementRequest r) {
        if (!r.startDate().isBefore(r.endDate())) {
            throw new BusinessRuleException("Start date must be before end date");
        }
        User student = userWithRole(r.studentId(), Role.STUDENT);
        User industrial = userWithRole(r.industrialSupervisorId(), Role.INDUSTRIAL_SUPERVISOR);
        User academic = userWithRole(r.academicSupervisorId(), Role.ACADEMIC_SUPERVISOR);
        Organisation org = organisations.findById(r.organisationId()).orElseThrow(
                () -> new ResourceNotFoundException("Organisation " + r.organisationId() + " not found"));
        if (placements.existsByStudentIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                student.getId(), r.endDate(), r.startDate())) {
            throw new BusinessRuleException("Student already has a placement overlapping "
                    + r.startDate() + " to " + r.endDate() + " (one placement per student per IPT period)");
        }
        Placement p = new Placement();
        p.setStudent(student);
        p.setOrganisation(org);
        p.setIndustrialSupervisor(industrial);
        p.setAcademicSupervisor(academic);
        p.setStartDate(r.startDate());
        p.setEndDate(r.endDate());
        p.setDepartment(r.department().trim());
        return PlacementMapper.toResponse(placements.save(p));
    }

    @Transactional(readOnly = true)
    public PlacementResponse get(Long id) {
        return PlacementMapper.toResponse(access.requireViewable(id));
    }

    /** Each role only lists the placements that are theirs (business rule 6). */
    @Transactional(readOnly = true)
    public PageResponse<PlacementResponse> list(Pageable pageable) {
        AppUserDetails me = CurrentUser.get();
        Page<Placement> page = switch (me.role()) {
            case ADMIN -> placements.findAll(pageable);
            case STUDENT -> placements.findByStudentId(me.id(), pageable);
            case INDUSTRIAL_SUPERVISOR -> placements.findByIndustrialSupervisorId(me.id(), pageable);
            case ACADEMIC_SUPERVISOR -> placements.findByAcademicSupervisorId(me.id(), pageable);
        };
        return PageResponse.from(page.map(PlacementMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PlacementSummary summary(Long id) {
        Placement p = access.requireViewable(id);
        List<StatusStat> stats = entries.statusStats(id);   // GROUP BY done by the database
        long total = 0, approved = 0, pending = 0, rejected = 0, approvedHours = 0;
        for (StatusStat s : stats) {
            total += s.getTotal();
            if (s.getStatus() == EntryStatus.APPROVED) {
                approved = s.getTotal();
                approvedHours = s.getHours();
            } else if (s.getStatus() == EntryStatus.SUBMITTED) {
                pending = s.getTotal();
            } else if (s.getStatus() == EntryStatus.REJECTED) {
                rejected = s.getTotal();
            }
        }
        return new PlacementSummary(p.getId(), p.getStudent().getFullName(), p.getOrganisation().getName(),
                total, approved, pending, rejected, approvedHours, daysWithoutEntry(p));
    }

    /** Weekdays (Mon-Fri) from the placement start up to today (or the end date) that have no entry. */
    private long daysWithoutEntry(Placement p) {
        LocalDate today = LocalDate.now(clock);
        LocalDate last = today.isBefore(p.getEndDate()) ? today : p.getEndDate();
        if (last.isBefore(p.getStartDate())) {
            return 0;
        }
        Set<LocalDate> logged = new HashSet<>(entries.findEntryDates(p.getId()));
        return p.getStartDate().datesUntil(last.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .filter(d -> !logged.contains(d))
                .count();
    }

    private User userWithRole(Long id, Role role) {
        User u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
        if (u.getRole() != role) {
            throw new BusinessRuleException("User " + id + " is not a " + role);
        }
        return u;
    }
}
