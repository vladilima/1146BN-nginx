package br.univille.chat.interfaces.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/name")
    public String name() {
        return "Chat";
    }

    @GetMapping("/user")
    public String waiter() {
        return "Chat - User";
    }

    @GetMapping("/admin")
    public String customer() {
        return "Chat - Admin";
    }

}