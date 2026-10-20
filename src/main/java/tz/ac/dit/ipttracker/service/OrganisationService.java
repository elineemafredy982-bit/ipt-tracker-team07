package tz.ac.dit.ipttracker.service;

import tz.ac.dit.ipttracker.dto.OrganisationRequest;
import tz.ac.dit.ipttracker.dto.OrganisationResponse;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.entity.Organisation;
import tz.ac.dit.ipttracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipttracker.mapper.OrganisationMapper;
import tz.ac.dit.ipttracker.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganisationService {

    private final OrganisationRepository repository;

    public OrganisationResponse create(OrganisationRequest request) {
        return OrganisationMapper.toResponse(repository.save(OrganisationMapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrganisationResponse> list(String region, String sector, Pageable pageable) {
        Specification<Organisation> spec = Specification.where(null);
        if (region != null && !region.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("region")), region.trim().toLowerCase()));
        }
        if (sector != null && !sector.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("sector")), sector.trim().toLowerCase()));
        }
        return PageResponse.from(repository.findAll(spec, pageable).map(OrganisationMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public OrganisationResponse get(Long id) {
        return OrganisationMapper.toResponse(find(id));
    }

    public OrganisationResponse update(Long id, OrganisationRequest request) {
        Organisation o = find(id);
        OrganisationMapper.apply(o, request);
        return OrganisationMapper.toResponse(o);
    }

    public void delete(Long id) {
        repository.delete(find(id));
        repository.flush();   // surface FK violations now so they map to 409
    }

    private Organisation find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation " + id + " not found"));
    }
}
