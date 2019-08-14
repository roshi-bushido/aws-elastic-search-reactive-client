package com.roshi.reactive.es.client;

import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Mono;

/**
 * @author Matias Suarez on 2019-08-14.
 */
@Data @Accessors(chain = true)
public class ElasticSearchResponse {
  private Map<String,String> headers;
  private int status;
  private String bodyAsString;
}
