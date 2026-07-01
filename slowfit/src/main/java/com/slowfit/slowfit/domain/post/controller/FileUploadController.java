package com.slowfit.slowfit.domain.post.controller;

import com.slowfit.slowfit.global.service.ImageModerationService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    private final Path uploadDirectory;
    private final ImageModerationService imageModerationService;

    public FileUploadController(@Value("${app.upload.dir:uploads}") String uploadDir,
                                ImageModerationService imageModerationService) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.imageModerationService = imageModerationService;
        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉터리를 생성할 수 없습니다.", e);
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                          HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "업로드할 파일이 없습니다."));
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetPath = this.uploadDirectory.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(this.uploadDirectory)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장에 실패했습니다.", e);
        }

        String fileUrl = "/uploads/" + storedFilename;
        HttpServletRequest requestToUse = Objects.requireNonNull(request, "request must not be null");
        String imageUrl = ServletUriComponentsBuilder.fromRequest(requestToUse)
                .replacePath(fileUrl)
                .build()
                .toUriString();

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            try {
                Boolean isInappropriate = imageModerationService.moderateImageAsync(imageUrl).join();
                if (Boolean.TRUE.equals(isInappropriate)) {
                    try {
                        Files.deleteIfExists(targetPath);
                    } catch (IOException deleteException) {
                        throw new IllegalStateException("업로드된 파일 삭제에 실패했습니다.", deleteException);
                    }
                    return ResponseEntity.badRequest().body(Map.of("message", "부적절한 이미지입니다."));
                }
            } catch (RuntimeException e) {
                try {
                    Files.deleteIfExists(targetPath);
                } catch (IOException deleteException) {
                    throw new IllegalStateException("업로드된 파일 삭제에 실패했습니다.", deleteException);
                }
                return ResponseEntity.badRequest().body(Map.of("message", "이미지 검증에 실패했습니다."));
            }
        }

        return ResponseEntity.ok(Map.of("url", fileUrl));
    }
}
