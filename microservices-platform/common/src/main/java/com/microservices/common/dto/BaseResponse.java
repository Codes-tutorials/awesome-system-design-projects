package com.microservices.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private LocalDateTime timestamp;
    
    public BaseResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public BaseResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public BaseResponse(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }
    
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "Success", data);
    }
    
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data);
    }
    
    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(false, message);
    }
    
    public static <T> BaseResponse<T> error(String message, String errorCode) {
        BaseResponse<T> response = new BaseResponse<>(false, message);
        response.setErrorCode(errorCode);
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}