package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.dto.SurveyQuestionDTO;
import it4341.HeTroGiup.service.SurveyQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/survey-questions")
@RequiredArgsConstructor
public class SurveyQuestionController {

    private final SurveyQuestionService questionService;

    // 1. Create
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> req) {
        return execute(() -> questionService.createQuestion(req));
    }

    // 2. Update
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, Object> req) {
        return execute(() -> questionService.updateQuestion(req));
    }

    // 3. Delete
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, Object> req) {
        return execute(() -> {
            List<Integer> ids = (List<Integer>) req.get("ids");
            questionService.deleteQuestions(ids);
            return "successful";
        });
    }

    // 4. Get All (Dùng POST vì gửi body surveyId)
    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll(@RequestBody Map<String, Object> req) {
        return execute(() -> {
            Long surveyId = Long.valueOf(req.get("surveyId").toString());
            return questionService.getAll(surveyId);
        });
    }

    // 5. Reorder
    @PostMapping("/reorder")
    public ResponseEntity<Map<String, Object>> reorder(@RequestBody Map<String, Object> req) {
        return execute(() -> {
            Long surveyId = Long.valueOf(req.get("surveyId").toString());
            List<Map<String, Object>> orders = (List<Map<String, Object>>) req.get("orders");
            questionService.reorderQuestions(surveyId, orders);
            return "successful";
        });
    }

    // 5. API Sắp xếp thứ tự
    // Path: {{beURL}} /survey-questions/reorder
//    @PostMapping("/reorder")
//    public ResponseEntity<Map<String, Object>> reorder(@RequestBody Map<String, Object> req) {
//        return execute(() -> {
//            Long surveyId = Long.valueOf(req.get("surveyId").toString());
//            List<Map<String, Object>> orders = (List<Map<String, Object>>) req.get("orders");
//
//            // Gọi Service (Hàm này giờ đã trả về List)
//            return questionService.reorderQuestions(surveyId, orders);
//        });
//    }

    // --- Hàm Helper xử lý Try-Catch chung ---
    private ResponseEntity<Map<String, Object>> execute(Task task) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Object data = task.perform();
            response.put("code", "00");
            response.put("message", null);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", "9999");
            response.put("message", "Lỗi: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    interface Task {
        Object perform() throws Exception;
    }
}