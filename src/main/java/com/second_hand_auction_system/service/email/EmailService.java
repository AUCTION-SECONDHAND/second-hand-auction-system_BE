package com.second_hand_auction_system.service.email;

import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
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
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 8;

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


    public String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
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
        otpService.saveOtp(email, otp);
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

    public void sendKycSuccessNotification(String email, String userName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email); // Địa chỉ email người nhận
        helper.setSubject("Xác thực KYC thành công");

        // Nội dung HTML có sử dụng CSS
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "  body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                "  .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }" +
                "  h1 { color: #333333; }" +
                "  p { font-size: 16px; color: #555555; }" +
                "  .highlight { color: #1a73e8; font-weight: bold; }" +
                "  .footer { margin-top: 20px; font-size: 12px; color: #888888; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='email-container'>" +
                "<h1>Kính chào, " + userName + "</h1>" +
                "<p>Chúc mừng bạn đã xác thực thành công thông tin KYC.</p>" +
                "<p>Xin vui lòng đăng nhập và bắt đầu sử dụng các chức năng dành riêng cho người bán trên nền tảng của chúng tôi.</p>" +
                "<p class='highlight'>Chúng tôi rất vui khi có bạn đồng hành!</p>" +
                "<br>" +
                "<p>Trân trọng,</p>" +
                "<p>Đội ngũ hỗ trợ</p>" +
                "<div class='footer'>" +
                "<p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(htmlContent, true); // 'true' để kích hoạt HTML
        mailSender.send(message);
    }

    public void sendNotificationRegisterItem(String email, String userName, String itemName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email); // Địa chỉ email người nhận
        helper.setSubject("Thông báo thẩm định sản phẩm");

        // Nội dung HTML có sử dụng CSS
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "  body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                "  .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }" +
                "  .header { background-color: #1a73e8; padding: 20px; border-radius: 10px 10px 0 0; text-align: center; color: white; }" +
                "  .header h1 { font-size: 26px; margin: 0; }" +
                "  .logo img { max-width: 120px; margin-bottom: 10px; }" +
                "  p { font-size: 16px; line-height: 1.6; color: #555555; margin-bottom: 20px; }" +
                "  .highlight { color: #1a73e8; font-weight: bold; }" +
                "  .footer { margin-top: 30px; font-size: 12px; color: #888888; text-align: center; border-top: 1px solid #eeeeee; padding-top: 10px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='email-container'>" +
                "  <div class='header'>" +
                "    <div class='logo'>" +
                "    </div>" +
                "    <h1>Thông báo sản phẩm</h1>" +
                "  </div>" +
                "  <h2>Xin chào, " + userName + "</h2>" +
                "  <p>Sản phẩm của bạn <span class='highlight'>" + itemName + "</span> đã được thẩm định.</p>" +
                "  <p>Nếu sản phẩm đã được phê duyệt, xin vui lòng đăng nhập và bắt đầu quản lý sản phẩm của bạn trên nền tảng của chúng tôi.</p>" +
                "  <p>Chúng tôi luôn sẵn sàng hỗ trợ nếu bạn có bất kỳ câu hỏi nào.</p>" +
                "  <p>Trân trọng,</p>" +
                "  <p>Đội ngũ hỗ trợ</p>" +
                "  <div class='footer'>" +
                "    <p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "  </div>" +
                "</div>" +
                "</body>" +
                "</html>";


        helper.setText(htmlContent, true); // 'true' để kích hoạt HTML
        mailSender.send(message);

    }

    public void sendSetPassword(String email) throws MessagingException {
        String password = generatePassword();

        otpService.saveOtp(email, password);
        var user = userRepository.findByEmail(email).orElse(null);
        assert user != null;
        String passwordEncode = encoder.encode(password);
        user.setPassword(passwordEncode);
        userRepository.save(user);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Set Password");

        String htmlContent = """
                <html>
                    <head>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                margin: 0;
                                padding: 0;
                                background-color: #f9f9f9;
                            }
                            .container {
                                max-width: 600px;
                                margin: 0 auto;
                                padding: 20px;
                                background-color: #ffffff;
                                border-radius: 10px;
                                box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                            }
                            .header {
                                background-color: #007bff;
                                color: #ffffff;
                                padding: 20px;
                                text-align: center;
                                border-top-left-radius: 10px;
                                border-top-right-radius: 10px;
                            }
                            .header h1 {
                                margin: 0;
                                font-size: 24px;
                            }
                            .content {
                                padding: 20px;
                            }
                            h1 {
                                color: #333333;
                                font-size: 20px;
                            }
                            p {
                                font-size: 16px;
                                color: #555555;
                                line-height: 1.5;
                            }
                            .password {
                                font-weight: bold;
                                color: #d9534f;
                                font-size: 18px;
                            }
                            .footer {
                                background-color: #f1f1f1;
                                padding: 10px;
                                font-size: 12px;
                                color: #777777;
                                text-align: center;
                                border-bottom-left-radius: 10px;
                                border-bottom-right-radius: 10px;
                                margin-top: 20px;
                            }
                            .footer p {
                                margin: 5px 0;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h1>Password Reset Request</h1>
                            </div>
                            <div class="content">
                                <p>Dear User,</p>
                                <p>We have received a request to reset your password. Your new password is:</p>
                                <p class="password">%s</p>
                                <p>Please make sure to change your password after logging in to ensure your account’s security.</p>
                                <p>Best regards,</p>
                                <p>Your Company Team</p>
                            </div>
                            <div class="footer">
                                <p>If you didn’t request this password reset, please contact our support immediately.</p>
                                <p>Company Name | Address | Contact</p>
                            </div>
                        </div>
                    </body>
                </html>
                """.formatted(password);

        mimeMessageHelper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }

    public void sendBidNotification(String email, String username, double newBidAmount, double oldBidAmount, int auctionId) throws MessagingException {
        // Tạo nội dung cho email
        String htmlContent = """
                    <html>
                        <head>
                            <style>
                                body {
                                    font-family: Arial, sans-serif;
                                    margin: 0;
                                    padding: 0;
                                    background-color: #f9f9f9;
                                }
                                .container {
                                    max-width: 600px;
                                    margin: 0 auto;
                                    padding: 20px;
                                    background-color: #ffffff;
                                    border-radius: 10px;
                                    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                                }
                                .header {
                                    background-color: #007bff;
                                    color: #ffffff;
                                    padding: 20px;
                                    text-align: center;
                                    border-top-left-radius: 10px;
                                    border-top-right-radius: 10px;
                                }
                                .header h1 {
                                    margin: 0;
                                    font-size: 24px;
                                }
                                .content {
                                    padding: 20px;
                                }
                                h1 {
                                    color: #333333;
                                    font-size: 20px;
                                }
                                p {
                                    font-size: 16px;
                                    color: #555555;
                                    line-height: 1.5;
                                }
                                .bid-amount {
                                    font-weight: bold;
                                    color: #d9534f;
                                    font-size: 18px;
                                }
                                .footer {
                                    background-color: #f1f1f1;
                                    padding: 10px;
                                    font-size: 12px;
                                    color: #777777;
                                    text-align: center;
                                    border-bottom-left-radius: 10px;
                                    border-bottom-right-radius: 10px;
                                    margin-top: 20px;
                                }
                                .footer p {
                                    margin: 5px 0;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>Bid Notification</h1>
                                </div>
                                <div class="content">
                                    <p>Dear %s,</p>
                                    <p>You have successfully placed a new bid for auction ID: %d.</p>
                                    <p>Your previous bid amount: <span class="bid-amount">$%.2f</span></p>
                                    <p>Your new bid amount: <span class="bid-amount">$%.2f</span></p>
                                    <p>Thank you for participating in the auction!</p>
                                    <p>Best regards,</p>
                                    <p>Your Company Team</p>
                                </div>
                                <div class="footer">
                                    <p>If you have any questions, please contact our support team.</p>
                                    <p>Company Name | Address | Contact</p>
                                </div>
                            </div>
                        </body>
                    </html>
                """.formatted(username, auctionId, oldBidAmount, newBidAmount);

        // Tạo và gửi email
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Bid Placed Successfully");
        mimeMessageHelper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }


    public void sendWinnerNotification(String email, Bid winningBid) throws MessagingException, IOException {
        // Tạo MimeMessage
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        // Thiết lập thông tin email
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Thông Báo Bạn Đã Thắng Cuộc Đấu Giá");

        // Tạo nội dung email (HTML) với các giá trị động
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Thông Báo Thắng Cuộc</title>" +
                "<style>" +
                "body { font-family: 'Arial', sans-serif; background-color: #f4f7fb; margin: 0; padding: 0; }" +
                ".email-container { background-color: #ffffff; border-radius: 8px; box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1); padding: 30px; margin: 20px auto; width: 600px; max-width: 100%; }" +
                ".email-header { text-align: center; margin-bottom: 30px; }" +
                ".email-header h1 { color: #4CAF50; font-size: 28px; margin: 0; }" +
                ".email-content { font-size: 16px; color: #333333; line-height: 1.6; }" +
                ".email-content p { margin-bottom: 15px; }" +
                ".email-content ul { padding-left: 20px; margin-bottom: 20px; }" +
                ".email-content li { font-size: 16px; margin-bottom: 10px; }" +
                ".footer { margin-top: 30px; text-align: center; font-size: 14px; color: #888888; padding-top: 20px; border-top: 1px solid #f1f1f1; }" +
                ".footer p { margin: 0; }" +
                ".footer a { color: #4CAF50; text-decoration: none; font-weight: bold; }" +
                ".btn { display: inline-block; background-color: #4CAF50; color: #ffffff; padding: 12px 20px; border-radius: 5px; text-decoration: none; font-size: 16px; font-weight: bold; margin-top: 20px; }" +
                ".btn:hover { background-color: #45a049; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"email-container\">" +
                "<div class=\"email-header\">" +
                "<h1>Chúc Mừng! Bạn Đã Thắng Cuộc Đấu Giá!</h1>" +
                "</div>" +
                "<div class=\"email-content\">" +
                "<p>Chào <strong>" + winningBid.getUser().getFullName() + "</strong>,</p>" +
                "<p>Chúng tôi vui mừng thông báo rằng bạn đã thắng cuộc đấu giá với mã <strong>" + winningBid.getAuction().getAuctionId() + "</strong>!</p>" +
                "<p>Thông tin chi tiết về chiến thắng của bạn:</p>" +
                "<ul>" +
                "<li><strong>Mã Đấu Giá:</strong> " + winningBid.getAuction().getAuctionId() + "</li>" +
                "<li><strong>Số Tiền Đặt Cược:</strong> " + winningBid.getBidAmount() + " VNĐ</li>" +
                "<li><strong>Sản Phẩm:</strong> " + winningBid.getAuction().getItem().getItemName() + "</li>" +
                "</ul>" +
                "<p>Để hoàn tất giao dịch, vui lòng thanh toán trong vòng 24 giờ. Nếu không thanh toán đúng hạn, bạn có thể mất quyền thắng cuộc.</p>" +
                "<p><a href=\"http://localhost:5173/Order/" + winningBid.getAuction().getAuctionId() + "\" class=\"btn\">Thanh toán ngay</a></p>"+
                "</div>" +
                "<div class=\"footer\">" +
                "<p>Trân trọng,</p>" +
                "<p>Đội ngũ Đấu giá của chúng tôi</p>" +
                "<p><a href=\"http://localhost:5173/Order\">Xem thông tin thêm về đấu giá của chúng tôi</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

// Thiết lập nội dung email (HTML)
        mimeMessageHelper.setText(htmlContent, true);


        // Thiết lập nội dung email (HTML)
        mimeMessageHelper.setText(htmlContent, true);

        // Gửi email
        mailSender.send(mimeMessage);


    }



    public void sendResultForAuction(String email, Bid winningBid) throws MessagingException {
        // Tạo MimeMessage
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        // Thiết lập thông tin email
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Kết quả đấu giá");
        // Tạo nội dung email cho người thua
        String htmlContent = "<html><body>" +
                "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; text-align: center;\">" +
                "<h2 style=\"color: #FF6347;\">Rất Tiếc! Bạn Đã Không Thắng Cuộc Đấu Giá</h2>" +
                "<p style=\"font-size: 16px; color: #333;\">Chúng tôi rất tiếc phải thông báo rằng bạn đã không thắng cuộc đấu giá với mã <strong>" + winningBid.getAuction().getAuctionId() + "</strong>.</p>" +
                "<p style=\"font-size: 16px; color: #333;\">Sản phẩm: <strong>" + winningBid.getAuction().getItem().getItemName() + "</strong></p>" +
                "<p style=\"font-size: 16px; color: #333;\">Số tiền cược cao nhất là : <strong>" + winningBid.getBidAmount() + " VNĐ</strong></p>" +
                "<p style=\"font-size: 16px; color: #333;\">Tuy nhiên, đừng lo! Tiền cọc của bạn sẽ được hoàn lại trong thời gian sớm nhất.</p>" +
                "<p style=\"font-size: 16px; color: #333;\">Xin cảm ơn bạn đã tham gia đấu giá của chúng tôi! Hãy tham gia những phiên đấu giá tiếp theo để có cơ hội chiến thắng.</p>" +
                "<div style=\"margin-top: 20px;\">" +
                "<p style=\"font-size: 14px; color: #888888;\">Trân trọng, Đội ngũ Đấu giá của chúng tôi</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
        // Thiết lập nội dung email (HTML)
        mimeMessageHelper.setText(htmlContent, true);

        // Gửi email
        mailSender.send(mimeMessage);
    }

}
