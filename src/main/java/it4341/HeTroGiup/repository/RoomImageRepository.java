package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.RoomImage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    @Modifying
    @Transactional
    void deleteByRoomId(Long roomId);

    @Modifying
    @Transactional
    @Query("UPDATE RoomImage r SET r.isDeleted = true WHERE r.room.id IN :roomIds")
    void softDeleteByRoomId(@Param("roomIds") List<Long> roomIds);

}