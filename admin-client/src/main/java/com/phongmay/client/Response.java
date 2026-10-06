package com.phongmay.client;

import java.util.UUID;

import com.phongmay.common.JsonUtil;

public class Response {
    // requestId tương ứng với request đã gửi để server biết phản hồi cho yêu cầu nào.
    private String requestId;
    // true nếu lệnh thực thi thành công, false nếu lỗi hoặc không hỗ trợ.
    private boolean success;
    // Thông điệp mô tả kết quả, ví dụ: "Login thành công".
    private String message;
    // Dữ liệu trả về bổ sung, ví dụ danh sách máy, thông tin hệ thống, ...
    private Object data;

    // Constructor mặc định dùng khi tạo phản hồi nhanh và cần sinh requestId mới.
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

    // Chuyển response client về model chung dùng trong protocol.
    public com.phongmay.common.Response toCommonResponse() {
        return new com.phongmay.common.Response(requestId, success, message, data);
    }

    // Serialize response ra JSON để gửi qua socket.
    public String toJson() {
        return JsonUtil.responseToJson(toCommonResponse());
    }

    // Chuyển ngược từ model chung sang response client.
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

    // Parse chuỗi JSON nhận từ server thành đối tượng Response.
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
