package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.Entity.user;
import org.example.fabric_waste_prediction.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // ── Login page — Spring Security handles the actual login ─────────────────
    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin-login";
    }

    // ── Logout — handled by Spring Security via /admin/logout ─────────────────
    // No need for a logout method — SecurityConfig handles it

    // ── Dashboard ─────────────────────────────────────────────────────────────
    // No session check needed — Spring Security protects this route
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-dashboard";
    }

    // ── Create User ───────────────────────────────────────────────────────────
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute user user,
                             RedirectAttributes redirectAttributes) {
        String result = userService.createUser(user);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "User created successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    // ── Get User by ID (JSON) ─────────────────────────────────────────────────
    @GetMapping("/users/{id}")
    @ResponseBody
    public user getUser(@PathVariable Long id) {
        return userService.getUserById(id).orElse(null);
    }

    // ── Update User ───────────────────────────────────────────────────────────
    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute user user,
                             RedirectAttributes redirectAttributes) {
        String result = userService.updateUser(id, user);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "User updated successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    // ── Delete User ───────────────────────────────────────────────────────────
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMsg", "User deleted successfully!");
        return "redirect:/admin/dashboard";
    }
}