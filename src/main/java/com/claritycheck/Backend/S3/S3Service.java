package com.claritycheck.Backend.S3;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3Service {

    private final S3Template s3Template;

    public S3Service(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public String upload(MultipartFile file) throws IOException{
        String key = file.getOriginalFilename();
        s3Template.upload(bucketName, key, file.getInputStream());
        return "Uploaded " + key;
    }

    public InputStream download(String key) throws IOException{
        S3Resource s3Resource = s3Template.download(bucketName, key);
        return s3Resource.getInputStream();
    }

    public String delete(String key){
        s3Template.deleteObject(bucketName, key);
        return "Deleted " + key;
    }
}
