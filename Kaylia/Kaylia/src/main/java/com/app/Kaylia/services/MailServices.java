package com.app.Kaylia.services;

import com.app.Kaylia.model.ConfirmOrder;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.UserRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@Service
public class MailServices {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SpringTemplateEngine templateEngine;


    //for order confirm email
    @Async
    public void sendOrderConfirmationEmail(ConfirmOrder order, String email) {
        try {
            String body = buildHtmlEmail(order, email);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Your Order #KAY-2025-" + order.getId() + " is Confirmed!");
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("Order confirmation email sent to " + email);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //to load email for confirm order
    private String buildHtmlEmail(ConfirmOrder order, String email) {
        User user = userRepo.findByEmail(email);

        // Prepare Thymeleaf context
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("orderId", order.getId());
        context.setVariable("orderDate", order.getTimeOrdered().toLocalDate().toString());
        context.setVariable("totalAmount", order.getTotalAmount());
        context.setVariable("firstName", user.getFName());

        // Render the template with variables
        return templateEngine.process("order-confirmation-email", context);
    }

    //when user sign up
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            // 1. Create Thymeleaf context
            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariable("user", firstName);

            // 2. Process HTML template
            String body = templateEngine.process("welcome-email", context);

            // 3. Create MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Kaylia!");
            helper.setText(body, true); // true = HTML

            // 4. Send email
            mailSender.send(message);

            System.out.println("Welcome email sent to " + toEmail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //when admin registered
    public void sendCredentialsEmail(User user, String rawPassword) {

        String role;

        switch (user.getRole()) {
            case "ROLE_SUPER_ADMIN" -> role = "super admin";
            case "ROLE_VENDOR" -> role = "vendor";
            default -> role = "admin";
        }

        LocalDateTime createdDateTime = LocalDateTime.now();
        Date createdDate = Date.from(createdDateTime.atZone(ZoneId.systemDefault()).toInstant());

        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("firstName", user.getFName());
        context.setVariable("email", user.getEmail());
        context.setVariable("password", rawPassword); // send plain password
        context.setVariable("role", role);
        context.setVariable("createdDate", createdDate);

        String body = templateEngine.process("admin-account-creation-email", context);

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to Kaylia - Your Account Details");
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
