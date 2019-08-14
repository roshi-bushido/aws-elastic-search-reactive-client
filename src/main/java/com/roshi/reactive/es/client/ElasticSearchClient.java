package com.roshi.reactive.es.client;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.roshi.reactive.es.http.AWS4SignerExchangeFunction;
import com.roshi.reactive.es.http.LoggingExchangeFunction;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import reactor.core.publisher.Mono;

/**
 * @author Matias Suarez on 2019-08-14.
 */
public class ElasticSearchClient {

  private static final String SERVICE_NAME = "es";
  private final ElasticSearchConfiguration configuration;

  public ElasticSearchClient(ElasticSearchConfiguration configuration) {
    this.configuration = configuration;
  }

  private String baseUrl(URI uri) {
    return uri.toString().replace(uri.getRawPath(), "");
  }

  public Mono<ElasticSearchResponse> execute(ElasticSearchRequest request) {
    AWSCredentialsProvider credentials = configuration.awsCredentialsProvider();
    WebClient.Builder client = WebClient.builder()
        .baseUrl(baseUrl(request.getEndpoint()))
        .filter(new AWS4SignerExchangeFunction(SERVICE_NAME, configuration.getRegion(),
            credentials, request));
    if (configuration.isDebugMode()) {
      client.filter(new LoggingExchangeFunction());
    }

    final RequestBodyUriSpec requestSpec = client.build().method(request.getMethod());
    if (request.getBody() != null) {
      requestSpec.body(BodyInserters.fromObject(request.getBody()));
    }

    Mono<ClientResponse> rMono = requestSpec
        .uri(request.getEndpoint().getRawPath())
        .exchange();
    return rMono.flatMap(response -> response.toEntity(String.class))
        .map(this::toElasticSearchResponse);
  }

  private ElasticSearchResponse toElasticSearchResponse(ResponseEntity<String> r) {
    return new ElasticSearchResponse()
        .setStatus(r.getStatusCodeValue())
        .setHeaders(r.getHeaders().toSingleValueMap())
        .setBodyAsString(r.getBody());
  }

}
