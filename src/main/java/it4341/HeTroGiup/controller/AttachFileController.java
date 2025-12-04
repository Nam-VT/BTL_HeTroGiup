package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.entity.AttachFile;
import it4341.HeTroGiup.service.AttachFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class AttachFileController {

    private final AttachFileService attachFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachFile> upload(@RequestParam("file") MultipartFile file) {
        AttachFile savedFile = attachFileService.uploadFile(file);

        return ResponseEntity.ok(savedFile);
    }
}