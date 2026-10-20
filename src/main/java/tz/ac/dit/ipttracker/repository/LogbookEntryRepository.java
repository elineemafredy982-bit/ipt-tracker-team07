package tz.ac.dit.ipttracker.repository;

import tz.ac.dit.ipttracker.entity.EntryStatus;
import tz.ac.dit.ipttracker.entity.LogbookEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogbookEntryRepository
        extends JpaRepository<LogbookEntry, Long>, JpaSpecificationExecutor<LogbookEntry> {

    boolean existsByPlacementIdAndEntryDate(Long placementId, LocalDate date);

    Page<LogbookEntry> findByPlacementId(Long placementId, Pageable pageable);

    @Query("select e from LogbookEntry e where e.status = :status "
            + "and e.placement.industrialSupervisor.id = :supervisorId order by e.submittedAt asc")
    Page<LogbookEntry> findPending(@Param("status") EntryStatus status,
                                   @Param("supervisorId") Long supervisorId, Pageable pageable);

    /** Aggregate per status: COUNT and SUM computed by the database (Step 5). */
    @Query("select e.status as status, count(e) as total, coalesce(sum(e.hoursWorked), 0) as hours "
            + "from LogbookEntry e where e.placement.id = :placementId group by e.status")
    List<StatusStat> statusStats(@Param("placementId") Long placementId);

    @Query("select e.entryDate from LogbookEntry e where e.placement.id = :placementId")
    List<LocalDate> findEntryDates(@Param("placementId") Long placementId);

    interface StatusStat {
        EntryStatus getStatus();

        long getTotal();

        long getHours();
    }
}
