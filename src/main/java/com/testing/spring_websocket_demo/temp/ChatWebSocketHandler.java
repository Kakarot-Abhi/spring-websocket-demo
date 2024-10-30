package com.testing.spring_websocket_demo.temp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Store sessions and connection IDs
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> userIds = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Assign a unique connection ID to the user
        String connectionId = session.getId();
        sessions.put(connectionId, session);
        userIds.put(session.getId(), connectionId);

        ;

        // Notify the user of their connection ID
        session.sendMessage(new TextMessage(getStrFromMap(Map.of("messages", Map.of("userId", connectionId)))));

        // Broadcast the updated user list to all connected users
        broadcastConnectedUsers();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Parse the incoming JSON message
        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String recipientId = payload.get("recipientId");
        String content = payload.get("content");

        // Get the sender's ID
        String senderId = session.getId();

        // Construct the message with sender's info
        String messageWithSender = objectMapper.writeValueAsString(Map.of(
            "senderId", senderId,
            "content", content
        ));

        // Send the message to the recipient
        WebSocketSession recipientSession = sessions.get(recipientId);
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage(messageWithSender));
        } else {
            session.sendMessage(new TextMessage("User not connected: " + recipientId));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the user from sessions and notify others
        String connectionId = session.getId();
        sessions.remove(connectionId);
        userIds.remove(connectionId);
        broadcastConnectedUsers();
    }

    private void broadcastConnectedUsers() {
        try {
            String connectedUsers = objectMapper.writeValueAsString(userIds.values());
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(getStrFromMap(Map.of("messages", Map.of("users", connectedUsers)))));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public String getStrFromMap(Map map){
        return objectMapper.writeValueAsString(map);
    }
}
