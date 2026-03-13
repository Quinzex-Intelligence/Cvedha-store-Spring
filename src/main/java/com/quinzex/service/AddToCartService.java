package com.quinzex.service;

import com.quinzex.dto.CartItemResponse;
import com.quinzex.dto.TotalCartValueResponse;
import com.quinzex.entity.CartItem;
import com.quinzex.entity.Ebooks;
import com.quinzex.entity.Gst;
import com.quinzex.repository.AddToCartRepo;
import com.quinzex.repository.EbookRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class AddToCartService implements IAddToCartService{

    private final EbookRepo  ebookRepo;
    private final AddToCartRepo addToCartRepo;
    private final S3Service s3Service;
    private final GstService gstService;
    @Override
    @Transactional
    public String addToCart(Long bookId) {

        String userEmail = getCurrentUserEmail();
        Ebooks ebook = ebookRepo.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if(!ebook.getActive()){
            throw new RuntimeException("Book is inactive");
        }

        CartItem existingItem = addToCartRepo.findByUserEmailAndEbook_BookIdAndActiveTrue(userEmail,bookId).orElse(null);
        int availableStock = ebook.getAvailableQuantity();
        if(existingItem!=null){

            int newQuantity = existingItem.getQuantity()+1;
            if(newQuantity>availableStock){
                throw new RuntimeException("Cannot add more than available stock");
            }
            if (newQuantity > 5) {
                throw new RuntimeException("Maximum 5 items allowed");
            }
            existingItem.setQuantity(newQuantity);

            return "Cart quantity increased";

        }
        else{
            long totalItems = addToCartRepo.countByUserEmailAndActiveTrue(userEmail);
            if(totalItems >=10){
                throw new RuntimeException("Maximum 10 items allowed in cart");
            }
            if (availableStock < 1) {
                throw new RuntimeException("Book out of stock");
            }
            CartItem cartItem = new CartItem();
            cartItem.setUserEmail(userEmail);
            cartItem.setEbook(ebook);
            cartItem.setQuantity(1);

            addToCartRepo.save(cartItem);

            return "Product added to cart";
        }
    }

    @Transactional
    @Override
    public void increaseQuantity(Long cartItemID){
        String userEmail =getCurrentUserEmail();

      CartItem cartItem = addToCartRepo.findById(cartItemID).orElseThrow(() -> new IllegalArgumentException("Item not found"));
      if(!userEmail.equals(cartItem.getUserEmail())){
          throw new RuntimeException("Suspicious Activity found");
      }
      Ebooks ebook = cartItem.getEbook();
        if (!ebook.getActive()) {
            throw new RuntimeException("Book is inactive");
        }
      int newQuantity = cartItem.getQuantity()+1;
      if(newQuantity>ebook.getAvailableQuantity()){
          throw new RuntimeException("Cannot add more than available stock");
      }
      if(newQuantity>5){
          throw new RuntimeException("Maximum 5 items allowed");
      }
      cartItem.setQuantity(newQuantity);
    }
    @Transactional
    @Override
    public void decreaseQuantity(Long cartItemID){

        String userEmail = getCurrentUserEmail();
        CartItem cartItem = addToCartRepo.findById(cartItemID).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if(!userEmail.equals(cartItem.getUserEmail())){
            throw new RuntimeException("Suspicious Activity found");
        }
        Ebooks ebook = cartItem.getEbook();
        if (!ebook.getActive()) {
            throw new RuntimeException("Book is inactive");
        }
        int currentQuantity = cartItem.getQuantity();
        if(currentQuantity<=1){
            throw new RuntimeException("Minimum quantity is 1");
        }
        cartItem.setQuantity(currentQuantity-1);
    }

    @Transactional
    @Override
    public void removeFromCart(Long cartItemID){

        String userEmail = getCurrentUserEmail();
        CartItem cartItem = addToCartRepo.findById(cartItemID).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if(!userEmail.equals(cartItem.getUserEmail())){
            throw new RuntimeException("Suspicious Activity found");
        }
        addToCartRepo.delete(cartItem);
    }
    @Transactional(readOnly = true)
    @Override
    public List<CartItemResponse> getCartItems(){

        String userEmail = getCurrentUserEmail();
        List<CartItemResponse> responses = new ArrayList<>();
        List<CartItem> cartItems = addToCartRepo.findActiveCartWithBook(userEmail);

        for(CartItem cartItem : cartItems){
            CartItemResponse response  = new CartItemResponse();
            Ebooks ebook = cartItem.getEbook();

            response.setCartItemId(cartItem.getId());
            response.setBookId(ebook.getBookId());
            response.setBookName(ebook.getBookName());
            if (ebook.getCoverPhoto() != null && !ebook.getCoverPhoto().isEmpty()) {
                response.setCoverPhotoUrl(s3Service.generatePresignedUrl(ebook.getCoverPhoto()));
            }

            response.setPrice(ebook.getBookPrice());
            response.setQuantity(cartItem.getQuantity());
            response.setLanguageCategory(ebook.getLanguageCategory());
            response.setSubTotal(ebook.getBookPrice() * cartItem.getQuantity());

            responses.add(response);
        }
        return responses;
    }
    @Transactional(readOnly = true)
    @Override
    public TotalCartValueResponse getTotalCartValueWithGst(){

        String userEmail =getCurrentUserEmail();
        List<CartItem> cartItems = addToCartRepo.findActiveCartWithBook(userEmail);
        List<Gst> gstList = gstService.findAll();
        double gstPercentage = gstList.isEmpty() ? 0 : gstList.get(0).getGstPercentage();
        TotalCartValueResponse cartValueResponse = new TotalCartValueResponse();

      double subTotal =   cartItems.stream().mapToDouble(item->item.getQuantity()*item.getEbook().getBookPrice()).sum();
        double gstAmount = (subTotal * gstPercentage) / 100;
        double totalAmount = subTotal + gstAmount;
        cartValueResponse.setGstPercentage(gstPercentage);
        cartValueResponse.setTotalAmount(totalAmount);
        cartValueResponse.setGstAmount(gstAmount);
        cartValueResponse.setSubTotal(subTotal);
       return cartValueResponse;
    }
    @Override
    public void doEmptyCart(){
        String userEmail =getCurrentUserEmail();
        List<CartItem> cartItems = addToCartRepo.findActiveCartWithBook(userEmail);

        addToCartRepo.deleteAll(cartItems);
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
