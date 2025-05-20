package org.example.doancnttfinalproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class HomeController {
    @GetMapping("/home")
    public String index(Model model, OAuth2AuthenticationToken authentication){
        OAuth2User user = authentication.getPrincipal();
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        System.out.println(email);
        System.out.println(name);
        // Truyền dữ liệu sang view (HTML nếu dùng Thymeleaf)
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        return "index.html";
    }

}
