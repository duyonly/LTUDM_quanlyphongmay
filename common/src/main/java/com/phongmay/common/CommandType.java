package com.phongmay.common;

public enum CommandType {
    CONNECT,
    LOGIN,
    HEARTBEAT,
    GET_CLIENTS,

    GET_SYSTEM_INFO,
    GET_PROCESSES,
    KILL_PROCESS,

    SCREENSHOT,

    LOCK,
    UNLOCK,
    SHUTDOWN,
    LOGOUT,

    MESSAGE,
    BROADCAST,
    FILE,

    REMOTE_INPUT
}
