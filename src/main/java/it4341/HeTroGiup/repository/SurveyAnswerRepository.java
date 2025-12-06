package it4341.HeTroGiup.repository;
import it4341.HeTroGiup.entity.SurveyAnswer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {
    @Modifying
    @Transactional
    void deleteByRoomId(Long roomId);
}