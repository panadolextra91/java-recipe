package com.javarecipe.backend.common.controller;

import com.javarecipe.backend.common.service.CloudinaryService;
import com.javarecipe.backend.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public ImageUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Upload a recipe image
     */
    @PostMapping("/upload/recipe")
    public ResponseEntity<?> uploadRecipeImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {

        try {
            CloudinaryService.CloudinaryUploadResult result = cloudinaryService.uploadRecipeImage(file);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image uploaded successfully");
            response.put("imageUrl", result.getUrl());
            response.put("publicId", result.getPublicId());
            response.put("width", result.getWidth());
            response.put("height", result.getHeight());
            response.put("format", result.getFormat());
            response.put("size", result.getBytes());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid file: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Upload a general image to a specific folder
     */
    @PostMapping("/upload/{folder}")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @PathVariable String folder,
            @AuthenticationPrincipal User currentUser) {

        try {
            // Validate folder name
            if (!isValidFolder(folder)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid folder name. Allowed: recipes, avatars, general");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            CloudinaryService.CloudinaryUploadResult result = cloudinaryService.uploadImage(file, folder);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image uploaded successfully");
            response.put("imageUrl", result.getUrl());
            response.put("publicId", result.getPublicId());
            response.put("width", result.getWidth());
            response.put("height", result.getHeight());
            response.put("format", result.getFormat());
            response.put("size", result.getBytes());
            response.put("folder", folder);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid file: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete an image by public ID
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String publicId,
            @AuthenticationPrincipal User currentUser) {

        try {
            boolean deleted = cloudinaryService.deleteImage(publicId);

            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image deleted successfully");
                response.put("publicId", publicId);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Failed to delete image or image not found");
                return ResponseEntity.notFound().build();
            }

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Generate thumbnail URL for an existing image
     */
    @GetMapping("/{publicId}/thumbnail")
    public ResponseEntity<?> generateThumbnail(
            @PathVariable String publicId,
            @RequestParam(defaultValue = "200") int width,
            @RequestParam(defaultValue = "200") int height,
            @AuthenticationPrincipal User currentUser) {

        try {
            // Validate dimensions
            if (width <= 0 || height <= 0 || width > 1000 || height > 1000) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid dimensions. Width and height must be between 1 and 1000 pixels");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String thumbnailUrl = cloudinaryService.generateThumbnailUrl(publicId, width, height);

            Map<String, Object> response = new HashMap<>();
            response.put("thumbnailUrl", thumbnailUrl);
            response.put("publicId", publicId);
            response.put("width", width);
            response.put("height", height);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to generate thumbnail: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get upload guidelines and limits
     */
    @GetMapping("/upload-info")
    public ResponseEntity<?> getUploadInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("maxFileSize", "10MB");
        info.put("allowedFormats", new String[]{"JPEG", "PNG", "GIF", "WebP"});
        info.put("allowedFolders", new String[]{"recipes", "avatars", "general"});
        info.put("recommendedDimensions", Map.of(
                "recipes", "800x600 pixels",
                "avatars", "200x200 pixels",
                "general", "No specific requirements"
        ));

        return ResponseEntity.ok(info);
    }

    /**
     * Validate folder name
     */
    private boolean isValidFolder(String folder) {
        String[] allowedFolders = {"recipes", "avatars", "general"};
        for (String allowed : allowedFolders) {
            if (allowed.equals(folder)) {
                return true;
            }
        }
        return false;
    }
}
