package tz.ac.dit.ipttracker.controller;

import tz.ac.dit.ipttracker.dto.AssessmentRequest;
import tz.ac.dit.ipttracker.dto.AssessmentResponse;
import tz.ac.dit.ipttracker.dto.CreateEntryRequest;
import tz.ac.dit.ipttracker.dto.CreatePlacementRequest;
import tz.ac.dit.ipttracker.dto.EntryResponse;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.dto.PlacementResponse;
import tz.ac.dit.ipttracker.dto.PlacementSummary;
import tz.ac.dit.ipttracker.entity.EntryStatus;
import tz.ac.dit.ipttracker.service.AssessmentService;
import tz.ac.dit.ipttracker.service.EntryService;
import tz.ac.dit.ipttracker.service.PlacementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/placements")
@RequiredArgsConstructor
@Tag(name = "Placements", description = "Placements, their logbook entries, summary and assessments")
public class PlacementController {

    private final PlacementService placementService;
    private final EntryService entryService;
    private final AssessmentService assessmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a placement and assign supervisors (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "422", description = "Business rule violated (dates, roles, overlap)")
    public ResponseEntity<PlacementResponse> create(@Valid @RequestBody CreatePlacementRequest request) {
        PlacementResponse created = placementService.create(request);
        return ResponseEntity.created(Locations.of("/api/v1/placements/{id}", created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List the placements visible to the logged-in user (paged)")
    @ApiResponse(responseCode = "200", description = "A page of placements")
    public ResponseEntity<PageResponse<PlacementResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(placementService.list(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a placement with the names of the student and supervisors")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "403", description = "Not your placement")
    @ApiResponse(responseCode = "404", description = "Not found")
    public ResponseEntity<PlacementResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(placementService.get(id));
    }

    @PostMapping("/{id}/entries")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Create a DRAFT logbook entry (owning student)")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "403", description = "Not your placement")
    @ApiResponse(responseCode = "422", description = "Duplicate date, outside period or in the future")
    public ResponseEntity<EntryResponse> createEntry(@PathVariable Long id,
                                                     @Valid @RequestBody CreateEntryRequest request) {
        EntryResponse created = entryService.create(id, request);
        return ResponseEntity.created(Locations.of("/api/v1/entries/{id}", created.id())).body(created);
    }

    @GetMapping("/{id}/entries")
    @Operation(summary = "List entries of a placement (filter by status, from, to; paged)")
    @ApiResponse(responseCode = "200", description = "A page of entries")
    @ApiResponse(responseCode = "403", description = "Not your placement")
    public ResponseEntity<PageResponse<EntryResponse>> listEntries(
            @PathVariable Long id,
            @RequestParam(required = false) EntryStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(entryService.list(id, status, from, to, pageable));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Progress summary computed with aggregate queries")
    @ApiResponse(responseCode = "200", description = "Summary")
    @ApiResponse(responseCode = "403", description = "Not your placement")
    public ResponseEntity<PlacementSummary> summary(@PathVariable Long id) {
        return ResponseEntity.ok(placementService.summary(id));
    }

    @PostMapping("/{id}/assessments")
    @PreAuthorize("hasRole('ACADEMIC_SUPERVISOR')")
    @Operation(summary = "Record a visit assessment (assigned academic supervisor)")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Score must be 0-100")
    @ApiResponse(responseCode = "403", description = "Not assigned to this placement")
    public ResponseEntity<AssessmentResponse> createAssessment(@PathVariable Long id,
                                                               @Valid @RequestBody AssessmentRequest request) {
        AssessmentResponse created = assessmentService.create(id, request);
        return ResponseEntity.created(Locations.of("/api/v1/placements/{id}/assessments", id))
                .body(created);
    }

    @GetMapping("/{id}/assessments")
    @Operation(summary = "List assessments of a placement (paged)")
    @ApiResponse(responseCode = "200", description = "A page of assessments")
    @ApiResponse(responseCode = "403", description = "Not your placement")
    public ResponseEntity<PageResponse<AssessmentResponse>> listAssessments(@PathVariable Long id,
                                                                            Pageable pageable) {
        return ResponseEntity.ok(assessmentService.list(id, pageable));
    }
}
