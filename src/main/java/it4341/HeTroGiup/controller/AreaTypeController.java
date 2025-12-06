package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.dto.AreaTypeDTO;
import it4341.HeTroGiup.service.AreaTypeService;

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
@RequestMapping("/area-types")
@RequiredArgsConstructor
public class AreaTypeController {

    private final AreaTypeService areaTypeService;

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllAreaTypes() {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            List<AreaTypeDTO> data = areaTypeService.getAllAreaTypes();

            response.put("code", "00");
            response.put("message", null);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Xử lý lỗi
            response.put("code", "9999");
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            response.put("data", null);

            // Trả về HTTP 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}