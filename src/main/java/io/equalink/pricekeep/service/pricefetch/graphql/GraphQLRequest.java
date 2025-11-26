package io.equalink.pricekeep.service.pricefetch.graphql;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GraphQLRequest<V>(String query, Map<String, V> variables) {}
