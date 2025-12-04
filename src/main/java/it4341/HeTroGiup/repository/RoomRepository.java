package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByLandlordIdAndIsDeletedFalseOrderByIdDesc(Long landlordId);
}