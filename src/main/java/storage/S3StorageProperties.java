package webencoder.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage.s3")
public class S3StorageProperties {

  private String bucket_name = "unknown";
  private String key = "unknown";
  private String private_key = "unknown";

  public String getKey() {
    return this.key;
  }
  public void setKey(String name) {
    this.key = name;
  }

  public String getPrivateKey() {
    return this.private_key;
  }

  public void setPrivateKey(String name) {
    this.private_key = name;
  }

  public String getBucketName() {
    return this.bucket_name;
  }

  public void setBucketName(String name) {
    this.bucket_name = name;
  }

}
