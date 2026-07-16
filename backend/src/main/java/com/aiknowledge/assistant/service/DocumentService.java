package com.aiknowledge.assistant.service;

import com.aiknowledge.assistant.config.AppProperties;
import com.aiknowledge.assistant.dto.document.DocumentResponse;
import com.aiknowledge.assistant.entity.Conversation;
import com.aiknowledge.assistant.entity.ConversationType;
import com.aiknowledge.assistant.entity.Document;
import com.aiknowledge.assistant.entity.User;
import com.aiknowledge.assistant.exception.BadRequestException;
import com.aiknowledge.assistant.repository.ConversationRepository;
import com.aiknowledge.assistant.repository.DocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final ConversationRepository conversationRepository;
    private final AppProperties.Upload uploadProperties;

    public DocumentService(DocumentRepository documentRepository,
                           ConversationRepository conversationRepository,
                           AppProperties properties) {
        this.documentRepository = documentRepository;
        this.conversationRepository = conversationRepository;
        this.uploadProperties = properties.upload();
    }

    @Transactional
    public DocumentResponse upload(User user, MultipartFile file) {
        validate(file);
        String filename = sanitizeFilename(file.getOriginalFilename());
        String extractedText = extractText(file, filename);
        if (extractedText.isBlank()) {
            throw new BadRequestException(
                    "No readable text could be extracted from the file. Scanned/image-only PDFs are not supported.");
        }

        Document document = documentRepository.save(new Document(
                user, filename, resolveContentType(filename), file.getSize(), extractedText));
        Conversation conversation = conversationRepository.save(new Conversation(
                user, "Document: " + filename, ConversationType.DOCUMENT, document));

        log.info("User id={} uploaded document id={} ({} chars extracted)",
                user.getId(), document.getId(), extractedText.length());
        return DocumentResponse.from(document, conversation.getId());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(User user) {
        Map<Long, Long> conversationIdByDocumentId = conversationRepository
                .findAllByUserIdAndType(user.getId(), ConversationType.DOCUMENT).stream()
                .filter(conversation -> conversation.getDocument() != null)
                .collect(Collectors.toMap(
                        conversation -> conversation.getDocument().getId(),
                        Conversation::getId,
                        (first, second) -> first));
        return documentRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(document -> DocumentResponse.from(document, conversationIdByDocumentId.get(document.getId())))
                .toList();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A non-empty file is required");
        }
        if (file.getSize() > uploadProperties.maxFileSizeBytes()) {
            throw new BadRequestException("File exceeds the maximum allowed size of 5 MB");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !(hasExtension(filename, ".pdf") || hasExtension(filename, ".txt"))) {
            throw new BadRequestException("Only PDF and TXT files are supported");
        }
    }

    private String extractText(MultipartFile file, String filename) {
        try {
            if (hasExtension(filename, ".pdf")) {
                try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
                    return new PDFTextStripper().getText(pdf).strip();
                }
            }
            return new String(file.getBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException ex) {
            throw new BadRequestException("The file could not be read. Please upload a valid PDF or TXT file.");
        }
    }

    private boolean hasExtension(String filename, String extension) {
        return filename.toLowerCase(Locale.ROOT).endsWith(extension);
    }

    private String resolveContentType(String filename) {
        return hasExtension(filename, ".pdf") ? "application/pdf" : "text/plain";
    }

    private String sanitizeFilename(String original) {
        String name = original == null ? "document" : original.replaceAll("[\\\\/:*?\"<>|]", "_").strip();
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }
}
