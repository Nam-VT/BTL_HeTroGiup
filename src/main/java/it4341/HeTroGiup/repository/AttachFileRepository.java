package it4341.HeTroGiup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import it4341.HeTroGiup.entity.AttachFile;

@Repository
public interface AttachFileRepository extends JpaRepository<AttachFile, Long> {
}