package com.phongmay.common;

public class Response {
    private boolean success;
    private String requestId;
    private String message;
    private Object data;

    public Response() {

    }
    public Response(String requestId,boolean success, String message,Object data){
        this.requestId = requestId;
        this.success = success;
        this.message = message;
        this.data = data;
    }
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
