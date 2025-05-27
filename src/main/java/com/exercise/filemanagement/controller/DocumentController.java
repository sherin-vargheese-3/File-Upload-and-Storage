package com.exercise.filemanagement.controller;

import com.exercise.filemanagement.dtos.DocumentDTO;
import com.exercise.filemanagement.model.Document;
import com.exercise.filemanagement.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public DocumentDTO uploadFile(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam String name,
            @RequestParam String description) throws IOException {
        return documentService.uploadFile(multipartFile, name, description);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<DocumentDTO> listDocuments() {
        return documentService.listDocuments();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) throws IOException {
        documentService.deleteDocument(id);
        return ResponseEntity.ok("Document with ID " + id + " has been deleted.");
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        Resource resource = documentService.downloadFile(id);
        Document doc = documentService.getDocumentMetadata(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(doc.getSize()))
                .body(resource);
    }
}