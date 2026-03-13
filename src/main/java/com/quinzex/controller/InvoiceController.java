package com.quinzex.controller;

import com.quinzex.dto.InvoiceResponse;
import com.quinzex.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long orderId) {

        InvoiceResponse invoice = invoiceService.getInvoice(orderId);

        return ResponseEntity.ok(invoice);
    }
}