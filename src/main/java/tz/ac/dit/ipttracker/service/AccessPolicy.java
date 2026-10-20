package tz.ac.dit.ipttracker.service;

import tz.ac.dit.ipttracker.entity.Placement;
import tz.ac.dit.ipttracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipttracker.repository.PlacementRepository;
import tz.ac.dit.ipttracker.security.AppUserDetails;
import tz.ac.dit.ipttracker.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Ownership rules: role says who you are, ownership says what is yours (business rule 6). */
@Component
@RequiredArgsConstructor
public class AccessPolicy {

    private final PlacementRepository placements;

    public Placement loadPlacement(Long id) {
        return placements.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement " + id + " not found"));
    }

    /** Loads the placement and returns it only if the logged-in user may see it; otherwise 403. */
    public Placement requireViewable(Long placementId) {
        Placement p = loadPlacement(placementId);
        AppUserDetails me = CurrentUser.get();
        boolean allowed = switch (me.role()) {
            case ADMIN -> true;
            case STUDENT -> p.getStudent().getId().equals(me.id());
            case INDUSTRIAL_SUPERVISOR -> p.getIndustrialSupervisor().getId().equals(me.id());
            case ACADEMIC_SUPERVISOR -> p.getAcademicSupervisor().getId().equals(me.id());
        };
        if (!allowed) {
            throw new AccessDeniedException("Placement " + placementId + " does not belong to you");
        }
        return p;
    }
}
