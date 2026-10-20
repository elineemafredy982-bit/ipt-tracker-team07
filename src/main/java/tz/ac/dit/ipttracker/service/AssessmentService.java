package tz.ac.dit.ipttracker.service;

import tz.ac.dit.ipttracker.dto.AssessmentRequest;
import tz.ac.dit.ipttracker.dto.AssessmentResponse;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.entity.Assessment;
import tz.ac.dit.ipttracker.entity.Placement;
import tz.ac.dit.ipttracker.entity.Role;
import tz.ac.dit.ipttracker.mapper.AssessmentMapper;
import tz.ac.dit.ipttracker.repository.AssessmentRepository;
import tz.ac.dit.ipttracker.repository.UserRepository;
import tz.ac.dit.ipttracker.security.AppUserDetails;
import tz.ac.dit.ipttracker.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessments;
    private final UserRepository users;
    private final AccessPolicy access;

    public AssessmentResponse create(Long placementId, AssessmentRequest r) {
        AppUserDetails me = CurrentUser.get();
        Placement p = access.loadPlacement(placementId);
        if (me.role() != Role.ACADEMIC_SUPERVISOR || !p.getAcademicSupervisor().getId().equals(me.id())) {
            throw new AccessDeniedException("Only the academic supervisor assigned to this placement can assess it");
        }
        Assessment a = new Assessment();
        a.setPlacement(p);
        a.setAssessor(users.getReferenceById(me.id()));
        a.setVisitDate(r.visitDate());
        a.setScore(r.score());
        a.setRemarks(r.remarks());
        return AssessmentMapper.toResponse(assessments.save(a));
    }

    @Transactional(readOnly = true)
    public PageResponse<AssessmentResponse> list(Long placementId, Pageable pageable) {
        access.requireViewable(placementId);
        return PageResponse.from(assessments.findByPlacementId(placementId, pageable)
                .map(AssessmentMapper::toResponse));
    }
}
