package tz.ac.dit.ipttracker.mapper;

import tz.ac.dit.ipttracker.dto.PlacementResponse;
import tz.ac.dit.ipttracker.entity.Placement;

public final class PlacementMapper {
    private PlacementMapper() {
    }

    /** Call inside a transaction: reads lazy associations. */
    public static PlacementResponse toResponse(Placement p) {
        return new PlacementResponse(p.getId(), p.getStudent().getId(), p.getStudent().getFullName(),
                p.getOrganisation().getId(), p.getOrganisation().getName(),
                p.getIndustrialSupervisor().getId(), p.getIndustrialSupervisor().getFullName(),
                p.getAcademicSupervisor().getId(), p.getAcademicSupervisor().getFullName(),
                p.getStartDate(), p.getEndDate(), p.getDepartment(), p.getStatus());
    }
}
