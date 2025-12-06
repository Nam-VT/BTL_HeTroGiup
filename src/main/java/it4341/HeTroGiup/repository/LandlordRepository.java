package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.Landlord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LandlordRepository extends JpaRepository<Landlord, Long> {
    Optional<Landlord> findByEmail(String email);
    boolean existsByEmail(String email);
}
