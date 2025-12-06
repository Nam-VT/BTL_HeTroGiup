package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    // 1. Lấy danh sách hiển thị (Sắp xếp tăng dần)
    List<SurveyQuestion> findBySurveyIdAndIsDeletedFalseOrderByQuestionOrderAsc(Long surveyId);

    // 2. Tìm số thứ tự lớn nhất hiện tại (Để Create)
    @Query("SELECT MAX(sq.questionOrder) FROM SurveyQuestion sq WHERE sq.survey.id = :surveyId AND sq.isDeleted = false")
    Integer findMaxOrder(Long surveyId);

    // 3. Kiểm tra thứ tự đã tồn tại chưa (Để Update)
    boolean existsBySurveyIdAndQuestionOrderAndIsDeletedFalse(Long surveyId, Integer questionOrder);

    // 4. Logic "Đẩy hàng": Tăng thứ tự lên 1 cho các câu phía sau
    @Modifying
    @Transactional
    @Query("UPDATE SurveyQuestion sq SET sq.questionOrder = sq.questionOrder + 1 " +
            "WHERE sq.survey.id = :surveyId AND sq.questionOrder >= :order AND sq.isDeleted = false")
    void incrementOrdersFrom(Long surveyId, Integer order);
}