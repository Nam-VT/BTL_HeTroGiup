package it4341.HeTroGiup.service;

import it4341.HeTroGiup.dto.SurveyQuestionDTO;
import it4341.HeTroGiup.entity.Survey;
import it4341.HeTroGiup.entity.SurveyQuestion;
import it4341.HeTroGiup.repository.SurveyQuestionRepository;
import it4341.HeTroGiup.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SurveyQuestionService {

    private final SurveyQuestionRepository questionRepository;
    private final SurveyRepository surveyRepository;

    // --- 1. CREATE: Tự động tính Max Order + 1 ---
    @Transactional
    public SurveyQuestionDTO createQuestion(Map<String, Object> req) {
        Long surveyId = Long.valueOf(req.get("surveyId").toString());
        String text = (String) req.get("questionText");

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey không tồn tại"));

        // Tính Order mới
        Integer maxOrder = questionRepository.findMaxOrder(surveyId);
        int newOrder = (maxOrder == null) ? 1 : maxOrder + 1;

        SurveyQuestion entity = SurveyQuestion.builder()
                .survey(survey)
                .questionText(text)
                .questionOrder(newOrder)
                .isDeleted(false)
                .build();

        return toDTO(questionRepository.save(entity));
    }

    // --- 2. UPDATE: Logic chèn và đẩy order ---
    @Transactional
    public SurveyQuestionDTO updateQuestion(Map<String, Object> req) {
        Long id = Long.valueOf(req.get("id").toString());
        Long surveyId = Long.valueOf(req.get("surveyId").toString());
        String text = (String) req.get("questionText");
        Integer newOrder = Integer.valueOf(req.get("questionOrder").toString());

        SurveyQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại"));

        // Nếu đổi sang thứ tự mới mà thứ tự đó đã có người ngồi
        if (!question.getQuestionOrder().equals(newOrder)) {
            if (questionRepository.existsBySurveyIdAndQuestionOrderAndIsDeletedFalse(surveyId, newOrder)) {
                questionRepository.incrementOrdersFrom(surveyId, newOrder);
            }
        }

        question.setQuestionText(text);
        question.setQuestionOrder(newOrder);

        return toDTO(questionRepository.save(question));
    }

    // --- 3. DELETE: Xóa nhiều ID ---
    @Transactional
    public void deleteQuestions(List<Integer> ids) {
        for (Integer id : ids) {
            SurveyQuestion q = questionRepository.findById(Long.valueOf(id)).orElse(null);
            if (q != null) {
                q.setIsDeleted(true);
                questionRepository.save(q);
            }
        }
    }

    // --- 4. GET ALL ---
    public List<SurveyQuestionDTO> getAll(Long surveyId) {
        return questionRepository.findBySurveyIdAndIsDeletedFalseOrderByQuestionOrderAsc(surveyId)
                .stream().map(this::toDTO).toList();
    }

    // --- 5. REORDER (Sắp xếp lại toàn bộ) ---
    @Transactional
    public void reorderQuestions(Long surveyId, List<Map<String, Object>> orders) {
        for (Map<String, Object> item : orders) {
            Long qId = Long.valueOf(item.get("id").toString());
            Integer newOrder = Integer.valueOf(item.get("questionOrder").toString());

            SurveyQuestion q = questionRepository.findById(qId).orElse(null);
            if (q != null && q.getSurvey().getId().equals(surveyId)) {
                q.setQuestionOrder(newOrder);
                questionRepository.save(q);
            }
        }
    }
    // --- 5. REORDER: Sắp xếp và TRẢ VỀ LUÔN danh sách mới ---
//    @Transactional
//    public List<SurveyQuestionDTO> reorderQuestions(Long surveyId, List<Map<String, Object>> orders) {
//
//        // 1. Lưu thứ tự mới (Logic cũ)
//        for (Map<String, Object> item : orders) {
//            Long qId = Long.valueOf(item.get("id").toString());
//            Integer newOrder = Integer.valueOf(item.get("questionOrder").toString());
//
//            SurveyQuestion q = questionRepository.findById(qId).orElse(null);
//
//            if (q != null && q.getSurvey().getId().equals(surveyId) && !q.getIsDeleted()) {
//                q.setQuestionOrder(newOrder);
//                questionRepository.save(q);
//            }
//        }
//        questionRepository.flush();
//        // 2. QUAN TRỌNG: Lấy lại danh sách đã sắp xếp để trả về luôn
//        // (Gọi lại hàm getAll ở ngay trong class này)
//        return this.getAll(surveyId);
//    }

    private SurveyQuestionDTO toDTO(SurveyQuestion e) {
        return SurveyQuestionDTO.builder()
                .id(e.getId())
                .surveyId(e.getSurvey().getId())
                .questionText(e.getQuestionText())
                .questionOrder(e.getQuestionOrder())
                .build();
    }
}