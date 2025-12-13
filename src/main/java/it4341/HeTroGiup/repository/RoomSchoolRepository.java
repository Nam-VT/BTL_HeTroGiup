package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.RoomSchool;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomSchoolRepository extends JpaRepository<RoomSchool, Long> {
    List<RoomSchool> findBySchoolId(Long id);

    List<RoomSchool> findByRoomId(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE RoomSchool rs SET rs.isDeleted = true WHERE rs.roomId IN :roomIds")
    void softDeleteByRoomId(@Param("roomIds") List<Long> roomIds);
}
