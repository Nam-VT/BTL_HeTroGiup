package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    @Query("SELECT s FROM School s WHERE s.isDeleted = false " +
            "AND LOWER(s.schoolName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<School> findByNameLike(@Param("name") String name);

}
