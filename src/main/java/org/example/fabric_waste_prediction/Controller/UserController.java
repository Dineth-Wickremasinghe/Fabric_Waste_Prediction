
package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.Entity.user;
import org.example.fabric_waste_prediction.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ── Login page ────────────────────────────────────────────────────────────
    @GetMapping("/user/login")
    public String userLoginPage() {
        return "user-login";
    }

    // ── Home page — shown after login for all roles ───────────────────────────
    @GetMapping("/user/home")
    public String userHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        user currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElse(null);
        model.addAttribute("user", currentUser);
        return "user-home";
    }

    // ── Dashboard — redirects to home ─────────────────────────────────────────
    @GetMapping("/user/dashboard")
    public String userDashboard() {
        return "redirect:/user/home";
    }
}
