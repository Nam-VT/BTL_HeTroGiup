package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.dto.SurveyDTO;
import it4341.HeTroGiup.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllSurveys() {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<SurveyDTO> data = surveyService.getAllSurveys();

            response.put("code", "00");
            response.put("message", null);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", "9999");
            response.put("message", "Lỗi: " + e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}