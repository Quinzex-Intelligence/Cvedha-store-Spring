package com.quinzex.service;

import com.quinzex.dto.CartItemResponse;
import com.quinzex.dto.TotalCartValueResponse;

import java.util.List;

public interface IAddToCartService {
    String addToCart(Long bookId);

    void increaseQuantity(Long cartItemID);

    void decreaseQuantity(Long cartItemID);

    void removeFromCart(Long cartItemID);

    List<CartItemResponse> getCartItems();

    TotalCartValueResponse getTotalCartValueWithGst();
    void doEmptyCart();
}
