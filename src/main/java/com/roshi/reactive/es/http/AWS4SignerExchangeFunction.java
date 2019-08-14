package com.roshi.reactive.es.http;

import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.Signer;
import com.amazonaws.http.HttpMethodName;
import com.roshi.reactive.es.client.ElasticSearchRequest;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.http.Header;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * An {@link HttpRequestInterceptor} that signs requests using any AWS {@link Signer} and {@link
 * AWSCredentialsProvider}.
 */
public class AWS4SignerExchangeFunction implements ExchangeFilterFunction {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * The service that we're connecting to. Technically not necessary. Could be used by a future
   * Signer, though.
   */
  private final String service;
  private final ElasticSearchRequest sourceRequest;

  /**
   * The particular signer implementation.
   */
  private AWS4Signer signer;

  /**
   * The source of AWS credentials for signing.
   */
  private final AWSCredentialsProvider awsCredentialsProvider;

  /**
   * @param service service that we're connecting to
   * @param awsCredentialsProvider source of AWS credentials for signing
   */
  public AWS4SignerExchangeFunction(final String service, final String region,
      final AWSCredentialsProvider awsCredentialsProvider, ElasticSearchRequest sourceRequest) {
    this.service = service;
    this.awsCredentialsProvider = awsCredentialsProvider;
    this.signer = new AWS4Signer();
    this.signer.setServiceName(this.service);
    this.signer.setRegionName(region);
    this.sourceRequest = sourceRequest;
  }

  /**
   * @param params list of HTTP query params as NameValuePairs
   * @return a multimap of HTTP query params
   */
  private static Map<String, List<String>> nvpToMapParams(final List<NameValuePair> params) {
    Map<String, List<String>> parameterMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (NameValuePair nvp : params) {
      List<String> argsList =
          parameterMap.computeIfAbsent(nvp.getName(), k -> new ArrayList<>());
      argsList.add(nvp.getValue());
    }
    return parameterMap;
  }

  /**
   * @param headers modeled Header objects
   * @return a Map of header entries
   */
  private static Map<String, String> headerArrayToMap(final HttpHeaders headers) {
    Map<String, String> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (Entry<String, List<String>> rawHeader : headers.entrySet()) {
      Header header = new BasicHeader(rawHeader.getKey(), rawHeader.getValue().get(0));
      if (!skipHeader(header)) {
        headersMap.put(header.getName(), header.getValue());
      }
    }
    return headersMap;
  }

  /**
   * @param header header line to check
   * @return true if the given header should be excluded when signing
   */
  private static boolean skipHeader(final Header header) {
    return ("content-length" .equalsIgnoreCase(header.getName())
        && "0" .equals(header.getValue())) // Strip Content-Length: 0
        || "host" .equalsIgnoreCase(header.getName()); // Host comes from endpoint
  }

  /**
   * @param mapHeaders Map of header entries
   * @return modeled Header objects
   */
  private static Header[] mapToHeaderArray(final Map<String, String> mapHeaders) {
    Header[] headers = new Header[mapHeaders.size()];
    int i = 0;
    for (Map.Entry<String, String> headerEntry : mapHeaders.entrySet()) {
      headers[i++] = new BasicHeader(headerEntry.getKey(), headerEntry.getValue());
    }
    return headers;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest clientRequest,
      ExchangeFunction exchangeFunction) {

    URIBuilder uriBuilder;
    uriBuilder = new URIBuilder(clientRequest.url());

    // Copy Apache HttpRequest to AWS DefaultRequest
    DefaultRequest<?> signableRequest = new DefaultRequest<>(service);
    signableRequest.setHttpMethod(HttpMethodName.fromValue(clientRequest.method().name()));
    signableRequest.setParameters(nvpToMapParams(uriBuilder.getQueryParams()));
    signableRequest.setHeaders(headerArrayToMap(clientRequest.headers()));

    if (clientRequest.method().equals(HttpMethod.POST)) {
      signableRequest.setContent(new ByteArrayInputStream(sourceRequest.getBody().toString().getBytes()));
    }

    try {
      URI url = uriBuilder.build();
      signableRequest.setResourcePath(url.getRawPath());
      signableRequest.setEndpoint(URI.create(url.toString().replace(url.getRawPath(), "")));
    } catch (URISyntaxException e) {
      logger.error("Error building endpoint with cause {}", e.getCause(), e);
    }

    signer.sign(signableRequest, awsCredentialsProvider.getCredentials());

    // Now copy everything back
    ClientRequest.Builder filtered = ClientRequest
        .from(clientRequest);
    for (Header signedHeader : mapToHeaderArray(signableRequest.getHeaders())) {
      filtered.header(signedHeader.getName(), signedHeader.getValue());
    }

    return exchangeFunction.exchange(filtered.build());
  }
}
