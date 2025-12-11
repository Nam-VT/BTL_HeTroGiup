package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.RoomSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomSchoolRepository extends JpaRepository<RoomSchool, Long> {
    List<RoomSchool> findBySchoolId(Long id);
}
