package com.aiknowledge.assistant.controller;

import com.aiknowledge.assistant.dto.document.DocumentResponse;
import com.aiknowledge.assistant.entity.User;
import com.aiknowledge.assistant.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Upload PDF/TXT documents and chat about their contents")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a PDF or TXT (max 5 MB); returns the document and its linked Q&A conversation")
    public ResponseEntity<DocumentResponse> upload(@AuthenticationPrincipal User user,
                                                   @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.upload(user, file));
    }

    @GetMapping
    @Operation(summary = "List the authenticated user's uploaded documents")
    public List<DocumentResponse> list(@AuthenticationPrincipal User user) {
        return documentService.list(user);
    }
}
