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

        // Thêm các file đính kèm nếu có
        if (files != null) {
            for (MultipartFile file : files) {
                mimeMessageHelper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), file);
            }
        }

        // Thêm CSS và nội dung HTML
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

        // Set the email details
        mimeMessageHelper.setFrom(mail);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Your OTP Code");

        // Xử lý nội dung HTML cho email với OTP được thêm vào
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


        // Set the content as HTML
        mimeMessageHelper.setText(htmlContent, true);

        // Send the email
        mailSender.send(message);
        otpService.saveOtp(email,otp);
        log.info("OTP has been sent to " + email + " with OTP: " + otp);
    }


}
