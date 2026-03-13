package com.quinzex.controller;

import com.quinzex.entity.Gst;
import com.quinzex.service.IGstInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/gst")
@RequiredArgsConstructor
public class GstController {

    private final IGstInterface gstService;

    @PostMapping
    @PreAuthorize("hasAuthority('GST_PERMISSION')")
    public ResponseEntity<String> addGst(@RequestParam int percentage) {
        return ResponseEntity.ok(gstService.addGst(percentage));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GST_PERMISSION')")
    public ResponseEntity<String> editGst(@PathVariable Long id,
                                          @RequestParam int percentage) {
        return ResponseEntity.ok(gstService.editGst(id, percentage));
    }

    @GetMapping
    public ResponseEntity<List<Gst>> getAllGst() {
        return ResponseEntity.ok(gstService.findAll());
    }
}