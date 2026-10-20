package tz.ac.dit.ipttracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tz.ac.dit.ipttracker.dto.CreateEntryRequest;
import tz.ac.dit.ipttracker.dto.EntryResponse;
import tz.ac.dit.ipttracker.dto.ReviewRequest;
import tz.ac.dit.ipttracker.dto.UpdateEntryRequest;
import tz.ac.dit.ipttracker.entity.EntryStatus;
import tz.ac.dit.ipttracker.entity.LogbookEntry;
import tz.ac.dit.ipttracker.entity.Placement;
import tz.ac.dit.ipttracker.entity.Role;
import tz.ac.dit.ipttracker.entity.User;
import tz.ac.dit.ipttracker.exception.BusinessRuleException;
import tz.ac.dit.ipttracker.repository.LogbookEntryRepository;
import tz.ac.dit.ipttracker.repository.UserRepository;
import tz.ac.dit.ipttracker.security.AppUserDetails;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EntryServiceTest {

    private static final long STUDENT_ID = 1L;
    private static final long SUPERVISOR_ID = 10L;
    private static final long PLACEMENT_ID = 5L;

    @Mock LogbookEntryRepository entries;
    @Mock UserRepository users;
    @Mock AccessPolicy access;

    private EntryService service;
    private Placement placement;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC);
        service = new EntryService(entries, users, access, clock);

        placement = new Placement();
        placement.setId(PLACEMENT_ID);
        placement.setStudent(user(STUDENT_ID));
        placement.setIndustrialSupervisor(user(SUPERVISOR_ID));
        placement.setStartDate(LocalDate.parse("2026-10-01"));
        placement.setEndDate(LocalDate.parse("2026-12-31"));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private static void loginAs(long id, Role role) {
        AppUserDetails principal = new AppUserDetails(id, "u" + id + "@ipt.test", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private LogbookEntry entryWith(EntryStatus status) {
        LogbookEntry e = new LogbookEntry();
        e.setId(100L);
        e.setPlacement(placement);
        e.setEntryDate(LocalDate.parse("2026-10-19"));
        e.setTasksDone("work");
        e.setHoursWorked(8);
        e.setStatus(status);
        return e;
    }

    private static CreateEntryRequest request(String date, int hours) {
        return new CreateEntryRequest(LocalDate.parse(date), "Did some work", "Learned things", hours);
    }

    // ---------------------------------------------------------------- create

    @Test
    void create_whenValid_savesDraftEntry() {
        loginAs(STUDENT_ID, Role.STUDENT);
        when(access.loadPlacement(PLACEMENT_ID)).thenReturn(placement);
        when(entries.existsByPlacementIdAndEntryDate(PLACEMENT_ID, LocalDate.parse("2026-10-19"))).thenReturn(false);
        when(entries.save(any(LogbookEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        EntryResponse r = service.create(PLACEMENT_ID, request("2026-10-19", 8));

        assertThat(r.status()).isEqualTo(EntryStatus.DRAFT);
        assertThat(r.hoursWorked()).isEqualTo(8);
    }

    @Test
    void create_whenDuplicateDate_throwsBusinessRuleException() {
        loginAs(STUDENT_ID, Role.STUDENT);
        when(access.loadPlacement(PLACEMENT_ID)).thenReturn(placement);
        when(entries.existsByPlacementIdAndEntryDate(PLACEMENT_ID, LocalDate.parse("2026-10-19"))).thenReturn(true);

        assertThatThrownBy(() -> service.create(PLACEMENT_ID, request("2026-10-19", 8)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("An entry already exists for 2026-10-19");
        verify(entries, never()).save(any());
    }

    @Test
    void create_whenDateOutsidePlacementPeriod_throwsBusinessRuleException() {
        loginAs(STUDENT_ID, Role.STUDENT);
        when(access.loadPlacement(PLACEMENT_ID)).thenReturn(placement);

        assertThatThrownBy(() -> service.create(PLACEMENT_ID, request("2026-09-15", 8)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("outside the placement period");
    }

    @Test
    void create_whenDateInTheFuture_throwsBusinessRuleException() {
        loginAs(STUDENT_ID, Role.STUDENT);
        when(access.loadPlacement(PLACEMENT_ID)).thenReturn(placement);

        assertThatThrownBy(() -> service.create(PLACEMENT_ID, request("2026-10-21", 8)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("future");
    }

    @Test
    void create_whenHoursAboveTwelve_throwsBusinessRuleException() {
        loginAs(STUDENT_ID, Role.STUDENT);
        when(access.loadPlacement(PLACEMENT_ID)).thenReturn(placement);

        assertThatThrownBy(() -> service.create(PLACEMENT_ID, request("2026-10-19", 13)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("between 1 and 12");
    }

    @Test
    void create_whenStudentDoesNotOwnPlacement_throwsAccessDenied() {
        loginAs(2L, Role.STUDENT);
        when(access.loadPlacement(PLACEMENT_ID)).thenReturn(placement);

        assertThatThrownBy(() -> service.create(PLACEMENT_ID, request("2026-10-19", 8)))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ---------------------------------------------------------------- workflow

    @Test
    void submit_whenEntryAlreadyApproved_throwsBusinessRuleException() {
        loginAs(STUDENT_ID, Role.STUDENT);
        when(entries.findById(100L)).thenReturn(Optional.of(entryWith(EntryStatus.APPROVED)));

        assertThatThrownBy(() -> service.submit(100L)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void reject_withoutComment_throwsBusinessRuleException() {
        loginAs(SUPERVISOR_ID, Role.INDUSTRIAL_SUPERVISOR);
        when(entries.findById(100L)).thenReturn(Optional.of(entryWith(EntryStatus.SUBMITTED)));
        when(users.getReferenceById(SUPERVISOR_ID)).thenReturn(user(SUPERVISOR_ID));

        assertThatThrownBy(() -> service.reject(100L, new ReviewRequest("")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("comment");
    }

    @Test
    void approve_whenSupervisorNotAssignedToPlacement_throwsAccessDenied() {
        loginAs(99L, Role.INDUSTRIAL_SUPERVISOR);
        when(entries.findById(100L)).thenReturn(Optional.of(entryWith(EntryStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.approve(100L, null)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void approve_whenAssignedSupervisor_setsApproved() {
        loginAs(SUPERVISOR_ID, Role.INDUSTRIAL_SUPERVISOR);
        when(entries.findById(100L)).thenReturn(Optional.of(entryWith(EntryStatus.SUBMITTED)));
        when(users.getReferenceById(SUPERVISOR_ID)).thenReturn(user(SUPERVISOR_ID));

        EntryResponse r = service.approve(100L, new ReviewRequest("Good"));

        assertThat(r.status()).isEqualTo(EntryStatus.APPROVED);
        assertThat(r.reviewedAt()).isNotNull();
    }

    @Test
    void update_whenEntryIsSubmitted_throwsBusinessRuleException() {
        loginAs(STUDENT_ID, Role.STUDENT);
        when(entries.findById(100L)).thenReturn(Optional.of(entryWith(EntryStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.update(100L, new UpdateEntryRequest("new", null, 5)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void delete_whenEntryIsNotDraft_throwsBusinessRuleException() {
        loginAs(STUDENT_ID, Role.STUDENT);
        when(entries.findById(100L)).thenReturn(Optional.of(entryWith(EntryStatus.REJECTED)));

        assertThatThrownBy(() -> service.delete(100L)).isInstanceOf(BusinessRuleException.class);
        verify(entries, never()).delete(any());
    }
}
