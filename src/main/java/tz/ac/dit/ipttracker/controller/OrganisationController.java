package tz.ac.dit.ipttracker.controller;

import tz.ac.dit.ipttracker.dto.OrganisationRequest;
import tz.ac.dit.ipttracker.dto.OrganisationResponse;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.service.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations")
@RequiredArgsConstructor
@Tag(name = "Organisations", description = "Host organisations for the placements")
public class OrganisationController {

    private final OrganisationService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create an organisation (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "403", description = "Not an admin")
    public ResponseEntity<OrganisationResponse> create(@Valid @RequestBody OrganisationRequest request) {
        OrganisationResponse created = service.create(request);
        return ResponseEntity.created(Locations.of("/api/v1/organisations/{id}", created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List organisations (paged, filter by region and sector)")
    @ApiResponse(responseCode = "200", description = "A page of organisations")
    public ResponseEntity<PageResponse<OrganisationResponse>> list(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String sector,
            Pageable pageable) {
        return ResponseEntity.ok(service.list(region, sector, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one organisation")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "Not found")
    public ResponseEntity<OrganisationResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an organisation (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "404", description = "Not found")
    public ResponseEntity<OrganisationResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody OrganisationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an organisation (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "Not found")
    @ApiResponse(responseCode = "409", description = "Still referenced by placements")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
