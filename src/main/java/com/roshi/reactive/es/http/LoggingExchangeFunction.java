package com.roshi.reactive.es.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class LoggingExchangeFunction implements ExchangeFilterFunction {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction next) {
    logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
    clientRequest.headers()
        .forEach((name, values) -> values.forEach(value -> logger.info("{}={}", name, value)));
    return next.exchange(clientRequest);
  }
}
