package com.phongmay.common;

public class Message {
    private String requestId;
    private CommandType command;
    private String target;
    private Object data;

    public Message() {
    }
    public Message(String requestId, CommandType command,String target, Object data){
        
        this.requestId = requestId;
        this.command = command;
        this.target = target;
        this.data = data;
    }
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
