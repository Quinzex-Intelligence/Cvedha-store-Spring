package com.quinzex.controller;

import com.quinzex.dto.BulkCreateEbookRequest;
import com.quinzex.dto.CreateEbookRequest;
import com.quinzex.entity.Ebooks;
import com.quinzex.service.IEbookAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/ebooks")
@RequiredArgsConstructor
public class EbookAdminController {

    private final IEbookAdminService ebookAdminService;

    @GetMapping("/inactive")
    public ResponseEntity<List<Ebooks>> getInactiveBooks(
            @RequestParam(required = false) Long cursor) {

        return ResponseEntity.ok(
                ebookAdminService.getInactiveEbooksList(cursor)
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadBooks(
            @ModelAttribute BulkCreateEbookRequest bulkCreateEbookRequest) throws IOException {

        return ResponseEntity.ok(
                ebookAdminService.createEbooks(bulkCreateEbookRequest.getBooks())
        );
    }

    @PutMapping("/deactivate")
    public ResponseEntity<String> deactivateBooks(
            @RequestBody List<Long> ids) {

        return ResponseEntity.ok(
                ebookAdminService.softDeleteEbooks(ids)
        );
    }

    @PutMapping("/activate")
    public ResponseEntity<String> activateBooks(
            @RequestBody List<Long> ids) {

        return ResponseEntity.ok(
                ebookAdminService.activateEbooks(ids)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> editBook(
            @PathVariable Long id,
            @ModelAttribute CreateEbookRequest request) throws IOException {

        return ResponseEntity.ok(
                ebookAdminService.editBook(id, request)
        );
    }
}