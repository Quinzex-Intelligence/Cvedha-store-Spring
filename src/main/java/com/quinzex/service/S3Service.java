package com.quinzex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.bucketName}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size should not exceed 5MB");
        }
    String contentType = file.getContentType();
        if(contentType==null  || !contentType.startsWith("image/")){
            throw new RuntimeException("Only image files are allowed");
        }
        String fileName = "ebooks/covers/" + UUID.randomUUID() +
                "_" + file.getOriginalFilename();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();
        s3Client.putObject(objectRequest, RequestBody.fromBytes(file.getBytes()));

        return fileName;
    }
    public String uploadFilePDF(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }
        String contentType = file.getContentType();
        String fileName = "ebooks/pdf/" + UUID.randomUUID() +
                "_" + file.getOriginalFilename();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();
        s3Client.putObject(objectRequest, RequestBody.fromBytes(file.getBytes()));

        return fileName;
    }

    public String generatePresignedUrl(String key){
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest).build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedGetObjectRequest.url().toString();
    }

    public void uploadFileWithKey(MultipartFile file, String key) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

          if(file.getSize() > 5 * 1024 * 1024) {
              throw new RuntimeException("File size should not exceed 5MB");
          }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key) // same key → overwrite
                .contentType(contentType)
                .build();

        s3Client.putObject(request,
                RequestBody.fromBytes(file.getBytes()));
    }
    public void uploadPdfFileWithKey(MultipartFile file, String key) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }



        String contentType = file.getContentType();



        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key) // same key → overwrite
                .contentType(contentType)
                .build();

        s3Client.putObject(request,
                RequestBody.fromBytes(file.getBytes()));
    }
    public InputStream downloadFile(String key) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        return s3Client.getObject(request);
    }
}
