package com.pixelforge.nexus.controller;

import com.pixelforge.nexus.dto.DocumentResponseDTO;
import com.pixelforge.nexus.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Document management controller
 * Handles file upload, download, and management
 */
@RestController
@RequestMapping("/documents")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * Upload document to project
     */
    @PostMapping("/projects/{projectId}/upload")
    public ResponseEntity<DocumentResponseDTO> uploadDocument(@PathVariable Long projectId,
                                                              @RequestParam("file") MultipartFile file,
                                                              @RequestParam(value = "description", required = false) String description,
                                                              Authentication authentication) {
        String username = authentication.getName();
        DocumentResponseDTO document = documentService.uploadDocument(projectId, file, description, username);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    /**
     * Get documents for a project
     */
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<DocumentResponseDTO>> getProjectDocuments(@PathVariable Long projectId,
                                                                         Authentication authentication) {
        String username = authentication.getName();
        List<DocumentResponseDTO> documents = documentService.getProjectDocuments(projectId, username);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get document by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getDocumentById(@PathVariable Long id,
                                                               Authentication authentication) {
        String username = authentication.getName();
        DocumentResponseDTO document = documentService.getDocumentById(id, username);
        return ResponseEntity.ok(document);
    }

    /**
     * Download document file
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id,
                                                     Authentication authentication) {
        String username = authentication.getName();

        // Get document info first
        DocumentResponseDTO documentInfo = documentService.getDocumentById(id, username);

        // Get file resource
        Resource resource = documentService.downloadDocument(id, username);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documentInfo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentInfo.getOriginalFileName() + "\"")
                .body(resource);
    }

    /**
     * Delete document
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id,
                                               Authentication authentication) {
        String username = authentication.getName();
        documentService.deleteDocument(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * Get documents uploaded by current user
     */
    @GetMapping("/my-uploads")
    public ResponseEntity<List<DocumentResponseDTO>> getUserDocuments(Authentication authentication) {
        String username = authentication.getName();
        List<DocumentResponseDTO> documents = documentService.getUserDocuments(username);
        return ResponseEntity.ok(documents);
    }
}