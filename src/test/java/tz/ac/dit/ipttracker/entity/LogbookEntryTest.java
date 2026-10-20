package tz.ac.dit.ipttracker.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tz.ac.dit.ipttracker.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

/** Pure unit tests of the workflow rules: no Spring, no database. */
class LogbookEntryTest {

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private static LogbookEntry entryWith(EntryStatus status) {
        Placement p = new Placement();
        p.setIndustrialSupervisor(user(10L));
        LogbookEntry e = new LogbookEntry();
        e.setPlacement(p);
        e.setStatus(status);
        return e;
    }

    @Test
    void submit_whenDraft_becomesSubmittedAndSetsSubmittedAt() {
        LogbookEntry e = entryWith(EntryStatus.DRAFT);
        e.submit();
        assertThat(e.getStatus()).isEqualTo(EntryStatus.SUBMITTED);
        assertThat(e.getSubmittedAt()).isNotNull();
    }

    @Test
    void submit_whenRejected_becomesSubmittedAgain() {
        LogbookEntry e = entryWith(EntryStatus.REJECTED);
        e.submit();
        assertThat(e.getStatus()).isEqualTo(EntryStatus.SUBMITTED);
    }

    @Test
    void submit_whenAlreadyApproved_throwsBusinessRuleException() {
        LogbookEntry e = entryWith(EntryStatus.APPROVED);
        assertThatThrownBy(e::submit).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void submit_whenAlreadySubmitted_throwsBusinessRuleException() {
        LogbookEntry e = entryWith(EntryStatus.SUBMITTED);
        assertThatThrownBy(e::submit).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void approve_whenSubmittedAndSupervisorAssigned_becomesApprovedAndSetsReviewedAt() {
        LogbookEntry e = entryWith(EntryStatus.SUBMITTED);
        e.approve(user(10L));
        assertThat(e.getStatus()).isEqualTo(EntryStatus.APPROVED);
        assertThat(e.getReviewedAt()).isNotNull();
    }

    @Test
    void approve_whenEntryIsDraft_throwsBusinessRuleException() {
        LogbookEntry e = entryWith(EntryStatus.DRAFT);
        assertThatThrownBy(() -> e.approve(user(10L))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void approve_whenSupervisorNotAssigned_throwsBusinessRuleException() {
        LogbookEntry e = entryWith(EntryStatus.SUBMITTED);
        assertThatThrownBy(() -> e.approve(user(99L))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void reject_withComment_becomesRejectedAndStoresComment() {
        LogbookEntry e = entryWith(EntryStatus.SUBMITTED);
        e.reject(user(10L), "Add more detail");
        assertThat(e.getStatus()).isEqualTo(EntryStatus.REJECTED);
        assertThat(e.getSupervisorComment()).isEqualTo("Add more detail");
    }

    @Test
    void reject_withoutComment_throwsBusinessRuleException() {
        LogbookEntry e = entryWith(EntryStatus.SUBMITTED);
        assertThatThrownBy(() -> e.reject(user(10L), "  "))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("comment");
    }

    @Test
    void assertEditable_whenSubmitted_throwsBusinessRuleException() {
        assertThatThrownBy(() -> entryWith(EntryStatus.SUBMITTED).assertEditable())
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void assertEditable_whenDraftOrRejected_isAllowed() {
        assertThatCode(() -> entryWith(EntryStatus.DRAFT).assertEditable()).doesNotThrowAnyException();
        assertThatCode(() -> entryWith(EntryStatus.REJECTED).assertEditable()).doesNotThrowAnyException();
    }
}
