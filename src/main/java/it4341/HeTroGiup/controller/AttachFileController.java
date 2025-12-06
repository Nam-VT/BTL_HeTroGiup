package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.dto.response.ApiResponse;
import it4341.HeTroGiup.entity.AttachFile;
import it4341.HeTroGiup.service.AttachFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class AttachFileController {

    private final AttachFileService attachFileService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> upload(@RequestParam("files") List<MultipartFile> files) {
        try {
//            // Cố gắng upload
//            AttachFile savedFile = attachFileService.uploadFile(file);

            List<AttachFile> savedFile = attachFileService.uploadFiles(files);

            // THÀNH CÔNG: code 00
            return ResponseEntity.ok(new ApiResponse("00", null, savedFile));

        } catch (Exception e) {
            // THẤT BẠI: code Exception, message lấy từ Service
            return ResponseEntity.badRequest().body(new ApiResponse("EXCEPTION", e.getMessage(), null));
        }
    }
}