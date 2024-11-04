//package com.second_hand_auction_system.service.cloud;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import jakarta.validation.constraints.Pattern;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class CloudinaryService {
//    private final Cloudinary cloudinary;
//    public String uploadImage(MultipartFile imageFile) throws IOException {
//        if (imageFile.isEmpty()) {
//            throw new IOException("No file uploaded");
//        }
//        Map<String, Object> uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.asMap("folder", "kyc_documents"));
//        return (String) uploadResult.get("url");
//    }
//
//   public String uploadImageFromUrl(@Pattern(regexp = "^(http(s?):)([/|.|\\w|\\s|-])*\\.(?:jpg|jpeg|png)$", message = "Invalid image URL format") String imageUrl) throws IOException {
//        Map<String, Object> uploadResult = cloudinary.uploader().upload(imageUrl, ObjectUtils.asMap("folder", "kyc_documents"));
//        return (String) uploadResult.get("url");
//    }
//}
