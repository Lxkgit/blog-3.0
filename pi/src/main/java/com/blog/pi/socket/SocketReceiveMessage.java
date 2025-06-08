package com.blog.pi.socket;

import jakarta.websocket.Session;
import lombok.Data;

@Data
public class SocketReceiveMessage {

    private String type;
    private String id;
    private Session session;
    private String message;

    public SocketReceiveMessage(String type, String id, Session session, String message) {
        this.type = type;
        this.id = id;
        this.session = session;
        this.message = message;
    }
}
