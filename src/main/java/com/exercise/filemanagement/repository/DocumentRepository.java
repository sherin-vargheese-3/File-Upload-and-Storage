package com.exercise.filemanagement.repository;

import com.exercise.filemanagement.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
