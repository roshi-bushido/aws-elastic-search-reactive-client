package com.roshi.reactive.es;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.roshi.reactive.es.client.ElasticSearchClient;
import com.roshi.reactive.es.client.ElasticSearchConfiguration;
import com.roshi.reactive.es.client.ElasticSearchRequest;
import com.roshi.reactive.es.client.ElasticSearchResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ReactiveElasticSearchApplicationTests {

  private static String ES_HOST;
  private static String ES_REGION;
  private static String AWS_ACCESS_KEY;
  private static String AWS_ACCESS_SECRET;

  @BeforeClass
  public static void loadCredentials() {
    try (InputStream input = ReactiveElasticSearchApplicationTests.class.getClassLoader()
        .getResourceAsStream("credentials.properties")) {
      Properties properties = new Properties();
      properties.load(input);
      ES_HOST = properties.getProperty("ES_HOST");
      ES_REGION = properties.getProperty("ES_REGION");
      AWS_ACCESS_KEY = properties.getProperty("AWS_ACCESS_KEY");
      AWS_ACCESS_SECRET = properties.getProperty("AWS_ACCESS_SECRET");
    } catch (IOException ex) {
    }
  }


  private ElasticSearchConfiguration configuration() {
    return new ElasticSearchConfiguration(ES_HOST, ES_REGION, AWS_ACCESS_KEY, AWS_ACCESS_SECRET)
        .setDebugMode(true);
  }

  private String endpoint(String s) {
    return String.format("%s/%s", ES_HOST, s);
  }

  @Test
  public void shouldTestGETEndpoint() {
    ElasticSearchClient client = new ElasticSearchClient(configuration());
    ElasticSearchRequest request = new ElasticSearchRequest()
        .setEndpointAsString(endpoint("_cat/indices"))
        .setMethod(HttpMethod.GET);

    Mono<ElasticSearchResponse> responseMono = client.execute(request);

    StepVerifier.create(responseMono)
        .consumeNextWith(response -> assertThat(response.getStatus(), equalTo(200)))
        .verifyComplete();
  }

  @Test
  public void shouldTestPOSTEndpoint() {
    ElasticSearchClient client = new ElasticSearchClient(configuration());
    ElasticSearchRequest request = new ElasticSearchRequest()
        .setBodyAsString("{\"size\": 1}")
        .setEndpointAsString(endpoint("places/_search"))
        .setMethod(HttpMethod.POST);

    Mono<ElasticSearchResponse> responseMono = client.execute(request);

    StepVerifier.create(responseMono)
        .consumeNextWith(response -> {
          assertThat(response.getStatus(), equalTo(200));
          assertThat(response.getBodyAsString(), notNullValue());
        })
        .verifyComplete();
  }

}