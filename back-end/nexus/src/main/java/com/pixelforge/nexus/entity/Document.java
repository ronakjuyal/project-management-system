package com.pixelforge.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Document entity for managing project-related files
 * Stores file metadata and relationships to projects and users
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "File name is required")
    @Size(min = 1, max = 255, message = "File name must be between 1 and 255 characters")
    @Column(nullable = false)
    private String fileName;

    @NotBlank(message = "Original file name is required")
    @Column(nullable = false)
    private String originalFileName;

    @NotBlank(message = "File path is required")
    @Column(nullable = false)
    private String filePath;

    @NotNull(message = "File size is required")
    @Column(nullable = false)
    private Long fileSize;

    @NotBlank(message = "Content type is required")
    @Column(nullable = false)
    private String contentType;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    // Constructors
    public Document() {}

    public Document(String fileName, String originalFileName, String filePath,
                    Long fileSize, String contentType, Project project, User uploadedBy) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.project = project;
        this.uploadedBy = uploadedBy;
    }

    // Business methods
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    public String getFileExtension() {
        int lastDot = originalFileName.lastIndexOf('.');
        return lastDot > 0 ? originalFileName.substring(lastDot + 1) : "";
    }

    public boolean isImageFile() {
        String contentTypeLower = contentType.toLowerCase();
        return contentTypeLower.startsWith("image/");
    }

    public boolean isPdfFile() {
        return "application/pdf".equalsIgnoreCase(contentType);
    }

    public boolean isDocumentFile() {
        String contentTypeLower = contentType.toLowerCase();
        return contentTypeLower.contains("document") ||
                contentTypeLower.contains("word") ||
                contentTypeLower.contains("text") ||
                isPdfFile();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
}