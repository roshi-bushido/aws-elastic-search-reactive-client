package com.roshi.reactive.es.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.roshi.reactive.es.jackson.ObjectMapperUtils;
import java.net.URI;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;

/**
 * @author Matias Suarez on 2019-08-14.
 */
@Data @Accessors(chain = true)
public class ElasticSearchRequest {
  private URI endpoint;
  private HttpMethod method;
  private Map<String,Object> queryParams;
  private Map<String,Object> headers;
  private JsonNode body;

  public ElasticSearchRequest setBodyAsString(String string) {
    this.body = ObjectMapperUtils.parse(string, JsonNode.class);
    return this;
  }

  public ElasticSearchRequest setEndpointAsString(String url) {
    this.endpoint = URI.create(url);
    return this;
  }

}
