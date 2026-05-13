package com.catchandgo.auth.service;

import com.catchandgo.auth.dto.DocumentoDto;
import com.catchandgo.auth.entity.DocumentoUsuario;
import com.catchandgo.auth.repository.DocumentoUsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentoService {

    @Value("${app.docs.storage-path:./uploads/documentos}")
    private String storagePath;

    private final DocumentoUsuarioRepository repository;

    public DocumentoService(DocumentoUsuarioRepository repository) {
        this.repository = repository;
    }

    public DocumentoDto guardar(Long usuarioId, String tipo, MultipartFile file) throws IOException {
        Path dirPath = Paths.get(storagePath, String.valueOf(usuarioId));
        Files.createDirectories(dirPath);

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path filePath = dirPath.resolve(filename);
        file.transferTo(filePath.toFile());

        DocumentoUsuario doc = new DocumentoUsuario();
        doc.setUsuarioId(usuarioId);
        doc.setTipo(tipo);
        doc.setFilename(usuarioId + "/" + filename);
        DocumentoUsuario saved = repository.save(doc);

        return toDto(saved);
    }

    public List<DocumentoDto> listar(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public Resource cargar(String relativePath) throws MalformedURLException {
        Path file = Paths.get(storagePath).resolve(relativePath);
        return new UrlResource(file.toUri());
    }

    private DocumentoDto toDto(DocumentoUsuario doc) {
        return new DocumentoDto(
            doc.getId(), doc.getTipo(), doc.getFilename(),
            "/auth/documentos/" + doc.getFilename(),
            doc.getFechaSubida()
        );
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".bin";
        return filename.substring(filename.lastIndexOf("."));
    }
}
