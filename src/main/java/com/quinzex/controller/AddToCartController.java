package com.quinzex.controller;

import com.quinzex.dto.CartItemResponse;
import com.quinzex.dto.TotalCartValueResponse;
import com.quinzex.service.IAddToCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class AddToCartController {

    private final IAddToCartService addToCartService;

    @PostMapping("/{bookId}")
    public ResponseEntity<String> addToCart(@PathVariable Long bookId) {
        return ResponseEntity.ok(addToCartService.addToCart(bookId));
    }

    @PutMapping("/increase/{cartItemId}")
    public ResponseEntity<String> increaseQuantity(@PathVariable Long cartItemId) {
        addToCartService.increaseQuantity(cartItemId);
        return ResponseEntity.ok("Quantity increased");
    }

    @PutMapping("/decrease/{cartItemId}")
    public ResponseEntity<String> decreaseQuantity(@PathVariable Long cartItemId) {
        addToCartService.decreaseQuantity(cartItemId);
        return ResponseEntity.ok("Quantity decreased");
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long cartItemId) {
        addToCartService.removeFromCart(cartItemId);
        return ResponseEntity.ok("Item removed from cart");
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartItems() {
        return ResponseEntity.ok(addToCartService.getCartItems());
    }

    @GetMapping("/total")
    public ResponseEntity<TotalCartValueResponse> getTotalCartValue() {
        return ResponseEntity.ok(addToCartService.getTotalCartValueWithGst());
    }
}