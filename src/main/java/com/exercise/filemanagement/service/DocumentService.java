package com.exercise.filemanagement.service;

import com.exercise.filemanagement.dtos.DocumentDTO;
import com.exercise.filemanagement.model.Document;
import com.exercise.filemanagement.repository.DocumentRepository;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class DocumentService {

    @Autowired
    private final DocumentRepository documentRepository;
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentDTO uploadFile(
            MultipartFile multipartFile,
            String name,
            String description) throws IOException {

        if (multipartFile.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file.");
        }
        if (multipartFile.getSize() > 10485760) {  //10MB  in bytes
            throw new RuntimeException("File size exceeds 10MB limit");
        }
        String contentType = multipartFile.getContentType();
        if (!isValidContentType(contentType)) {
            throw new RuntimeException("Only PDF, PNG, and JPEG files are allowed");
        }

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path targetDir = fileStorageLocation.resolve(datePath);
        Files.createDirectories(targetDir);
        Path targetPath = targetDir.resolve(fileName);

        Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document();
        document.setName(name);
        document.setDescription(description);
        document.setFilePath(targetPath.toString());
        document.setContentType(contentType);
        document.setSize(multipartFile.getSize());
        document.setUploadDate(LocalDateTime.now());
        documentRepository.save(document);

        return convertToDto(document);
    }

    private boolean isValidContentType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpeg")
        );
    }

    public List<DocumentDTO> listDocuments() {
        List<Document> documents = documentRepository.findAll();
        return documents.stream()
                .map(this::convertToDto)
                .toList();
    }

    public void deleteDocument(Long id) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Document not found."));

        Path filePath = Paths.get(document.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        documentRepository.delete(document);
    }

    public DocumentDTO convertToDto(Document document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .name(document.getName())
                .description(document.getDescription())
                .filePath(document.getFilePath())
                .contentType(document.getContentType())
                .size(document.getSize())
                .uploadDate(document.getUploadDate())
                .build();
    }

    public Resource downloadFile(Long id) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Document not found with ID: " + id));
        Path filePath = Paths.get(fileStorageLocation.toUri())
                .resolve(document.getFilePath())
                .normalize();

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found on disk: " + filePath);
        }

        return new UrlResource(filePath.toUri());
    }

    public Document getDocumentMetadata(Long id) throws FileNotFoundException {
        return documentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Document not found with ID: " + id));
    }
}