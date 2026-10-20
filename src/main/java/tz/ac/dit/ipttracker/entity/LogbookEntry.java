package tz.ac.dit.ipttracker.entity;

import tz.ac.dit.ipttracker.exception.BusinessRuleException;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A daily logbook entry. The workflow rules live here (not in the services):
 * DRAFT -> SUBMITTED -> APPROVED, or SUBMITTED -> REJECTED -> (edit) -> SUBMITTED.
 */
@Entity
@Table(name = "logbook_entries")
@Getter
@Setter
@NoArgsConstructor
public class LogbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placement_id")
    private Placement placement;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "tasks_done", nullable = false, length = 2000)
    private String tasksDone;

    @Column(name = "skills_learned", length = 2000)
    private String skillsLearned;

    @Column(name = "hours_worked", nullable = false)
    private Integer hoursWorked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntryStatus status = EntryStatus.DRAFT;

    @Column(name = "supervisor_comment", length = 1000)
    private String supervisorComment;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    // ---------------------------------------------------------------- transitions

    /** Student action: DRAFT or REJECTED -> SUBMITTED. */
    public void submit() {
        if (status != EntryStatus.DRAFT && status != EntryStatus.REJECTED) {
            throw new BusinessRuleException("Only DRAFT or REJECTED entries can be submitted (current status: "
                    + status + ")");
        }
        status = EntryStatus.SUBMITTED;
        submittedAt = Instant.now();
        reviewedAt = null;
    }

    public void approve(User supervisor) {
        approve(supervisor, null);
    }

    /** Supervisor action: SUBMITTED -> APPROVED. */
    public void approve(User supervisor, String comment) {
        requireSubmitted("approved");
        requireAssignedSupervisor(supervisor);
        status = EntryStatus.APPROVED;
        supervisorComment = comment;
        reviewedAt = Instant.now();
    }

    /** Supervisor action: SUBMITTED -> REJECTED; a comment is mandatory. */
    public void reject(User supervisor, String comment) {
        requireSubmitted("rejected");
        requireAssignedSupervisor(supervisor);
        if (comment == null || comment.isBlank()) {
            throw new BusinessRuleException("A rejection must include a comment");
        }
        status = EntryStatus.REJECTED;
        supervisorComment = comment.trim();
        reviewedAt = Instant.now();
    }

    /** Student may only change DRAFT and REJECTED entries (business rule 4). */
    public void assertEditable() {
        if (status == EntryStatus.SUBMITTED || status == EntryStatus.APPROVED) {
            throw new BusinessRuleException("A " + status + " entry is read-only and cannot be edited");
        }
    }

    private void requireSubmitted(String action) {
        if (status != EntryStatus.SUBMITTED) {
            throw new BusinessRuleException("Only SUBMITTED entries can be " + action + " (current status: "
                    + status + ")");
        }
    }

    private void requireAssignedSupervisor(User supervisor) {
        User assigned = placement.getIndustrialSupervisor();
        if (supervisor == null || assigned == null || !assigned.getId().equals(supervisor.getId())) {
            throw new BusinessRuleException("Only the industrial supervisor assigned to this placement "
                    + "can review its entries");
        }
    }
}
