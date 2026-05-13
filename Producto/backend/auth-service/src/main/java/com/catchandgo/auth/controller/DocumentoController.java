package com.catchandgo.auth.controller;

import com.catchandgo.auth.dto.DocumentoDto;
import com.catchandgo.auth.service.DocumentoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/auth/documentos")
public class DocumentoController {

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDto> subir(
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("tipo") String tipo,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.guardar(usuarioId, tipo, file));
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<DocumentoDto> listar(@PathVariable("usuarioId") Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{usuarioId}/{filename}")
    public ResponseEntity<Resource> descargar(
            @PathVariable("usuarioId") String usuarioId,
            @PathVariable("filename") String filename) throws IOException {
        Resource resource = service.cargar(usuarioId + "/" + filename);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
            .body(resource);
    }
}
