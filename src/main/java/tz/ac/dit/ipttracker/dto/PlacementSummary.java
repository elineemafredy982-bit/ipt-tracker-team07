package tz.ac.dit.ipttracker.dto;

public record PlacementSummary(Long placementId, String studentName, String organisation, long totalEntries,
                               long approved, long pending, long rejected, long totalApprovedHours,
                               long daysWithoutEntry) {
}
