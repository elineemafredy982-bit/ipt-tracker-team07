package tz.ac.dit.ipttracker.controller;

import tz.ac.dit.ipttracker.dto.EntryResponse;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.service.EntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/supervisor")
@RequiredArgsConstructor
@Tag(name = "Supervisor", description = "Supervisor work queue")
public class SupervisorController {

    private final EntryService service;

    @GetMapping("/pending-entries")
    @PreAuthorize("hasRole('INDUSTRIAL_SUPERVISOR')")
    @Operation(summary = "SUBMITTED entries awaiting review, oldest first (industrial supervisor)")
    @ApiResponse(responseCode = "200", description = "A page of pending entries")
    @ApiResponse(responseCode = "403", description = "Not an industrial supervisor")
    public ResponseEntity<PageResponse<EntryResponse>> pending(Pageable pageable) {
        return ResponseEntity.ok(service.pending(pageable));
    }
}
