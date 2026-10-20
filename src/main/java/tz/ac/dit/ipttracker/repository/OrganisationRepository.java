package tz.ac.dit.ipttracker.repository;

import tz.ac.dit.ipttracker.entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrganisationRepository
        extends JpaRepository<Organisation, Long>, JpaSpecificationExecutor<Organisation> {
}
