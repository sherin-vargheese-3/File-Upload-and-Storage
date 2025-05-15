package com.exercise.FileManagement.repository;

import com.exercise.FileManagement.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
