package com.quinzex.service;
import com.quinzex.dto.InventoryItemDto;
import com.quinzex.dto.InventoryReserveEvent;
import com.quinzex.dto.PaymentVerificationRequest;
import com.quinzex.entity.OrderItems;
import com.quinzex.entity.Orders;
import com.quinzex.kafka.InventoryProducer;
import com.quinzex.repository.OrderItemRepo;
import com.quinzex.repository.OrderRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService{

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    private final InventoryProducer  inventoryProducer;

    private final OrderItemRepo orderItemRepo;

    private final OrderRepo  orderRepo;

    private final IAddToCartService    addToCartService;
    private final IEmailService emailService;

    @Override
    public String createRazorPayOrder(Long orderId) throws Exception {
       Orders order = orderRepo.findById(orderId).orElseThrow();
        if(!"INVENTORY_RESERVED".equals(order.getStatus()) && !"FAILED".equals(order.getStatus())){
            throw new RuntimeException("Order not ready for payment");
        }
        if(order.getPaymentExpiryTime().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Payment time expired");
        }

        if(order.getRazorpayOrderId() != null){
            return   order.getRazorpayOrderId();
        }
        if(order.getTotalAmount() == null || order.getTotalAmount() <= 0){
            throw new RuntimeException("Invalid order amount");
        }
        RazorpayClient client = new RazorpayClient(razorpayKey,razorpaySecret);
        JSONObject options = new JSONObject();
        options.put("amount", Math.round(order.getTotalAmount() * 100));
        options.put("currency", "INR");
        options.put("receipt", "order_" + order.getId() + "_" + System.currentTimeMillis());
        Order razorpayOrder = client.orders.create(options);

        order.setRazorpayOrderId(razorpayOrder.get("id"));
        orderRepo.save(order);

        return razorpayOrder.get("id").toString();
    }

public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId,String razorpaySignature)  {
try{
String payload = razorpayOrderId + "|" + razorpayPaymentId;
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec key = new SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256");
    mac.init(key);
    byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    String generatedSignature = Hex.encodeHexString(hash);
    return MessageDigest.isEqual(generatedSignature.getBytes(StandardCharsets.UTF_8),razorpaySignature.getBytes(StandardCharsets.UTF_8));
}catch(Exception e){
      throw new RuntimeException("Payment verification failed");
}
}
@Override
@Transactional
public void verifyPayment(PaymentVerificationRequest request){

        boolean valid = verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());
        if(!valid){
            throw new RuntimeException("Invalid payment signature");
        }
        Orders order = orderRepo.findByRazorpayOrderId(request.getRazorpayOrderId()).orElseThrow();

    if("PAID".equals(order.getStatus())){
        return;
    }
    if (!"INVENTORY_RESERVED".equals(order.getStatus())) {
        throw new RuntimeException("Invalid order state for payment");
    }

        order.setRazorpayPaymentId(request.getRazorpayPaymentId());
        order.setStatus("PAID");
        orderRepo.save(order);
    addToCartService.doEmptyCart();
    List<OrderItems> items = orderItemRepo.findByOrder_Id(order.getId());
    emailService.sendOrderSuccessEmail(order.getUserEmail(), order.getId(), order.getTotalAmount(), items);

}
@Override
@Transactional
public void processWebhook(String payload,String signature){
        boolean valid = verifyWebhookSignature(payload,signature);

    if(!valid){
        throw new RuntimeException("Invalid webhook signature");
    }
    JSONObject json = new JSONObject(payload);
    String event = json.getString("event");

    switch (event){
        case "payment.captured": {
            JSONObject payment = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            if(!payment.has("order_id")){
                return;
            }
            String razorpayOrderId = payment.getString("order_id");
            Orders order = orderRepo.findByRazorpayOrderId(razorpayOrderId).orElseThrow();
            if ("PAID".equals(order.getStatus())) {
                return;
            }
            if (!"INVENTORY_RESERVED".equals(order.getStatus())) {
                throw new RuntimeException("Invalid order state for payment");
            }
            order.setRazorpayPaymentId(payment.getString("id"));
            order.setStatus("PAID");
            orderRepo.save(order);
            addToCartService.doEmptyCart();
            List<OrderItems> items = orderItemRepo.findByOrder_Id(order.getId());
            emailService.sendOrderSuccessEmail(order.getUserEmail(), order.getId(), order.getTotalAmount(), items);

            break;
        }
        case "payment.failed": {
            JSONObject payment = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            if(!payment.has("order_id")){
                return;
            }
            String razorpayOrderId = payment.getString("order_id");
            String razorpayPaymentId = payment.getString("id");
            Orders order = orderRepo.findByRazorpayOrderId(razorpayOrderId).orElseThrow();
            if ("FAILED".equals(order.getStatus())) {
                return;
            }

            if(order.getRazorpayPaymentId()==null){
                order.setRazorpayPaymentId(razorpayPaymentId);
            }
            order.setStatus("FAILED");
            orderRepo.save(order);

            break;
        }
        default:
            // Ignore other events
            break;
    }

}
public boolean verifyWebhookSignature(String payload,String signature){
        try{
          Mac mac = Mac.getInstance("HmacSHA256");
          SecretKeySpec key = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
          mac.init(key);
          byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
          String generatedSignature = Hex.encodeHexString(hash);
          return MessageDigest.isEqual(generatedSignature.getBytes(StandardCharsets.UTF_8),signature.getBytes(StandardCharsets.UTF_8));
        }catch(Exception e){
            throw new RuntimeException("Webhook verification failed");
        }
}
@Transactional
@Override
    public Long retryPayment(Long oldOrderId){
        Orders oldOrder = orderRepo.findById(oldOrderId).orElseThrow();
    if(!"FAILED".equals(oldOrder.getStatus()) && !"INVENTORY_RESERVED".equals(oldOrder.getStatus())){
        throw new RuntimeException("Order cannot be retried");
    }
    if(oldOrder.getPaymentExpiryTime() == null){
        throw new RuntimeException("Invalid order state");
    }
        if(oldOrder.getPaymentExpiryTime().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Payment time expired");
        }
        Orders newOrder = new Orders();
        newOrder.setUserEmail(oldOrder.getUserEmail());
        newOrder.setStatus("CREATED");
        newOrder.setSubTotal(oldOrder.getSubTotal());
        newOrder.setTotalAmount(oldOrder.getTotalAmount());
        newOrder.setCreatedAt(LocalDateTime.now());
        newOrder.setPaymentExpiryTime(LocalDateTime.now().plusMinutes(15));
        oldOrder.setStatus("REPLACED");
        orderRepo.save(oldOrder);
        orderRepo.save(newOrder);

        List<OrderItems> newItems = oldOrder.getOrderItems().stream()
                .map(item->{
                    OrderItems items = new OrderItems();
                    items.setOrder(newOrder);
                    items.setBookId(item.getBookId());
                    items.setQuantity(item.getQuantity());
                    items.setPrice(item.getPrice());
                    return items;
                }).toList();
        orderItemRepo.saveAll(newItems);
        InventoryReserveEvent inventoryReserveEvent = new InventoryReserveEvent();
        inventoryReserveEvent.setOrderId(newOrder.getId());
        inventoryReserveEvent.setItems(newItems.stream().map(item -> new InventoryItemDto(item.getBookId(), item.getQuantity())).toList());
        inventoryProducer.sendReserveEvent(inventoryReserveEvent);
        return newOrder.getId();
}
}
