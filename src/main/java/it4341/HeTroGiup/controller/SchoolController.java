package it4341.HeTroGiup.controller;


import it4341.HeTroGiup.dto.request.SchoolRequest;
import it4341.HeTroGiup.dto.response.ApiResponse;
import it4341.HeTroGiup.dto.response.SchoolResponse;
import it4341.HeTroGiup.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/schools")
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping("/all")
    public ResponseEntity<ApiResponse> getAllSchool(@RequestBody SchoolRequest request) {
        try {
            // Service trả về List<SchoolResponse>
            List<SchoolResponse> result = schoolService.getAllSchool(request);
            return ResponseEntity.ok(new ApiResponse("00", null, result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("exception", e.getMessage(), null));
        }
    }

}
