package com.phongmay.client;

import java.util.UUID;

import com.phongmay.common.JsonUtil;

public class Response {
    private String requestId;
    private boolean success;
    private String message;
    private Object data;

    public Response() {
        this.requestId = UUID.randomUUID().toString();
    }

    public Response(String requestId, boolean success, String message, Object data) {
        this.requestId = requestId == null || requestId.isBlank()
                ? UUID.randomUUID().toString()
                : requestId;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public com.phongmay.common.Response toCommonResponse() {
        return new com.phongmay.common.Response(requestId, success, message, data);
    }

    public String toJson() {
        return JsonUtil.responseToJson(toCommonResponse());
    }

    public static Response fromCommonResponse(com.phongmay.common.Response response) {
        if (response == null) {
            return null;
        }
        return new Response(
                response.getRequestId(),
                response.isSuccess(),
                response.getMessage(),
                response.getData()
        );
    }

    public static Response fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return fromCommonResponse(JsonUtil.jsonToResponse(json));
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null || requestId.isBlank()
                ? UUID.randomUUID().toString()
                : requestId;
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
