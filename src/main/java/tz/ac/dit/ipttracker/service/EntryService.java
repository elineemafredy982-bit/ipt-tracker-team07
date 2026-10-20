package tz.ac.dit.ipttracker.service;

import tz.ac.dit.ipttracker.dto.CreateEntryRequest;
import tz.ac.dit.ipttracker.dto.EntryResponse;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.dto.ReviewRequest;
import tz.ac.dit.ipttracker.dto.UpdateEntryRequest;
import tz.ac.dit.ipttracker.entity.EntryStatus;
import tz.ac.dit.ipttracker.entity.LogbookEntry;
import tz.ac.dit.ipttracker.entity.Placement;
import tz.ac.dit.ipttracker.entity.PlacementStatus;
import tz.ac.dit.ipttracker.entity.Role;
import tz.ac.dit.ipttracker.exception.BusinessRuleException;
import tz.ac.dit.ipttracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipttracker.mapper.EntryMapper;
import tz.ac.dit.ipttracker.repository.LogbookEntryRepository;
import tz.ac.dit.ipttracker.repository.UserRepository;
import tz.ac.dit.ipttracker.security.AppUserDetails;
import tz.ac.dit.ipttracker.security.CurrentUser;
import jakarta.persistence.criteria.Predicate;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EntryService {

    private final LogbookEntryRepository entries;
    private final UserRepository users;
    private final AccessPolicy access;
    private final Clock clock;

    // ------------------------------------------------------------------ student actions

    public EntryResponse create(Long placementId, CreateEntryRequest r) {
        AppUserDetails me = CurrentUser.get();
        Placement p = access.loadPlacement(placementId);
        requireOwningStudent(p, me);
        validateNewEntry(p, r.entryDate(), r.hoursWorked());

        LogbookEntry e = new LogbookEntry();
        e.setPlacement(p);
        e.setEntryDate(r.entryDate());
        e.setTasksDone(r.tasksDone().trim());
        e.setSkillsLearned(r.skillsLearned());
        e.setHoursWorked(r.hoursWorked());
        e.setStatus(EntryStatus.DRAFT);
        return EntryMapper.toResponse(entries.save(e));
    }

    public EntryResponse update(Long id, UpdateEntryRequest r) {
        LogbookEntry e = find(id);
        requireOwningStudent(e.getPlacement(), CurrentUser.get());
        e.assertEditable();                                    // rule 4: only DRAFT / REJECTED
        checkHours(r.hoursWorked());
        e.setTasksDone(r.tasksDone().trim());
        e.setSkillsLearned(r.skillsLearned());
        e.setHoursWorked(r.hoursWorked());
        return EntryMapper.toResponse(e);
    }

    public void delete(Long id) {
        LogbookEntry e = find(id);
        requireOwningStudent(e.getPlacement(), CurrentUser.get());
        if (e.getStatus() != EntryStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT entries can be deleted (current status: "
                    + e.getStatus() + ")");
        }
        entries.delete(e);
    }

    public EntryResponse submit(Long id) {
        LogbookEntry e = find(id);
        requireOwningStudent(e.getPlacement(), CurrentUser.get());
        e.submit();
        return EntryMapper.toResponse(e);
    }

    // ------------------------------------------------------------------ supervisor actions

    public EntryResponse approve(Long id, ReviewRequest r) {
        LogbookEntry e = find(id);
        requireAssignedSupervisor(e.getPlacement(), CurrentUser.get());
        e.approve(users.getReferenceById(CurrentUser.get().id()), r == null ? null : r.comment());
        return EntryMapper.toResponse(e);
    }

    public EntryResponse reject(Long id, ReviewRequest r) {
        LogbookEntry e = find(id);
        requireAssignedSupervisor(e.getPlacement(), CurrentUser.get());
        e.reject(users.getReferenceById(CurrentUser.get().id()), r == null ? null : r.comment());
        return EntryMapper.toResponse(e);
    }

    @Transactional(readOnly = true)
    public PageResponse<EntryResponse> pending(Pageable pageable) {
        AppUserDetails me = CurrentUser.get();
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());  // query sorts oldest first
        return PageResponse.from(entries.findPending(EntryStatus.SUBMITTED, me.id(), unsorted)
                .map(EntryMapper::toResponse));
    }

    // ------------------------------------------------------------------ queries

    @Transactional(readOnly = true)
    public EntryResponse get(Long id) {
        LogbookEntry e = find(id);
        access.requireViewable(e.getPlacement().getId());
        return EntryMapper.toResponse(e);
    }

    @Transactional(readOnly = true)
    public PageResponse<EntryResponse> list(Long placementId, EntryStatus status, LocalDate from, LocalDate to,
                                            Pageable pageable) {
        access.requireViewable(placementId);          // 404 if missing, 403 if not yours
        AppUserDetails me = CurrentUser.get();
        Specification<LogbookEntry> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("placement").get("id"), placementId));
            if (status != null) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("entryDate"), from));
            }
            if (to != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("entryDate"), to));
            }
            if (me.role() == Role.INDUSTRIAL_SUPERVISOR) {   // supervisors do not see unsubmitted drafts
                ps.add(cb.notEqual(root.get("status"), EntryStatus.DRAFT));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return PageResponse.from(entries.findAll(spec, pageable).map(EntryMapper::toResponse));
    }

    // ------------------------------------------------------------------ rules & helpers

    private void validateNewEntry(Placement p, LocalDate date, Integer hours) {
        if (p.getStatus() != PlacementStatus.ACTIVE) {
            throw new BusinessRuleException("Entries can only be added to an ACTIVE placement");
        }
        checkHours(hours);
        if (date.isAfter(LocalDate.now(clock))) {
            throw new BusinessRuleException("An entry cannot be dated in the future");
        }
        if (date.isBefore(p.getStartDate()) || date.isAfter(p.getEndDate())) {
            throw new BusinessRuleException("Entry date " + date + " is outside the placement period "
                    + p.getStartDate() + " to " + p.getEndDate());
        }
        if (entries.existsByPlacementIdAndEntryDate(p.getId(), date)) {
            throw new BusinessRuleException("An entry already exists for " + date);
        }
    }

    private void checkHours(Integer hours) {
        if (hours == null || hours < 1 || hours > 12) {
            throw new BusinessRuleException("hoursWorked must be between 1 and 12");
        }
    }

    private void requireOwningStudent(Placement p, AppUserDetails me) {
        if (me.role() != Role.STUDENT || !p.getStudent().getId().equals(me.id())) {
            throw new AccessDeniedException("Only the student who owns this placement can do this");
        }
    }

    private void requireAssignedSupervisor(Placement p, AppUserDetails me) {
        if (me.role() != Role.INDUSTRIAL_SUPERVISOR || !p.getIndustrialSupervisor().getId().equals(me.id())) {
            throw new AccessDeniedException("Only the industrial supervisor assigned to this placement can do this");
        }
    }

    private LogbookEntry find(Long id) {
        return entries.findById(id).orElseThrow(() -> new ResourceNotFoundException("Entry " + id + " not found"));
    }
}
