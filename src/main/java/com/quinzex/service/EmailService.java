package com.quinzex.service;

import com.quinzex.entity.OrderItems;
import com.quinzex.repository.EbookRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final EbookRepo ebookRepo;

    @Async
    @Override
    public void sendOrderSuccessEmail(String userEmail,
                                      Long orderId,
                                      Double amount,
                                      List<OrderItems> items) {

        try {

            StringBuilder booksHtml = new StringBuilder();

            for (OrderItems item : items) {
                String bookName = ebookRepo.findById(item.getBookId())
                        .orElseThrow()
                        .getBookName();

                booksHtml.append("""
                        <li style="font-size:15px;">%s</li>
                        """.formatted(bookName));
            }

            String html = """
                    <h2>Purchase Successful 🎉</h2>

                    <p>Your order in <b>Career Vedha</b> was successful.</p>

                    <p><b>Order ID:</b> %s</p>
                    <p><b>Amount Paid:</b> ₹%.2f</p>

                    <h3>Books Purchased</h3>
                    <ul>
                    %s
                    </ul>

                    <p>You can download your books anytime from the <b>My Books</b> section after logging into your account.</p>

                    <p>Thank you for your purchase.</p>
                    """.formatted(orderId, amount, booksHtml);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(userEmail);
            helper.setSubject("Career Vedha – Purchase Successful");
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send purchase email");
        }
    }
}
