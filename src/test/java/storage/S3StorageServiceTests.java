package webencoder.storage;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import java.io.IOException;
import org.springframework.boot.test.context.SpringBootTest;

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
    public void testLoad() throws IOException {
        Resource file = service.load("test.txt", "test_input");
        InputStream output_stream = file.getInputStream();
        String output = IOUtils.toString(output_stream, StandardCharsets.UTF_8);
        assertTrue("Output not equal to test file", output.equals("test file\n"));
    }

    @Test(expected = AmazonS3Exception.class)
    public void testLoadWrongFile() throws IOException{
        Resource file = service.load("test2.txt", "test_input");
    }

    @Test
    public void testStore() {
      MockMultipartFile mockMultipartFile = new MockMultipartFile(
       "test.txt",                //filename
       "Hallo World".getBytes()); //content
       service.store(mockMultipartFile, "test_output");
    }

    @Test
    public void testPath() {
        System.out.println("KEY:");
        System.out.println(s3_properties.getKey());

        String path = service.path("sample.dv", "test_input");
        assertEquals(path, "https://s3-sa-east-1.amazonaws.com/webencoder/test_input/sample.dv");
    }

}
