package tz.ac.dit.ipttracker.controller;

import tz.ac.dit.ipttracker.dto.EntryResponse;
import tz.ac.dit.ipttracker.dto.ReviewRequest;
import tz.ac.dit.ipttracker.dto.UpdateEntryRequest;
import tz.ac.dit.ipttracker.service.EntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entries")
@RequiredArgsConstructor
@Tag(name = "Logbook entries", description = "Edit, submit, approve and reject entries")
public class EntryController {

    private final EntryService service;

    @GetMapping("/{id}")
    @Operation(summary = "Get one entry (anyone who may see its placement)")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "403", description = "Not your placement")
    @ApiResponse(responseCode = "404", description = "Not found")
    public ResponseEntity<EntryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Edit a DRAFT or REJECTED entry (owning student)")
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "403", description = "Not your entry")
    @ApiResponse(responseCode = "422", description = "Entry is SUBMITTED or APPROVED (read-only)")
    public ResponseEntity<EntryResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateEntryRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Delete a DRAFT entry (owning student)")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "422", description = "Only DRAFT entries can be deleted")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit an entry: DRAFT or REJECTED -> SUBMITTED")
    @ApiResponse(responseCode = "200", description = "Submitted")
    @ApiResponse(responseCode = "422", description = "Illegal transition")
    public ResponseEntity<EntryResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('INDUSTRIAL_SUPERVISOR')")
    @Operation(summary = "Approve a submitted entry: SUBMITTED -> APPROVED (assigned supervisor)")
    @ApiResponse(responseCode = "200", description = "Approved")
    @ApiResponse(responseCode = "403", description = "Not the assigned supervisor")
    @ApiResponse(responseCode = "422", description = "Illegal transition")
    public ResponseEntity<EntryResponse> approve(@PathVariable Long id,
                                                 @Valid @RequestBody(required = false) ReviewRequest request) {
        return ResponseEntity.ok(service.approve(id, request));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('INDUSTRIAL_SUPERVISOR')")
    @Operation(summary = "Reject a submitted entry with a mandatory comment: SUBMITTED -> REJECTED")
    @ApiResponse(responseCode = "200", description = "Rejected")
    @ApiResponse(responseCode = "403", description = "Not the assigned supervisor")
    @ApiResponse(responseCode = "422", description = "Missing comment or illegal transition")
    public ResponseEntity<EntryResponse> reject(@PathVariable Long id,
                                                @Valid @RequestBody(required = false) ReviewRequest request) {
        return ResponseEntity.ok(service.reject(id, request));
    }
}
