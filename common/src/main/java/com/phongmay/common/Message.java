package com.phongmay.common;

public class Message {
    private String type;
    private String requestId;
    private CommandType command;
    private String target;
    private Object data;

    public Message() {
    }
}
