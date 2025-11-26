package io.equalink.pricekeep.service.pricefetch.graphql;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLResponse<T> {
    private T data;
    private List<GraphQLError> errors;
    private Map<String, Object> extensions;
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public List<GraphQLError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<GraphQLError> errors) {
        this.errors = errors;
    }
    
    public Map<String, Object> getExtensions() {
        return extensions;
    }
    
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
    
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
