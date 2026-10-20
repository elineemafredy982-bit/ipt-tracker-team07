package tz.ac.dit.ipttracker.repository;

import tz.ac.dit.ipttracker.entity.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Page<Assessment> findByPlacementId(Long placementId, Pageable pageable);
}
