package webencoder.encoding;

import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONArray;


import webencoder.encoding.ZencoderProperties;
import webencoder.storage.S3StorageProperties;

import static org.junit.Assert.*;

public class ZencoderS3ServiceTests {

    private ZencoderProperties zencoder_properties = new ZencoderProperties();
    private S3StorageProperties s3_properties = new S3StorageProperties();
    private ZencoderS3Service service;


    @Before
    public void init() {
        s3_properties.setRegion("sa-east-1");
        zencoder_properties.setFullKey("c71c60e9f517820909b123f4bc2df246");
        zencoder_properties.setReadKey("58f8a2ce3004bd7aff1cef5daa0661d9");
        zencoder_properties.setZencoderJobUrl("https://app.zencoder.com/api/v2/jobs");
        this.service = new ZencoderS3Service(zencoder_properties, s3_properties);
        this.service.init();
    }

    @Test
    public void encodeRequestCorrect() {
        try {
            String response = service.encode("sample.dv", "test_input",
                                             "sample.webm", "test_output");
            JSONObject r_json = new JSONObject(response);

            assertTrue(r_json.has("input_id"));
            assertTrue(r_json.has("output_id"));
            assertTrue(r_json.has("output_url"));
            assertEquals(r_json.get("zencoder_key"), zencoder_properties.getReadKey());
        } catch (Exception e){
            fail(e.getMessage());
        }
    }

}
