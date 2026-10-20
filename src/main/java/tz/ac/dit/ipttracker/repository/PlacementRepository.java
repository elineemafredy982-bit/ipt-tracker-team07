package tz.ac.dit.ipttracker.repository;

import tz.ac.dit.ipttracker.entity.Placement;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacementRepository extends JpaRepository<Placement, Long> {

    /** True when the student already has a placement overlapping [start, end]. */
    boolean existsByStudentIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long studentId, LocalDate end, LocalDate start);

    Page<Placement> findByStudentId(Long studentId, Pageable pageable);

    Page<Placement> findByIndustrialSupervisorId(Long supervisorId, Pageable pageable);

    Page<Placement> findByAcademicSupervisorId(Long supervisorId, Pageable pageable);
}
