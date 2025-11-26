package io.equalink.pricekeep.service.pricefetch.graphql;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLError {
    private String message;
    private List<Object> path;
    private Map<String, Object> extensions;
    private String errorType;
    private String classification;
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<Object> getPath() {
        return path;
    }
    
    public void setPath(List<Object> path) {
        this.path = path;
    }
    
    public Map<String, Object> getExtensions() {
        return extensions;
    }
    
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
    
    public String getErrorType() {
        return errorType;
    }
    
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
    
    public String getClassification() {
        return classification;
    }
    
    public void setClassification(String classification) {
        this.classification = classification;
    }
}