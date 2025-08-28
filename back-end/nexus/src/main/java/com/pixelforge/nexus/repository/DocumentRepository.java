// Document Repository
package com.pixelforge.nexus.repository;

import com.pixelforge.nexus.entity.Document;
import com.pixelforge.nexus.entity.Project;
import com.pixelforge.nexus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Document entity
 * Provides document-specific data access methods
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Find documents by project
     * @param project Project to filter by
     * @return List of documents for the project
     */
    List<Document> findByProject(Project project);

    /**
     * Find documents by project ordered by upload date
     * @param project Project to filter by
     * @return List of documents ordered by upload date (newest first)
     */
    @Query("SELECT d FROM Document d WHERE d.project = :project ORDER BY d.uploadedAt DESC")
    List<Document> findByProjectOrderByUploadedAtDesc(@Param("project") Project project);

    /**
     * Find documents uploaded by user
     * @param user User who uploaded
     * @return List of documents uploaded by user
     */
    List<Document> findByUploadedBy(User user);

    /**
     * Find document by filename and project
     * @param fileName File name
     * @param project Project
     * @return Optional Document
     */
    Optional<Document> findByFileNameAndProject(String fileName, Project project);

    /**
     * Count documents by project
     * @param project Project to count for
     * @return Number of documents in project
     */
    long countByProject(Project project);

    /**
     * Find documents by content type
     * @param contentType Content type to filter by
     * @return List of documents with specified content type
     */
    List<Document> findByContentType(String contentType);
}