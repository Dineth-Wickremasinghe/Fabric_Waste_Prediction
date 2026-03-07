package org.example.fabric_waste_prediction.controller;

import org.example.fabric_waste_prediction.entity.user;
import org.example.fabric_waste_prediction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path="/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping(path="/add")
    public @ResponseBody String addNewUser(@RequestParam String username, @RequestParam String password) {
        user newUser = new user();

        newUser.setUsername(username);
        newUser.setPassword(password);
        userRepository.save(newUser);
        return "User added successfully";
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<user> getAllUsers() {
        return userRepository.findAll();
    }

}


