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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin-login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        System.out.println("ADMIN LOGIN ATTEMPT: " + username);

        // Ensure the third parameter is 'true' for Admin authentication
        Optional<user> admin = userService.authenticate(username, password, true);

        System.out.println("AUTH RESULT: " + admin.isPresent());

        if (admin.isPresent()) {
            session.setAttribute("adminUser", admin.get());
            session.setAttribute("adminLoggedIn", true);
            return "redirect:/admin/dashboard";
        }

        // Changed to "error" to match your HTML: th:if="${error}"
        redirectAttributes.addFlashAttribute("error", "Invalid admin username or password.");
        return "redirect:/admin/login";
    }

    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("users", userService.getAllUsers());
        return "admin-dashboard";
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute user user,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }
        String result = userService.createUser(user);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "User created successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users/{id}")
    @ResponseBody
    public user getUser(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminUser") == null) return null;
        return userService.getUserById(id).orElse(null);
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute user user,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }
        String result = userService.updateUser(id, user);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "User updated successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMsg", "User deleted successfully!");
        return "redirect:/admin/dashboard";
    }
}