package com.javarecipe.backend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload an image to Cloudinary
     * @param file the image file to upload
     * @param folder the folder to upload to (e.g., "recipes", "avatars")
     * @return CloudinaryUploadResult containing URL and public ID
     * @throws IOException if upload fails
     */
    public CloudinaryUploadResult uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file
        validateImageFile(file);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString();

        // Upload parameters
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "public_id", uniqueFilename,
                "resource_type", "image",
                "format", fileExtension,
                "transformation", ObjectUtils.asMap(
                        "quality", "auto:good",
                        "fetch_format", "auto"
                )
        );

        // Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        return CloudinaryUploadResult.builder()
                .publicId((String) uploadResult.get("public_id"))
                .url((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .bytes((Integer) uploadResult.get("bytes"))
                .build();
    }

    /**
     * Upload a recipe image with specific transformations
     * @param file the image file to upload
     * @return CloudinaryUploadResult containing URL and public ID
     * @throws IOException if upload fails
     */
    public CloudinaryUploadResult uploadRecipeImage(MultipartFile file) throws IOException {
        // Validate file
        validateImageFile(file);

        // Generate unique filename
        String uniqueFilename = "recipe_" + UUID.randomUUID().toString();

        // Upload parameters with recipe-specific transformations
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", "recipes",
                "public_id", uniqueFilename,
                "resource_type", "image",
                "transformation", ObjectUtils.asMap(
                        "width", 800,
                        "height", 600,
                        "crop", "fill",
                        "quality", "auto:good",
                        "fetch_format", "auto"
                )
        );

        // Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        return CloudinaryUploadResult.builder()
                .publicId((String) uploadResult.get("public_id"))
                .url((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .bytes((Integer) uploadResult.get("bytes"))
                .build();
    }

    /**
     * Upload an avatar image with specific transformations
     * @param file the image file to upload
     * @return CloudinaryUploadResult containing URL and public ID
     * @throws IOException if upload fails
     */
    public CloudinaryUploadResult uploadAvatarImage(MultipartFile file) throws IOException {
        // Validate file
        validateImageFile(file);

        // Generate unique filename
        String uniqueFilename = "avatar_" + UUID.randomUUID().toString();

        // Upload parameters with avatar-specific transformations
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", "avatars",
                "public_id", uniqueFilename,
                "resource_type", "image",
                "transformation", ObjectUtils.asMap(
                        "width", 200,
                        "height", 200,
                        "crop", "fill",
                        "gravity", "face",
                        "quality", "auto:good",
                        "fetch_format", "auto"
                )
        );

        // Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        return CloudinaryUploadResult.builder()
                .publicId((String) uploadResult.get("public_id"))
                .url((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .bytes((Integer) uploadResult.get("bytes"))
                .build();
    }

    /**
     * Delete an image from Cloudinary
     * @param publicId the public ID of the image to delete
     * @return true if deletion was successful
     * @throws IOException if deletion fails
     */
    public boolean deleteImage(String publicId) throws IOException {
        Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return "ok".equals(deleteResult.get("result"));
    }

    /**
     * Generate a thumbnail URL for an existing image
     * @param publicId the public ID of the image
     * @param width thumbnail width
     * @param height thumbnail height
     * @return thumbnail URL
     */
    public String generateThumbnailUrl(String publicId, int width, int height) {
        return cloudinary.url()
                .transformation(new Transformation()
                        .width(width)
                        .height(height)
                        .crop("fill")
                        .quality("auto:good")
                        .fetchFormat("auto"))
                .secure(true)
                .generate(publicId);
    }

    /**
     * Validate uploaded image file
     * @param file the file to validate
     * @throws IllegalArgumentException if file is invalid
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size (10MB limit)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Check allowed image types
        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
        boolean isValidType = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new IllegalArgumentException("Only JPEG, PNG, GIF, and WebP images are allowed");
        }
    }

    /**
     * Extract public ID from Cloudinary URL
     * @param cloudinaryUrl the full Cloudinary URL
     * @return public ID or null if URL is invalid
     */
    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return null;
        }

        try {
            // Cloudinary URLs have format: https://res.cloudinary.com/cloud_name/image/upload/v123456/folder/public_id.ext
            // We need to extract the folder/public_id part
            String[] parts = cloudinaryUrl.split("/");
            if (parts.length < 2) {
                return null;
            }

            // Find the upload part
            int uploadIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    uploadIndex = i;
                    break;
                }
            }

            if (uploadIndex == -1 || uploadIndex + 2 >= parts.length) {
                return null;
            }

            // Skip version (v123456) if present
            int startIndex = uploadIndex + 1;
            if (parts[startIndex].startsWith("v") && parts[startIndex].length() > 1) {
                startIndex++;
            }

            // Reconstruct public_id (folder/filename without extension)
            StringBuilder publicId = new StringBuilder();
            for (int i = startIndex; i < parts.length; i++) {
                if (i > startIndex) {
                    publicId.append("/");
                }
                String part = parts[i];
                // Remove file extension from the last part
                if (i == parts.length - 1 && part.contains(".")) {
                    part = part.substring(0, part.lastIndexOf('.'));
                }
                publicId.append(part);
            }

            return publicId.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get file extension from filename
     * @param filename the filename
     * @return file extension without dot
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg"; // default extension
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Result class for Cloudinary upload operations
     */
    public static class CloudinaryUploadResult {
        private String publicId;
        private String url;
        private String format;
        private Integer width;
        private Integer height;
        private Integer bytes;

        // Builder pattern
        public static CloudinaryUploadResultBuilder builder() {
            return new CloudinaryUploadResultBuilder();
        }

        // Getters
        public String getPublicId() { return publicId; }
        public String getUrl() { return url; }
        public String getFormat() { return format; }
        public Integer getWidth() { return width; }
        public Integer getHeight() { return height; }
        public Integer getBytes() { return bytes; }

        // Builder class
        public static class CloudinaryUploadResultBuilder {
            private String publicId;
            private String url;
            private String format;
            private Integer width;
            private Integer height;
            private Integer bytes;

            public CloudinaryUploadResultBuilder publicId(String publicId) {
                this.publicId = publicId;
                return this;
            }

            public CloudinaryUploadResultBuilder url(String url) {
                this.url = url;
                return this;
            }

            public CloudinaryUploadResultBuilder format(String format) {
                this.format = format;
                return this;
            }

            public CloudinaryUploadResultBuilder width(Integer width) {
                this.width = width;
                return this;
            }

            public CloudinaryUploadResultBuilder height(Integer height) {
                this.height = height;
                return this;
            }

            public CloudinaryUploadResultBuilder bytes(Integer bytes) {
                this.bytes = bytes;
                return this;
            }

            public CloudinaryUploadResult build() {
                CloudinaryUploadResult result = new CloudinaryUploadResult();
                result.publicId = this.publicId;
                result.url = this.url;
                result.format = this.format;
                result.width = this.width;
                result.height = this.height;
                result.bytes = this.bytes;
                return result;
            }
        }
    }
}
