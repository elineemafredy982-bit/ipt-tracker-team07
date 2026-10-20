package tz.ac.dit.ipttracker.mapper;

import tz.ac.dit.ipttracker.dto.OrganisationRequest;
import tz.ac.dit.ipttracker.dto.OrganisationResponse;
import tz.ac.dit.ipttracker.entity.Organisation;

public final class OrganisationMapper {
    private OrganisationMapper() {
    }

    public static Organisation toEntity(OrganisationRequest r) {
        Organisation o = new Organisation();
        apply(o, r);
        return o;
    }

    public static void apply(Organisation o, OrganisationRequest r) {
        o.setName(r.name().trim());
        o.setSector(r.sector().trim());
        o.setRegion(r.region().trim());
        o.setAddress(r.address());
        o.setContactEmail(r.contactEmail());
    }

    public static OrganisationResponse toResponse(Organisation o) {
        return new OrganisationResponse(o.getId(), o.getName(), o.getSector(), o.getRegion(), o.getAddress(),
                o.getContactEmail());
    }
}
