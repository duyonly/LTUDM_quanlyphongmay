package com.phongmay.cilent;

import java.util.UUID;

import com.phongmay.common.CommandType;
import com.phongmay.common.JsonUtil;
import com.phongmay.common.Message;

public class Request {
    // Mã định danh duy nhất cho mỗi yêu cầu gửi lên server.
    private String requestId;
    // Loại hành động cần thực thi: LOGIN, GET_SYSTEM_INFO, MESSAGE, ...
    private CommandType command;
    // Máy đích nhận lệnh hoặc máy cần thao tác.
    private String target;
    // Dữ liệu phụ của lệnh, ví dụ tham số hoặc payload JSON.
    private Object data;

    // Constructor mặc định dùng để sinh requestId mới.
    public Request() {
        this.requestId = UUID.randomUUID().toString();
    }

    public Request(CommandType command, String target, Object data) {
        this(UUID.randomUUID().toString(), command, target, data);
    }

    public Request(String requestId, CommandType command, String target, Object data) {
        this.requestId = requestId == null || requestId.isBlank()
                ? UUID.randomUUID().toString()
                : requestId;
        this.command = command;
        this.target = target;
        this.data = data;
    }

    // Chuyển request client thành đối tượng Message dùng chung trong protocol.
    public Message toMessage() {
        return new Message(requestId, command, target, data);
    }

    // Chuyển request thành chuỗi JSON để gửi qua socket.
    public String toJson() {
        return JsonUtil.messageToJson(toMessage());
    }

    // Chuyển ngược từ object Message chung về Request client.
    public static Request fromMessage(Message message) {
        if (message == null) {
            return null;
        }
        return new Request(
                message.getRequestId(),
                message.getCommand(),
                message.getTarget(),
                message.getData()
        );
    }

    // Parse JSON nhận được từ server trở lại thành Request.
    public static Request fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return fromMessage(JsonUtil.jsonToMessage(json));
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null || requestId.isBlank()
                ? UUID.randomUUID().toString()
                : requestId;
    }

    public CommandType getCommand() {
        return command;
    }

    public void setCommand(CommandType command) {
        this.command = command;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
