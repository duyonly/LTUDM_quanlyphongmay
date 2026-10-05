package com.phongmay.cilent;

import java.util.UUID;

import com.phongmay.common.CommandType;
import com.phongmay.common.JsonUtil;
import com.phongmay.common.Message;

public class Request {
    private String requestId;
    private CommandType command;
    private String target;
    private Object data;

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

    public Message toMessage() {
        return new Message(requestId, command, target, data);
    }

    public String toJson() {
        return JsonUtil.messageToJson(toMessage());
    }

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
