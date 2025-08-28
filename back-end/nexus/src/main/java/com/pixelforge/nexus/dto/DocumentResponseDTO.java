package com.pixelforge.nexus.dto;

import java.time.LocalDateTime;

/**
 * DTO for document response data
 */
public class DocumentResponseDTO {

    private Long id;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String formattedFileSize;
    private String contentType;
    private String description;
    private LocalDateTime uploadedAt;
    private Long projectId;
    private String projectName;
    private UserResponseDTO uploadedBy;
    private String fileExtension;
    private boolean isImage;
    private boolean isPdf;
    private boolean isDocument;

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DocumentResponseDTO dto = new DocumentResponseDTO();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder fileName(String fileName) { dto.fileName = fileName; return this; }
        public Builder originalFileName(String originalFileName) {
            dto.originalFileName = originalFileName; return this;
        }
        public Builder fileSize(Long fileSize) { dto.fileSize = fileSize; return this; }
        public Builder formattedFileSize(String formattedFileSize) {
            dto.formattedFileSize = formattedFileSize; return this;
        }
        public Builder contentType(String contentType) { dto.contentType = contentType; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder uploadedAt(LocalDateTime uploadedAt) { dto.uploadedAt = uploadedAt; return this; }
        public Builder projectId(Long projectId) { dto.projectId = projectId; return this; }
        public Builder projectName(String projectName) { dto.projectName = projectName; return this; }
        public Builder uploadedBy(UserResponseDTO uploadedBy) { dto.uploadedBy = uploadedBy; return this; }
        public Builder fileExtension(String fileExtension) { dto.fileExtension = fileExtension; return this; }
        public Builder isImage(boolean isImage) { dto.isImage = isImage; return this; }
        public Builder isPdf(boolean isPdf) { dto.isPdf = isPdf; return this; }
        public Builder isDocument(boolean isDocument) { dto.isDocument = isDocument; return this; }

        public DocumentResponseDTO build() { return dto; }
    }

    // Getters
    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getOriginalFileName() { return originalFileName; }
    public Long getFileSize() { return fileSize; }
    public String getFormattedFileSize() { return formattedFileSize; }
    public String getContentType() { return contentType; }
    public String getDescription() { return description; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public Long getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public UserResponseDTO getUploadedBy() { return uploadedBy; }
    public String getFileExtension() { return fileExtension; }
    public boolean isImage() { return isImage; }
    public boolean isPdf() { return isPdf; }
    public boolean isDocument() { return isDocument; }
}
