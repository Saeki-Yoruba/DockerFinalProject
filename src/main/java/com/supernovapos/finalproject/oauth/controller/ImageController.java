package com.supernovapos.finalproject.oauth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.oauth.service.impl.ImageBBService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

	private final ImageBBService imageBBService;

	@PostMapping("/upload")
	@Operation(summary = "上傳圖片", description = "將圖片上傳至 ImageBB，回傳圖片 URL")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "上傳成功", content = @Content(schema = @Schema(example = "{ \"url\": \"https://i.ibb.co/xxxxxx.png\" }"))),
			@ApiResponse(responseCode = "400", description = "格式或大小不合法", content = @Content(schema = @Schema(example = "{ \"error\": \"只允許 jpg/jpeg/png/webp 格式\" }"))),
			@ApiResponse(responseCode = "500", description = "伺服器錯誤", content = @Content(schema = @Schema(example = "{ \"error\": \"上傳失敗: ...\" }")))
	})
	public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
	    try {
	        String url = imageBBService.uploadImage(file);
	        return ResponseEntity.ok(Map.of("url", url));
	    } catch (InvalidRequestException e) {
	        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "上傳失敗: " + e.getMessage()));
	    }
	}
}
