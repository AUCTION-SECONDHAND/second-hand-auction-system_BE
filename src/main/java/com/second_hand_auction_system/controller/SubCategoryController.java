package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.subCategory.SubCategoryDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryResponse;
import com.second_hand_auction_system.service.subCategory.ISubCategoryService;
import com.second_hand_auction_system.sse.SubCategoryEventListener;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/sub-category")
public class SubCategoryController {
    private final ISubCategoryService subCategoryService;
    private final SubCategoryEventListener subCategoryEventListener;

    @PostMapping("")
    public ResponseEntity<?> createSubCategory(
            @Valid @RequestBody SubCategoryDto subCategoryDto,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(String.valueOf(errorMessages))
                            .build()
            );
        }
        subCategoryService.addSubCategory(subCategoryDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Tạo nhật danh mục phụ thành công")
                        .build()
        );
    }

    @PutMapping("/{scId}")
    public ResponseEntity<?> updateSubCategory(
            @PathVariable int scId,
            @Valid @RequestBody SubCategoryDto subCategoryDto,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(String.valueOf(errorMessages))
                            .build()
            );
        }
        subCategoryService.updateSubCategory(scId, subCategoryDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Cập nhật danh mục phụ thành công")
                        .build()
        );
    }

    @DeleteMapping("{scId}")
    public ResponseEntity<?> deleteSubCategory(
            @PathVariable int scId
    ) throws Exception {
        subCategoryService.deleteSubCategory(scId);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Xóa nhật danh mục phụ thành công")
                        .build()
        );
    }

    @GetMapping("")
    public ResponseEntity<?> findAllSubCategories() throws Exception {
        List<SubCategoryResponse> subCategoryResponses = subCategoryService.getSubCategory();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("thành công")
                        .data(subCategoryResponses)
                        .build()
        );
    }

//    @GetMapping("/stream")
//    public SseEmitter streamSubCategories() {
//        return subCategoryEventListener.registerEmitter();
//    }

//    @GetMapping("/stream")
//    public SseEmitter streamSubCategories() {
//        SseEmitter emitter = new SseEmitter();
//
//        new Thread(() -> {
//            try {
//                // Gọi service để lấy danh sách SubCategory
//                List<SubCategoryResponse> subCategoryResponses = subCategoryService.getSubCategory();
//
//                // Gửi từng SubCategory qua SSE
//                for (SubCategoryResponse response : subCategoryResponses) {
//                    emitter.send(response);
//                }
//
//                // Kết thúc stream
//                emitter.complete();
//            } catch (Exception e) {
//                emitter.completeWithError(e);
//            }
//        }).start();
//
//        return emitter;
//    }
//@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//public SseEmitter streamSubCategories() {
//    // Tạo một SseEmitter mới
//    SseEmitter emitter = new SseEmitter();
//
//    // Executor để xử lý việc gửi dữ liệu bất đồng bộ
//    ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//    executorService.submit(() -> {
//        try {
//            // Giả sử bạn có một cách để theo dõi hoặc kiểm tra sự thay đổi trong danh sách subCategories
//            while (true) {
//                List<SubCategoryResponse> subCategoryResponses = subCategoryService.getSubCategory();
//
//                // Gửi danh sách subCategories mới mỗi lần có sự thay đổi hoặc khi cần thiết
//                emitter.send(subCategoryResponses);
//
//                // Mỗi lần gửi dữ liệu, có thể đặt thời gian chờ để tránh việc gửi liên tục
//                Thread.sleep(10000); // Gửi lại sau mỗi 10 giây (hoặc tùy vào yêu cầu)
//
//                // Bạn có thể thay đổi điều kiện dừng loop tùy theo yêu cầu.
//            }
//
//        } catch (Exception e) {
//            emitter.completeWithError(e); // Nếu có lỗi, hoàn thành kết nối với lỗi
//        }
//    });
//
//    // Đảm bảo đóng kết nối khi client không còn cần nữa (hoặc khi có sự cố)
//    emitter.onCompletion(() -> executorService.shutdown());
//
//    return emitter;
//}

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSubCategories() {
        return subCategoryEventListener.registerEmitter();
    }


//    @GetMapping("/stream-v2")
//    public SseEmitter streamSubCategoriesV2() {
//        SseEmitter emitter = new SseEmitter(0L);
//
//        new Thread(() -> {
//            try {
//                // Gọi service để lấy danh sách SubCategory
//                List<SubCategoryResponse> subCategoryResponses = subCategoryService.getSubCategory();
//
//                // Gửi toàn bộ danh sách SubCategory qua SSE
//                emitter.send(subCategoryResponses);
//
//                // Kết thúc stream
//                //emitter.complete();
//            } catch (Exception e) {
//                emitter.completeWithError(e);
//            }
//        }).start();
//
//        return emitter;
//    }

}
