package webencoder.storage;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

public class S3StorageServiceTests {

    private StorageProperties properties = new StorageProperties();
    private S3StorageProperties s3_properties = new S3StorageProperties();
    private S3StorageService service;

    @Before
    public void init() {
        properties.setLocation("target/files/" + Math.abs(new Random().nextLong()));
        service = new S3StorageService(properties, s3_properties);
        service.init();
    }
}
