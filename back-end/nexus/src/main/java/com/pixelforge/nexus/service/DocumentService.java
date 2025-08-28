package com.pixelforge.nexus.service;

import com.pixelforge.nexus.entity.Document;
import com.pixelforge.nexus.entity.Project;
import com.pixelforge.nexus.entity.User;
import com.pixelforge.nexus.entity.Role;
import com.pixelforge.nexus.repository.DocumentRepository;
import com.pixelforge.nexus.dto.DocumentResponseDTO;
import com.pixelforge.nexus.dto.UserResponseDTO;
import com.pixelforge.nexus.exception.ResourceNotFoundException;
import com.pixelforge.nexus.exception.InvalidOperationException;
import com.pixelforge.nexus.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for Document management operations
 * Handles file upload, storage, and retrieval with security checks
 */
@Service
@Transactional
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final Path fileStorageLocation;

    public DocumentService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Upload document to project
     * @param projectId Project ID
     * @param file Multipart file
     * @param description Optional description
     * @param uploaderUsername Username of uploader
     * @return Document response
     */
    public DocumentResponseDTO uploadDocument(Long projectId, MultipartFile file, String description,
                                              String uploaderUsername) {
        // Validate file
        if (file.isEmpty()) {
            throw new InvalidOperationException("Cannot upload empty file");
        }

        // Get project and user
        Project project = projectService.getProjectEntityById(projectId);
        User uploader = userService.getUserEntityByUsername(uploaderUsername);

        // Check if user can upload to this project
        if (!canUploadToProject(project, uploader)) {
            throw new InvalidOperationException("You don't have permission to upload files to this project");
        }

        // Validate file type and size
        validateFile(file);

        // Generate unique filename
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Check if filename contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Create document entity
            Document document = new Document();
            document.setFileName(fileName);
            document.setOriginalFileName(originalFileName);
            document.setFilePath(targetLocation.toString());
            document.setFileSize(file.getSize());
            document.setContentType(file.getContentType());
            document.setDescription(description);
            document.setProject(project);
            document.setUploadedBy(uploader);

            Document savedDocument = documentRepository.save(document);
            return convertToResponseDTO(savedDocument);

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    /**
     * Get documents for a project
     * @param projectId Project ID
     * @param currentUsername Current user's username
     * @return List of document responses
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getProjectDocuments(Long projectId, String currentUsername) {
        Project project = projectService.getProjectEntityById(projectId);
        User currentUser = userService.getUserEntityByUsername(currentUsername);

        // Check if user has access to this project
        if (!hasAccessToProject(project, currentUser)) {
            throw new InvalidOperationException("You don't have access to this project's documents");
        }

        return documentRepository.findByProjectOrderByUploadedAtDesc(project)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get document by ID
     * @param documentId Document ID
     * @param currentUsername Current user's username
     * @return Document response
     */
    @Transactional(readOnly = true)
    public DocumentResponseDTO getDocumentById(Long documentId, String currentUsername) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User currentUser = userService.getUserEntityByUsername(currentUsername);

        // Check if user has access to this document's project
        if (!hasAccessToProject(document.getProject(), currentUser)) {
            throw new InvalidOperationException("You don't have access to this document");
        }

        return convertToResponseDTO(document);
    }

    /**
     * Download document file
     * @param documentId Document ID
     * @param currentUsername Current user's username
     * @return File resource
     */
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId, String currentUsername) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User currentUser = userService.getUserEntityByUsername(currentUsername);

        // Check if user has access to this document's project
        if (!hasAccessToProject(document.getProject(), currentUser)) {
            throw new InvalidOperationException("You don't have access to this document");
        }

        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + document.getOriginalFileName());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + document.getOriginalFileName());
        }
    }

    /**
     * Delete document
     * @param documentId Document ID
     * @param currentUsername Current user's username
     */
    public void deleteDocument(Long documentId, String currentUsername) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User currentUser = userService.getUserEntityByUsername(currentUsername);

        // Check if user can delete this document
        if (!canDeleteDocument(document, currentUser)) {
            throw new InvalidOperationException("You don't have permission to delete this document");
        }

        try {
            // Delete physical file
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete database record
            documentRepository.delete(document);

        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + document.getOriginalFileName(), ex);
        }
    }

    /**
     * Get documents uploaded by user
     * @param currentUsername Current user's username
     * @return List of document responses
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getUserDocuments(String currentUsername) {
        User currentUser = userService.getUserEntityByUsername(currentUsername);

        return documentRepository.findByUploadedBy(currentUser)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validate uploaded file
     * @param file Multipart file
     */
    private void validateFile(MultipartFile file) {
        // Check file size (10MB limit)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new InvalidOperationException("File size exceeds maximum limit of 10MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new InvalidOperationException("Invalid file type");
        }

        // Allow common document types
        List<String> allowedTypes = List.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain",
                "image/jpeg",
                "image/png",
                "image/gif",
                "image/bmp",
                "image/webp"
        );

        if (!allowedTypes.contains(contentType.toLowerCase())) {
            throw new InvalidOperationException("File type not allowed: " + contentType);
        }
    }

    /**
     * Get file extension from filename
     * @param filename Filename
     * @return File extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    /**
     * Check if user can upload to project
     * @param project Project
     * @param user User
     * @return true if user can upload
     */
    private boolean canUploadToProject(Project project, User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.PROJECT_LEAD) {
            return project.getLead() != null && project.getLead().getId().equals(user.getId());
        }
        return false;
    }

    /**
     * Check if user has access to project
     * @param project Project
     * @param user User
     * @return true if user has access
     */
    private boolean hasAccessToProject(Project project, User user) {
        switch (user.getRole()) {
            case ADMIN:
                return true;
            case PROJECT_LEAD:
                return project.getLead() != null && project.getLead().getId().equals(user.getId());
            case DEVELOPER:
                return project.getAssignedDevelopers() != null &&
                        project.getAssignedDevelopers().stream()
                                .anyMatch(dev -> dev.getId().equals(user.getId()));
            default:
                return false;
        }
    }

    /**
     * Check if user can delete document
     * @param document Document
     * @param user User
     * @return true if user can delete
     */
    private boolean canDeleteDocument(Document document, User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        // Users can delete their own documents
        if (document.getUploadedBy().getId().equals(user.getId())) {
            return true;
        }
        // Project leads can delete documents in their projects
        if (user.getRole() == Role.PROJECT_LEAD) {
            Project project = document.getProject();
            return project.getLead() != null && project.getLead().getId().equals(user.getId());
        }
        return false;
    }

    /**
     * Convert Document entity to DocumentResponseDTO
     * @param document Document entity
     * @return DocumentResponseDTO
     */
    private DocumentResponseDTO convertToResponseDTO(Document document) {
        User uploader = document.getUploadedBy();
        UserResponseDTO uploaderDTO = UserResponseDTO.builder()
                .id(uploader.getId())
                .username(uploader.getUsername())
                .firstName(uploader.getFirstName())
                .lastName(uploader.getLastName())
                .role(uploader.getRole())
                .build();

        return DocumentResponseDTO.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .originalFileName(document.getOriginalFileName())
                .fileSize(document.getFileSize())
                .formattedFileSize(document.getFormattedFileSize())
                .contentType(document.getContentType())
                .description(document.getDescription())
                .uploadedAt(document.getUploadedAt())
                .projectId(document.getProject().getId())
                .projectName(document.getProject().getName())
                .uploadedBy(uploaderDTO)
                .fileExtension(document.getFileExtension())
                .isImage(document.isImageFile())
                .isPdf(document.isPdfFile())
                .isDocument(document.isDocumentFile())
                .build();
    }
}