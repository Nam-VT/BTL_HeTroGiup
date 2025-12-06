package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.Room;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    void deleteById(Long roomId);

    Page<Room> findByIsDeletedFalse(Pageable pageable);
}