package webencoder.storage;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import webencoder.storage.S3StorageProperties;
import webencoder.Application;

/*
Storage service that uses Amazon's S3 as a storage medium.

It implements all StorageService interface using only S3, inserting and
reading from it. It uses ConfigurationProperties to store sensitive information
like access key and bucket region.
*/
@Service
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageService implements StorageService {

    private final AmazonS3 s3client;
    private final StorageProperties storage_properties;
	  private final S3StorageProperties s3_storage_properties;

    /*
    Class constructor.

    It sets up the amazon s3 client for further uinterface calls.
    */
    @Autowired
    public S3StorageService(
        StorageProperties properties, S3StorageProperties s3_properties
    ) {
        this.storage_properties = properties;
        this.s3_storage_properties = s3_properties;

        AWSCredentials credentials = new BasicAWSCredentials(
                    this.s3_storage_properties.getKey(),
                    this.s3_storage_properties.getPrivateKey());

        // create a client connection based on credentials
        this.s3client = new AmazonS3Client(credentials);
    }

    /*
    This storage service does not require initial computation.
    */
    @Override
    public void init() {}

    /*
    Store the MultipartFile file into Amazon's S3 storage.

    It uses path to resolve the file's folder inside S3
    */
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
            Application.logger.info("Stored "+fileName+" into S3");
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    /*
    Load and returns as resource the file within the path folder in S3.
    */
    @Override
    public Resource load(String filename, String path) {
        S3Object s3_file = this.s3client.getObject(
            this.s3_storage_properties.getBucketName(), path + "/" + filename
        );
        S3ObjectInputStream file_stream = s3_file.getObjectContent();

        Resource resource = new InputStreamResource(file_stream);
        if (resource.exists() || resource.isReadable()) {
            Application.logger.info("Loaded "+path + "/" + filename+" into S3");
            return resource;
        }
        else {
            throw new StorageFileNotFoundException(
                    "Could not read file: " + filename);
        }
    }

    @Override
    /*
    Return S3 path for the file name and path received.
    */
    public String path(String filename, String local_path) {
        String root_location = this.storage_properties.getLocation();
        return root_location + "/" + this.s3_storage_properties.getBucketName() + "/" + local_path + "/" + filename;
    }
}
