package webencoder.encoding;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.lang.StringBuffer;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import webencoder.Application;
import webencoder.encoding.EncodingException;
import webencoder.encoding.ZencoderProperties;
import webencoder.storage.S3StorageProperties;

/*
Class that provides the encoding service using Zencoder and S3.

Its encoding uses a file present in S3 and commands Zencoder to store
the encoded file in S3 as well.

It uses ConfigurationProperties to store data like keys and S3 information.
*/
@Service
@EnableConfigurationProperties(ZencoderProperties.class)
public class ZencoderS3Service implements EncodingService {

    private final ZencoderProperties zencoder_properties;
    private final S3StorageProperties s3_properties;

    @Autowired
    public ZencoderS3Service(ZencoderProperties properties, S3StorageProperties s3_properties) {
        this.zencoder_properties = properties;
        this.s3_properties = s3_properties;
    }

    @Override
    public void init() {}

    /*
    Create the JSON needed for encoding input file.

    params: string with input file with full path and string with output
            file with full path.
    return: Request JSON in string format.
    */
    private String createJSONRequestBody(
      String input,
      String output
    ) {
        try {
            JSONObject request_body = new JSONObject();
            // Set API key with full access to Zencoder service.
            request_body.put("api_key", zencoder_properties.getFullKey());
            // Set input url from Amazon AWS.
            request_body.put(
              "input",
              "s3+" + s3_properties.getRegion() + "://" +
                s3_properties.getBucketName() + input
            );
            // Set output configuration data
            JSONObject json_output = new JSONObject();
            json_output.put(
              "url",
              "s3+" + s3_properties.getRegion() + "://"
                + s3_properties.getBucketName() + output
            );
            json_output.put("public", "true");
            request_body.put("output", json_output);
            // return request JSON
            return request_body.toString();
        } catch(Exception e){
          throw new EncodingException("Error creating request body for encoding job.");
        }
    }

    /*
    Send POST request to run encoding job.

    params: Request body in JSON format.
    return: Request's response.
    */
    private String sendEncodeRequest(String request_body) {
      try {
        // Create connection.
        URL url = new URL(zencoder_properties.getZencoderJobUrl());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        // Setting Header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        // Setting output and Sending
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(request_body.toString());
        wr.flush();
        wr.close();
        // Get response code.
        int response_code = con.getResponseCode();
        //Log.
        Application.logger.info("\nSending 'POST' request to URL : " + url);
        Application.logger.info("Response Code : " + response_code);
        // Read response
        BufferedReader in = new BufferedReader(
        new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        // Return response in string format.
        Application.logger.info("RESPONSE:");
        Application.logger.info(response.toString());
        return response.toString();
      } catch(Exception e){
        throw new EncodingException("Error when sending Zencoder job.");
      }
    }

    /*
    Encode input file to web format and store it to output.

    params: Input file name and folder path in S3 and output file name and
            folder path in S3.
    return: String in JSON format with input id, output id and output url and
            the apropriate Zencoder read key.
    */
    @Override
    public String encode(
      String input_filename,
      String input_path,
      String output_filename,
      String output_path
     ) {
          // Create json request body.
          String request_body = createJSONRequestBody(
            "/" + input_path + "/" + input_filename,
            "/" + output_path + "/" + output_filename
          );
          // Log.
          Application.logger.info("JOB REQUEST:");
          Application.logger.info(request_body);
          // Create and send encoding request to Zencoder.
          String response = sendEncodeRequest(request_body);
          // Prepare return string.
          JSONObject response_JSON = new JSONObject(response);
          Object input_id = response_JSON.get("id");
          Object output_id = ((JSONObject)((JSONArray)response_JSON.get("outputs")).get(0)).get("id");
          Object output_url = ((JSONObject)((JSONArray)response_JSON.get("outputs")).get(0)).get("url");

          JSONObject return_JSON = new JSONObject();
          return_JSON.put("input_id", input_id);
          return_JSON.put("output_id", output_id);
          return_JSON.put("output_url", output_url);
          return_JSON.put("zencoder_key", zencoder_properties.getReadKey());
          // Return string
          return return_JSON.toString();
    }
}
