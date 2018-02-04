package webencoder.storage;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import webencoder.storage.S3StorageProperties;

@Service
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageService implements StorageService {

    private final AmazonS3 s3client;
    private final StorageProperties storage_properties;
	private final S3StorageProperties s3_storage_properties;

    @Autowired
    public S3StorageService(StorageProperties properties, S3StorageProperties s3_properties) {
        this.storage_properties = properties;
        this.s3_storage_properties = s3_properties;

        AWSCredentials credentials = new BasicAWSCredentials(
                    this.s3_storage_properties.getKey(),
                    this.s3_storage_properties.getPrivateKey());

        // create a client connection based on credentials
        this.s3client = new AmazonS3Client(credentials);
    }

    @Override
    public void init() {}

    @Override
    public void store(MultipartFile file, String path) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }

    		// upload file to s3
    		String fileName = path + "/" + filename;
    		this.s3client.putObject(new PutObjectRequest(this.s3_storage_properties.getBucketName(), fileName,
    				file.getInputStream(), new ObjectMetadata())
                    .withCannedAcl(CannedAccessControlList.PublicRead));

        }
        catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Resource load(String filename) {
        S3Object s3_file = this.s3client.getObject(this.s3_storage_properties.getBucketName(), filename);
        S3ObjectInputStream file_stream = s3_file.getObjectContent();

        Resource resource = new InputStreamResource(file_stream);
        if (resource.exists() || resource.isReadable()) {
            return resource;
        }
        else {
            throw new StorageFileNotFoundException(
                    "Could not read file: " + filename);

        }
    }

    @Override
    public String path(String filename) {
        String root_location = this.storage_properties.getLocation();
        return root_location + "/" + this.s3_storage_properties.getBucketName() + "/" + filename;
    }
}
