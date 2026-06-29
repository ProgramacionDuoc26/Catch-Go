package com.catchandgo.profile.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    @Value("${file.upload-dir:/app/uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false, defaultValue = "anonymous") String userId
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
            }

            // Crear directorio si no existe
            Path dir = Paths.get(uploadDir, userId);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // Generar nombre único
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + ext;

            // Guardar archivo
            Path filePath = dir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Construir URL pública
            String fileUrl = "/files/view/" + userId + "/" + fileName;

            return ResponseEntity.ok(Map.of("url", fileUrl, "fileName", originalName != null ? originalName : fileName));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al guardar archivo: " + e.getMessage()));
        }
    }

    @GetMapping("/view/{userId}/{fileName}")
    public ResponseEntity<byte[]> viewFile(
            @PathVariable("userId") String userId,
            @PathVariable("fileName") String fileName
    ) {
        try {
            Path filePath = Paths.get(uploadDir, userId, fileName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] content = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
