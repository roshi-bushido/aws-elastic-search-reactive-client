package com.roshi.reactive.es.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Matias Suarez on 2019-01-22.
 */
public class ObjectMapperUtils {

  private static ObjectMapper mapper = new ObjectMapper();

  public static Optional<JsonNode> read(final String content) {
    try {
      return Optional.of(ObjectMapperUtils.mapper.readTree(content));
    } catch (final IOException e) {
      return Optional.empty();
    }
  }

  public static String toString(final Object content) {
    try {
      return ObjectMapperUtils.mapper.writeValueAsString(content);
    } catch (final IOException e) {
      return "";
    }
  }

  public static JsonNode toJsonNode(final Object content) {
    try {
      return ObjectMapperUtils.mapper.readTree(ObjectMapperUtils.mapper.writeValueAsBytes(content));
    } catch (final IOException e) {
      throw new RuntimeException("Cannot parse content to JsonNode");
    }
  }

  public static <T> T parse(final String value, final Class<T> clazz) {
    try {
      return ObjectMapperUtils.mapper.readValue(value, clazz);
    } catch (final IOException e) {
      return null;
    }
  }

  public static ArrayNode toArray(final Collection<?> o) {
    return ObjectMapperUtils.mapper.valueToTree(o);
  }

  public static <T> Collection<T> parseList(final String value, final Class<T> clazz) {
    try {
      final TypeFactory typeFactory = ObjectMapperUtils.mapper.getTypeFactory();
      return ObjectMapperUtils.mapper
          .readValue(value, typeFactory.constructCollectionType(List.class, clazz));
    } catch (final IOException e) {
      return null;
    }
  }

  public static String getStringNode(final JsonNode node, final String prop, final String defaultValue) {
    if (node.get(prop) != null) {
      return node.get(prop).asText();
    }
    return defaultValue;
  }

  public static ObjectNode createObject() {
    return ObjectMapperUtils.mapper.createObjectNode();
  }

  public static ArrayNode createArray() {
    return ObjectMapperUtils.mapper.createArrayNode();
  }
}
