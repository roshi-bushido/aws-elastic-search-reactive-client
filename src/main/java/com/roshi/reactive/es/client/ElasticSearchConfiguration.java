package com.roshi.reactive.es.client;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import lombok.Getter;

/**
 * @author Matias Suarez on 2019-08-14.
 */
@Getter
public class ElasticSearchConfiguration {
  private String endpoint;
  private String region;
  private String accessKey;
  private String accessSecret;
  private boolean debugMode;

  public ElasticSearchConfiguration(String endpoint, String region, String accessKey, String accessSecret) {
    this.endpoint = endpoint;
    this.accessKey = accessKey;
    this.accessSecret = accessSecret;
    this.region = region;
  }

  public AWSCredentialsProvider awsCredentialsProvider() {
    return new AWSStaticCredentialsProvider(new BasicAWSCredentials(this.accessKey, this.accessSecret));
  }

  public boolean isDebugMode() {
    return debugMode;
  }

  public ElasticSearchConfiguration setDebugMode(boolean debugMode) {
    this.debugMode = debugMode;
    return this;
  }
}
