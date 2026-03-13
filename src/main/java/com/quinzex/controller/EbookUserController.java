package com.quinzex.controller;

import com.quinzex.dto.DownloadResponse;
import com.quinzex.dto.EbookUserResponse;
import com.quinzex.service.IdowloadServie;
import com.quinzex.service.IeBookUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class EbookUserController {

    private final IeBookUserService ebookUserService;
    private final IdowloadServie downloadServie;


    @GetMapping
    public ResponseEntity<List<EbookUserResponse>> getActiveBooks(
            @RequestParam(required = false) Long cursor) {

        return ResponseEntity.ok(
                ebookUserService.getActiveEbooksList(cursor)
        );
    }
    @GetMapping("/ebooks/search")
    public List<EbookUserResponse> searchBooks(
            @RequestParam String keyword,
            @RequestParam(required = false) Long cursor
    ){
        return ebookUserService.searchBooks(keyword, cursor);
    }


    @GetMapping("/language")
    public List<EbookUserResponse> getBooksByLanguage(
            @RequestParam String language,
            @RequestParam(required = false) Long cursor
    ) {
        return ebookUserService.getBooksByLanguage(language, cursor);
    }

    @GetMapping("/category")
    public List<EbookUserResponse> getBooksByCategory(
            @RequestParam String category,
            @RequestParam(required = false) Long cursor
    ) {
        return ebookUserService.getBooksByCategory(category, cursor);
    }

    @GetMapping("/download/{bookId}")
    public ResponseEntity<InputStreamResource> downloadBook(@PathVariable Long bookId) throws IOException {
       DownloadResponse downloadResponse = downloadServie.downloadBook(bookId);
       InputStreamResource resource = new InputStreamResource(downloadResponse.getStream());
        String safeName = URLEncoder.encode(downloadResponse.getFileName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,  "attachment; filename=\""+safeName+".pdf\"").contentType(MediaType.APPLICATION_PDF).body(resource);
    }
}