package com.testing.spring_websocket_demo.temp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/connected-users")
@CrossOrigin(origins = "*") // Enable CORS for all origins
public class UserController {

    @Autowired
    ChatWebSocketHandler socketHandler;

    @GetMapping
    public List<String> getUsers(){
        return List.of("I am connected");
    }
}
