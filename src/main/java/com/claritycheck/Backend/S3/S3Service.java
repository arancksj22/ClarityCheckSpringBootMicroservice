package com.claritycheck.Backend.S3;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Template s3Template;
    private final S3Client s3Client;
    private final RestTemplate restTemplate;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Template s3Template, S3Client s3Client) {
        this.s3Template = s3Template;
        this.s3Client = s3Client;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Uploads a file with Quota Check (Max 3 files per user)
     */
    public String upload(MultipartFile file, String userEmail) throws IOException {
        String userPrefix = userEmail + "/";

        // 1. CHECK QUOTA
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userPrefix)
                .maxKeys(4)
                .build();

        ListObjectsV2Response result = s3Client.listObjectsV2(listRequest);
        int currentCount = result.keyCount();

        // NOTE: Temporarily set to 10 for testing, change back to 3 for prod
        if (currentCount >= 10) {
            throw new RuntimeException("Upload limit reached! User " + userEmail + " already has 3 files.");
        }

        // 2. UPLOAD TO S3
        String key = userPrefix + file.getOriginalFilename();
        s3Template.upload(bucketName, key, file.getInputStream());

        // 3. EXTRACT TEXT (PDFBox 2.0 Style)
        String extractedText = "";
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
        }

        String pythonServiceUrl = "https://claritycheckfastapimicroservice.onrender.com/analyze";

        // Prepare Payload
        Map<String, String> payload = Collections.singletonMap("text", extractedText);

        // Send Request & Extract "filtered_text" from response
        try {
            Map response = restTemplate.postForObject(pythonServiceUrl, payload, Map.class);
            if (response != null && response.containsKey("filtered_text")) {
                return (String) response.get("filtered_text");
            }
        } catch (Exception e) {
            // Fallback: If Python fails, just return the first 5000 chars of raw text
            // so the app doesn't crash.
            System.err.println("Python Microservice failed: " + e.getMessage());
            return extractedText.substring(0, Math.min(extractedText.length(), 5000));
        }

        return ""; // Should not happen
    }

    /**
     * NEW METHOD: Lists files for a specific user only
     */
    public List<String> listFiles(String userEmail) {
        String userPrefix = userEmail + "/";

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userPrefix) // Only look in this user's folder
                .build();

        return s3Client.listObjectsV2(request).contents().stream()
                .map(S3Object::key)
                // Remove the "email/" prefix so frontend just sees "resume.pdf"
                .map(key -> key.replace(userPrefix, ""))
                .collect(Collectors.toList());
    }

    /**
     * UPDATED: Takes email to construct the secure path
     */
    public InputStream download(String fileName, String userEmail) throws IOException {
        String fullPath = userEmail + "/" + fileName;
        S3Resource s3Resource = s3Template.download(bucketName, fullPath);
        return s3Resource.getInputStream();
    }

    /**
     * UPDATED: Takes email to delete from the secure path
     */
    public String delete(String fileName, String userEmail) {
        String fullPath = userEmail + "/" + fileName;
        s3Template.deleteObject(bucketName, fullPath);
        return "Deleted " + fileName;
    }
}