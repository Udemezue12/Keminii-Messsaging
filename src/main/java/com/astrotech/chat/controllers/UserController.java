package com.astrotech.chat.controllers;

import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.Status;
import com.astrotech.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;



    @GetMapping("/users")
    public ResponseEntity<List<User>> getConnectedUsers() {
        var user = userService.findConnectedUsers(Status.ONLINE);
        return ResponseEntity.ok(user);
    }


}
