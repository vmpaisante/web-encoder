package webencoder.storage;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.Resource;

import static org.junit.Assert.*;

public class S3StorageServiceTests {

    private StorageProperties properties = new StorageProperties();
    private S3StorageProperties s3_properties = new S3StorageProperties();
    private S3StorageService service;

    @Before
    public void init() {
        properties.setLocation("https://s3-sa-east-1.amazonaws.com");
        s3_properties.setBucketName("webencoder");
        s3_properties.setKey("AKIAI4EAPB2WC6JOBKDA");
        s3_properties.setPrivateKey("0Q7z0RJg41OuOjpPQqZIdE2R+gZZThHk8NhkcUjv");
        s3_properties.setRegion("sa-east-1");
        service = new S3StorageService(properties, s3_properties);
        service.init();
    }

    @Test
    public void testPath() {
        String path = service.path("sample.dv", "test_input");
        assertEquals(path, "https://s3-sa-east-1.amazonaws.com/webencoder/test_input/sample.dv");
    }

}
