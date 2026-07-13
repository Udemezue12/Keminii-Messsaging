package com.astrotech.chat.cloudinary;

import com.astrotech.chat.config.CloudinaryConfig;
import com.astrotech.chat.enums.MediaType;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.InternalServerException;
import com.astrotech.chat.util.EncryptionUtil;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private static final Map<String, String> MIME_TO_RESOURCE = Map.ofEntries(
            Map.entry("image/jpeg", "image"),
            Map.entry("image/png", "image"),
            Map.entry("image/gif", "image"),
            Map.entry("image/webp", "image"),
            Map.entry("video/mp4", "video"),
            Map.entry("video/webm", "video"),
            Map.entry("audio/mpeg", "video"),
            Map.entry("audio/ogg", "video"),
            Map.entry("application/pdf", "raw")
    );
    private static final Map<String, Long> MAX_SIZES = Map.of(
            "image", 16L * 1024 * 1024,   // 16 MB
            "video", 64L * 1024 * 1024,   // 64 MB
            "raw", 10L * 1024 * 1024      // 10 MB
    );

    private final Cloudinary cloudinary;
    private final CloudinaryConfig config;
    private final EncryptionUtil encryptionUtil;

    public CloudinarySignedUploadResponse generateUploadSignature(
            String conversationId,
            CloudinarySignatureRequest request
            ) {
        var fileSize = request.fileSize();
        var mimeType = request.mimeType();
        String fileName = request.fileName();
        validateRequests(conversationId, fileSize, mimeType, fileName);

        var resourceType = MIME_TO_RESOURCE.get(mimeType);
        if (resourceType == null) {
            throw new BadRequestException("Unsupported file type");
        }

        var maxSize = MAX_SIZES.get(resourceType);
        if (fileSize > maxSize) {
            throw new BadRequestException(
                    "Maximum allowed size is " + (maxSize / (1024 * 1024)) + " MB"
            );
        }

        var timestamp = Instant.now().getEpochSecond();
        validateTimestamp(timestamp);

        var folder = "securechat/uploads/" + fileName;
        var publicId = "securechat/" + conversationId + "/" + UUID.randomUUID();

        var eager = switch (resourceType) {
            case "image" -> "f_auto,q_auto";
            case "video" -> "q_auto";
            default -> null;
        };

        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);
        params.put("public_id", publicId);
        params.put("allowed_formats", "jpg,jpeg,png,webp");
        params.put("overwrite", false);
        params.put("tags", List.of("securechat", conversationId));
        params.put("max_file_size", maxSize);
        params.put("resource_type", resourceType);

        if (eager != null) {
            params.put("eager", eager);
        }

        try {
            var signature = cloudinary.apiSignRequest(
                    params,
                    config.getApiSecret(),
                    config.getSignatureVersion()
            );

            List<String> allowedFormats = switch (resourceType) {
                case "image" -> List.of("jpg", "jpeg", "png", "gif", "webp");
                case "video" -> List.of("mp4", "webm", "mp3", "ogg");
                default -> List.of("pdf");
            };

            return CloudinarySignedUploadResponse.builder()
                    .signature(signature)
                    .timestamp(timestamp)
                    .apiKey(config.getApiKey())
                    .cloudName(config.getCloudName())
                    .folder(folder)
                    .publicId(publicId)
                    .resourceType(resourceType)
                    .eager(eager)
                    .maxFileSize(maxSize)
                    .allowedFormats(allowedFormats)
                    .build();

        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to generate Cloudinary signature", ex);
            throw new InternalServerException("Failed to generate upload signature");
        }
    }

    private static void validateRequests(String conversationId, Long fileSize, String mimeType, String fileName) {
        if (!StringUtils.hasText(conversationId)) {
            throw new BadRequestException("Conversation id is required");
        }
        if (fileSize == null) {
            throw new BadRequestException("File size is required.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("File name cannot be empty.");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new BadRequestException("File MIME type is required.");

        }
    }


    public CloudinaryBackendUploadResponse uploadLocalFile(
            String filePath,
            String conversationId,
            String mimeType,
            String originalName) throws IOException {

        File file = new File(filePath);
        if (!file.exists()) {
            throw new BadRequestException("Target file does not exist for batch processing");
        }

        String resourceType = MIME_TO_RESOURCE.getOrDefault(mimeType, "raw");
        validateFileSize(file.length(), resourceType);

        var mediaType = resolveMediaType(mimeType);
        var publicId = "securechat/" + conversationId + "/" + UUID.randomUUID();

        String checksum;
        try (InputStream is = Files.newInputStream(file.toPath())) {
            checksum = encryptionUtil.sha256Checksum(is);
        }

        Map<String, Object> options = new HashMap<>();
        options.put("public_id", publicId);
        options.put("resource_type", resourceType);
        options.put("overwrite", false);
        options.put("unique_filename", false);
        options.put("tags", List.of("securechat", conversationId));


        if ("image".equals(resourceType)) {
            options.put("eager", List.of(
                    Map.of("width", 400, "height", 400, "crop", "thumb", "gravity", "face")
            ));
            options.put("eager_async", true);
        } else if ("video".equals(resourceType) && mimeType != null && mimeType.startsWith("video")) {
            options.put("eager", List.of(
                    Map.of("width", 320, "height", 240, "crop", "fit", "format", "jpg")
            ));
            options.put("eager_async", true);
        }

        Map<?, ?> result = cloudinary.uploader().upload(file, options);
        Files.deleteIfExists(file.toPath());

        log.info("Local media processed and uploaded: publicId={}", publicId);

        return assembleMediaUploadResponse(result, publicId, resourceType, mimeType, mediaType, file.length(), originalName, checksum);
    }


    public CloudinaryBackendUploadResponse uploadMedia(
            MultipartFile file,
            String conversationId,
            String userId) {

        validateFile(file);

        var mimeType = file.getContentType();
        var resourceType = MIME_TO_RESOURCE.getOrDefault(mimeType, "raw");

        validateFileSize(file.getSize(), resourceType);

        var mediaType = resolveMediaType(mimeType);
        String publicId = "securechat/" + conversationId + "/" + UUID.randomUUID();

        Map<String, Object> options = buildUploadOptions(
                publicId,
                resourceType,
                mimeType,
                conversationId,
                userId
        );

        boolean uploaded = false;
        File tempFile = null;

        try {
            var digest = MessageDigest.getInstance("SHA-256");
            Map<?, ?> uploadResult;
            String checksum;

            // Create a fast temporary disk file to bypass wrapper parameter casting restrictions
            tempFile = File.createTempFile("securechat-", ".tmp");


            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = new FileOutputStream(tempFile);
                 DigestOutputStream digestOutputStream = new DigestOutputStream(outputStream, digest)) {

                inputStream.transferTo(digestOutputStream);
            }


            checksum = HexFormat.of().formatHex(digest.digest());


            if ("video".equals(resourceType) && file.getSize() >= 100L * 1024 * 1024) {
                uploadResult = cloudinary.uploader().uploadLarge(tempFile, options);
            } else {
                uploadResult = cloudinary.uploader().upload(tempFile, options);
            }

            uploaded = true;

            log.info(
                    "Media uploaded successfully. publicId={}, userId={}, size={} KB",
                    publicId,
                    userId,
                    file.getSize() / 1024
            );

            return assembleMediaUploadResponse(
                    uploadResult,
                    publicId,
                    resourceType,
                    mimeType,
                    mediaType,
                    file.getSize(),
                    file.getOriginalFilename(),
                    checksum
            );

        } catch (IOException e) {
            log.error("I/O error uploading media for user {}", userId, e);
            throw new BadRequestException("Failed to upload media.");

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm unavailable", e);
            throw new IllegalStateException("SHA-256 algorithm is unavailable.", e);

        } catch (Exception e) {
            log.error("Unexpected Cloudinary upload error for user {}", userId, e);
            if (uploaded) {
                deleteResource(publicId, resourceType);
            }
            throw new BadRequestException("Failed to upload media.");

        } finally {

            if (tempFile != null && tempFile.exists()) {
                try {
                    java.nio.file.Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    log.warn("Failed to clean up local temporary file at {}", tempFile.getAbsolutePath(), e);
                }
            }
        }
    }

    public List<CloudinaryBackendUploadResponse> processBatchUpload(MultipartFile[] files, String conversationId, String userId) {
        List<CloudinaryBackendUploadResponse> uploadedResponses = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            try {

               var response = uploadMedia(file, conversationId, userId);


                uploadedResponses.add(response);

            } catch (Exception e) {
                log.error("Failed to process and upload file: {}",
                        file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file", e);


                throw new InternalServerException("Batch processing aborted");
            }
        }

        return uploadedResponses;
    }

    public String getSignedUrl(String publicId, String mimeType, int expiresInSeconds) {
        try {
            String resourceType = MIME_TO_RESOURCE.getOrDefault(mimeType, "image");
            long expiresAt = (System.currentTimeMillis() / 1000) + expiresInSeconds;
            return cloudinary.url()
                    .resourceType(resourceType)
                    .signed(true)
                    .generate(publicId + "?_a=" + expiresAt);
        } catch (Exception e) {
            log.warn("Could not generate signed URL: {}", e.getMessage());
            return publicId;
        }
    }

    public void deleteResource(String publicId, String resourceType) {
        var options = ObjectUtils.asMap(
                "resource_type", resourceType,
                "invalidate", true
        );
        try {
            cloudinary.uploader().destroy(publicId, options);
        } catch (Exception e) {
            log.error("Failed to delete resource {}: {}", publicId, e.getMessage());
            throw new InternalServerException("Error deleting resource");
        }
    }

    public void deleteResources(List<String> publicIds) {
        try {
            cloudinary.api().deleteResources(
                    publicIds,
                    ObjectUtils.asMap("invalidate", true)
            );
        } catch (Exception e) {
            log.error("Failed to delete multi-resources: {}", e.getMessage());
            throw new InternalServerException("Error deleting resources");
        }
    }

    public Map uploadPdf(File file, String folder, String publicId) {
        try {
            return cloudinary.uploader().upload(
                    file,
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", folder,
                            "public_id", publicId
                    )
            );
        } catch (Exception e) {
            log.error("Failed PDF upload: {}", e.getMessage());
            throw new InternalServerException("Error uploading file");
        }
    }

    public String generateSignedPdfUrl(String publicId) {
        try {
            return cloudinary.url()
                    .resourceType("raw")
                    .signed(true)
                    .generate(publicId);
        } catch (Exception e) {
            log.error("Failed generating signed PDF URL: {}", e.getMessage());
            throw new InternalServerException("Error generating signed upload signature");
        }
    }

    public boolean resourceExists(String publicId, String resourceType) {
        try {
            cloudinary.api().resource(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private void validateTimestamp(long timestamp) {
        long now = Instant.now().getEpochSecond();
        if ((now - timestamp) > 30) {
            throw new BadRequestException("Timestamp expired");
        }
        if (timestamp > now + 5) {
            throw new BadRequestException("Timestamp is in the future");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String mimeType = file.getContentType();
        if (mimeType == null || !MIME_TO_RESOURCE.containsKey(mimeType)) {
            throw new BadRequestException("Unsupported file type: " + mimeType);
        }
    }

    private void validateFileSize(long fileSize, String resourceType) {
        Long maxSize = MAX_SIZES.get(resourceType);
        if (maxSize == null) {
            throw new BadRequestException("Unsupported resource type");
        }
        if (fileSize > maxSize) {
            throw new BadRequestException(
                    String.format("File exceeds the maximum allowed size of %d MB", maxSize / (1024 * 1024))
            );
        }
    }

    private String buildThumbnailUrl(Map<?, ?> result, String resourceType, String mimeType) {
        var url = (String) result.get("secure_url");
        if (url == null) return null;

        if ("image".equals(resourceType)) {
            return url.replace("/upload/", "/upload/w_400,h_400,c_thumb,g_face/");
        }
        if ("video".equals(resourceType) && mimeType != null && mimeType.startsWith("video")) {
            return url.replace("/upload/", "/upload/w_320,h_240,c_fit,so_0/").replaceAll("\\.[^.]+$", ".jpg");
        }
        return null;
    }

    private MediaType resolveMediaType(String mimeType) {
        if (mimeType == null) return MediaType.DOCUMENT;
        if (mimeType.startsWith("image/")) return MediaType.IMAGE;
        if (mimeType.startsWith("video/")) return MediaType.VIDEO;
        if (mimeType.startsWith("audio/")) return MediaType.AUDIO;
        return MediaType.DOCUMENT;
    }

    private CloudinaryBackendUploadResponse assembleMediaUploadResponse(
            Map<?, ?> result, String publicId, String resourceType, String mimeType,
            MediaType mediaType, long fileSize, String originalName, String checksum) {

        var secureUrl = (String) result.get("secure_url");
        String thumbnailUrl = buildThumbnailUrl(result, resourceType, mimeType);
        Integer width = result.get("width") != null ? (Integer) result.get("width") : null;
        Integer height = result.get("height") != null ? (Integer) result.get("height") : null;
        Integer duration = result.get("duration") != null ? ((Number) result.get("duration")).intValue() : null;

        return CloudinaryBackendUploadResponse.builder()
                .publicId(publicId).secureUrl(secureUrl).thumbnailUrl(thumbnailUrl)
                .originalName(originalName).mimeType(mimeType).mediaType(mediaType)
                .fileSize(fileSize).width(width).height(height).duration(duration).checksum(checksum)
                .build();
    }
    private Map<String, Object> buildUploadOptions(
            String publicId,
            String resourceType,
            String mimeType,
            String conversationId,
            String userId
    ) {

        Map<String, Object> options = new HashMap<>();

        options.put("public_id", publicId);
        options.put("resource_type", resourceType);
        options.put("overwrite", false);
        options.put("unique_filename", false);
        options.put("tags", List.of(
                "securechat",
                conversationId,
                userId
        ));

        if ("image".equals(resourceType)) {

            options.put(
                    "eager",
                    List.of(
                            new Transformation<>()
                                    .width(400)
                                    .height(400)
                                    .crop("thumb")
                                    .gravity("face")
                    )
            );

            options.put("eager_async", true);

        } else if ("video".equals(resourceType)
                && mimeType != null
                && mimeType.startsWith("video")) {

            options.put(
                    "eager",
                    List.of(
                            new Transformation<>()
                                    .width(320)
                                    .height(240)
                                    .crop("fit")
                                    .fetchFormat("jpg")
                    )
            );

            options.put("eager_async", true);
        }

        return options;
    }
}