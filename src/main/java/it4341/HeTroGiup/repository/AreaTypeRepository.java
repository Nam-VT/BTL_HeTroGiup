package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.AreaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaTypeRepository extends JpaRepository<AreaType, Long> {
}
