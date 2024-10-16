package com.second_hand_auction_system.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    @Value("${spring.mail.username}")
    private String mail;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final OtpService otpService;

    public String sendEmail(String to, String subject, String text, MultipartFile[] files) throws MessagingException {
        log.info("Sending email to " + to);

        // Tạo email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true, "UTF-8");

        mimeMessageHelper.setFrom(mail);

        if (to.contains(",")) {
            mimeMessageHelper.setTo(InternetAddress.parse(to));
        } else {
            mimeMessageHelper.setTo(to);
        }

        if (files != null) {
            for (MultipartFile file : files) {
                mimeMessageHelper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), file);
            }
        }
        String htmlContent = """
        <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 20px;
                    }
                    .email-container {
                        background-color: #ffffff;
                        border-radius: 8px;
                        padding: 20px;
                        box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
                    }
                    .email-header {
                        font-size: 24px;
                        color: #333333;
                        margin-bottom: 20px;
                    }
                    .email-content {
                        font-size: 16px;
                        color: #666666;
                        line-height: 1.5;
                    }
                    .email-footer {
                        font-size: 14px;
                        color: #999999;
                        margin-top: 30px;
                        text-align: center;
                    }
                    a {
                        color: #007bff;
                        text-decoration: none;
                    }
                    a:hover {
                        color: #0056b3;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-header">
                        %s
                    </div>
                    <div class="email-content">
                        %s
                    </div>
                    <div class="email-footer">
                        <p>Thank you for choosing our service!</p>
                        <p>For any questions, please contact us at <a href="mailto:support@example.com">support@example.com</a>.</p>
                    </div>
                </div>
            </body>
        </html>
        """.formatted(subject, text);  // Chèn chủ đề và nội dung vào template HTML

        // Cài đặt nội dung HTML vào email
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(htmlContent, true);  // Đặt true để sử dụng HTML

        // Gửi email
        mailSender.send(message);
        log.info("Email has been sent to " + to);

        return "Email has been sent to " + to;
    }




    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    public void sendOtp(String email, Integer id) throws MessagingException, IOException {
        log.info("Sending OTP to " + email);
        String otp = generateOtp();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        mimeMessageHelper.setFrom(mail);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Your OTP Code");
        String htmlContent = """
    <html>
        <head>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f7f7f7;
                    margin: 0;
                    padding: 0;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    background-color: #ffffff;
                    padding: 20px;
                    border-radius: 8px;
                    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                    text-align: center;
                }
                .header {
                    background-color: #007bff;
                    padding: 20px;
                    border-top-left-radius: 8px;
                    border-top-right-radius: 8px;
                    color: white;
                }
                .header h1 {
                    margin: 0;
                    font-size: 24px;
                }
                .otp {
                    font-size: 28px;
                    font-weight: bold;
                    color: #007bff;
                    margin: 20px 0;
                }
                .message {
                    font-size: 16px;
                    color: #333333;
                    margin-bottom: 20px;
                }
                .footer {
                    font-size: 12px;
                    color: #999999;
                    margin-top: 20px;
                }
                .footer a {
                    color: #007bff;
                    text-decoration: none;
                }
                .footer a:hover {
                    text-decoration: underline;
                }
                hr {
                    border: 0;
                    height: 1px;
                    background: #eeeeee;
                    margin: 20px 0;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>OTP Verification</h1>
                </div>
                <div class="message">
                    <p>Thank you for using our service. To continue, please use the following OTP code:</p>
                </div>
                <div class="otp">
                    %s
                </div>
                <hr>
                <div class="message">
                    <p>The OTP code is valid for <strong>10 minutes</strong>. Please do not share this code with anyone.</p>
                </div>
                <div class="footer">
                    <p>If you did not request this OTP, please ignore this email or <a href="#">contact support</a>.</p>
                </div>
            </div>
        </body>
    </html>
""".formatted(otp);
        mimeMessageHelper.setText(htmlContent, true);
        mailSender.send(message);
        otpService.saveOtp(email,otp);
        log.info("OTP has been sent to " + email + " with OTP: " + otp);
    }

    public void sendNotification(String email, Integer id, int amount) throws MessagingException, IOException {
        log.info("Sending notification to " + email);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        mimeMessageHelper.setFrom(mail);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Thông báo nạp tiền vào ví");

        String htmlContent = """
    <html>
        <head>
            <style>
                body {
                    font-family: 'Arial', sans-serif;
                    background-color: #f4f4f4;
                    margin: 0;
                    padding: 0;
                }
                .container {
                    max-width: 600px;
                    margin: 30px auto;
                    background-color: #ffffff;
                    padding: 30px;
                    border-radius: 10px;
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                }
                .header {
                    background-color: #4CAF50;
                    padding: 20px;
                    border-top-left-radius: 10px;
                    border-top-right-radius: 10px;
                    color: white;
                    text-align: center;
                }
                .header h1 {
                    font-size: 26px;
                    margin: 0;
                }
                .content {
                    text-align: left;
                    margin: 20px 0;
                    color: #333;
                }
                .content p {
                    font-size: 16px;
                    line-height: 1.6;
                    margin: 10px 0;
                }
                .amount {
                    display: inline-block;
                    font-size: 30px;
                    font-weight: bold;
                    background-color: #f7f7f7;
                    padding: 15px;
                    margin: 20px 0;
                    color: #4CAF50;
                    border-radius: 8px;
                }
                .footer {
                    font-size: 12px;
                    color: #666;
                    text-align: center;
                    margin-top: 30px;
                }
                .footer a {
                    color: #4CAF50;
                    text-decoration: none;
                }
                .footer a:hover {
                    text-decoration: underline;
                }
                .note {
                    font-size: 14px;
                    color: #555;
                    margin-top: 20px;
                    text-align: center;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Thông báo nạp tiền vào ví</h1>
                </div>
                <div class="content">
                    <p>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi. Bạn vừa nạp tiền thành công vào ví với số tiền:</p>
                    <div class="amount">%d VNĐ</div>
                    <p>Số tiền này đã được cập nhật vào ví của bạn. Cảm ơn bạn đã tin tưởng sử dụng dịch vụ của chúng tôi!</p>
                </div>
                <div class="note">
                    <p>Nếu bạn gặp vấn đề gì, hãy liên hệ với <a href="#">bộ phận hỗ trợ</a>.</p>
                </div>
                <div class="footer">
                    <p>© 2024 Hệ thống đấu giá | All rights reserved</p>
                </div>
            </div>
        </body>
    </html>
    """.formatted(amount);

        mimeMessageHelper.setText(htmlContent, true);
        mailSender.send(message);
        log.info("Notification has been sent to " + email + " about deposit of " + amount + " VNĐ.");
    }

//    public void sendAccountStaff(String email, String password) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//        helper.setTo(email); // Địa chỉ email người nhận
//        helper.setSubject("Thông tin tài khoản nhân viên");
//
//        // Nội dung email
//        String content = "<p>Xin chào,</p>"
//                + "<p>Thông tin tài khoản của bạn như sau:</p>"
//                + "<p><strong>Email:</strong> " + email + "</p>"
//                + "<p><strong>Password:</strong> " + password + "</p>"
//                + "<p>Vui lòng thay đổi mật khẩu sau khi đăng nhập lần đầu.</p>"
//                + "<br><p>Trân trọng,</p><p>Đội ngũ hỗ trợ</p>";
//
//        helper.setText(content, true); // 'true' để kích hoạt HTML
//
//        // Gửi email
//        mailSender.send(message);
//    }

}
