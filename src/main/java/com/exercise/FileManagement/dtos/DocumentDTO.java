package com.exercise.FileManagement.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentDTO {
    private Long id;
    private String name;
    private String description;
    private String filePath;
    private String contentType;
    private Long size;
    private LocalDateTime uploadDate;
}
