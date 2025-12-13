package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.service.DSSService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dss")
@RequiredArgsConstructor
public class DSSController {
    private final DSSService dssService;
        @PostMapping("/decision-table")
        public ResponseEntity<Map<String, Object>> createDecisionTable( @RequestBody Map<String, Object> req) {
            Map<String, Object> response = new LinkedHashMap<>();
            try {
                List<List<Double>> data = dssService.createDecisionTable(req);

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

        @PostMapping("/normalize-decision-table")
        public ResponseEntity<Map<String, Object>> normalizeDecisionTable( @RequestBody Map<String, Object> req) {
            Map<String, Object> response = new LinkedHashMap<>();
            try {
                List<List<Double>> data = dssService.normalizeDecisionTable(req);

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

        @PostMapping("/weight-calculate")
        public ResponseEntity<Map<String, Object>> calculateWeightedMatrix( @RequestBody Map<String, Object> req) {
            Map<String, Object> response = new LinkedHashMap<>();
            try {
                List<List<Double>> weights = dssService.calculateWeightedMatrix(req);

                response.put("code", "00");
                response.put("message", null);
                response.put("data", weights);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                response.put("code", "9999");
                response.put("message", "Lỗi: " + e.getMessage());
                response.put("data", null);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

    @PostMapping("/topsis")
    public ResponseEntity<Map<String, Object>> calculateTOPSIS(@RequestBody Map<String, Object> req) {
        // Tạo Map response theo thứ tự (LinkedHashMap giữ thứ tự put)
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // 1. Gọi Service để lấy cục data lõi (a_star, c_star...)
            Map<String, Object> logicResult = dssService.calculateTOPSIS(req);

            // 2. Đóng gói response thành công
            response.put("code", "00");
            response.put("message", null);
            response.put("data", logicResult);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 3. Đóng gói response lỗi
            response.put("code", "9999");
            response.put("message", "Lỗi xử lý: " + e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
