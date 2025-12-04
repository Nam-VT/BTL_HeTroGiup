package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    void deleteByRoomId(Long roomId);
}