package org.example.fabric_waste_prediction.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.fabric_waste_prediction.Entity.user;
import org.example.fabric_waste_prediction.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String userLoginPage() {
        return "user-login";
    }

    @PostMapping("/login")
    public String userLogin(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Optional<user> user = userService.authenticate(username, password, false);
        if (user.isPresent()) {
            session.setAttribute("loggedInUser", user.get());
            redirectAttributes.addFlashAttribute("success", "Welcome, " + user.get().getFullName() + "! You have logged in successfully.");
            return "redirect:/user/dashboard";
        }
        redirectAttributes.addFlashAttribute("error", "Invalid username or password. Please try again.");
        return "redirect:/user/login";
    }

    @GetMapping("/dashboard")
    public String userDashboard(HttpSession session, Model model) {
        user user = (user) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("user", user);
        return "user-dashboard";
    }

    @GetMapping("/logout")
    public String userLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }
}