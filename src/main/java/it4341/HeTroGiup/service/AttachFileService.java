package it4341.HeTroGiup.service;

import it4341.HeTroGiup.entity.AttachFile;
import it4341.HeTroGiup.repository.AttachFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import lombok.Builder;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class AttachFileService {

    private final AttachFileRepository attachFileRepository;
    private final Path fileStorageLocation;

    public AttachFileService(AttachFileRepository attachFileRepository,
                             @Value("${app.upload.dir}") String uploadDir) {
        this.attachFileRepository = attachFileRepository;

        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ tại " + this.fileStorageLocation, ex);
        }
    }

    @Transactional
    public AttachFile uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File rỗng");
        }

        // 1. Chuẩn hóa tên file
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // 2. Tạo tên file mới (UUID)
            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) fileExtension = originalFileName.substring(i);

            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // 3. Copy file vào thư mục đích
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 4. Tạo URL để frontend truy cập
            String fileUrl = "/uploads/" + newFileName;

            // 5. Lưu DB
            AttachFile attachFile = AttachFile.builder()
                    .url(fileUrl)
                    .isDeleted(false)
                    .build();

            return attachFileRepository.save(attachFile);

        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file " + originalFileName, ex);
        }
    }
}