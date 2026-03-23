package org.example.fabric_waste_prediction.Controller;

import jakarta.validation.Valid;
import org.example.fabric_waste_prediction.Entity.user;
import org.example.fabric_waste_prediction.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // ── Login page ────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin-login";
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-dashboard";
    }

    // ── Create User ───────────────────────────────────────────────────────────
    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute user user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        // ✅ Backend validation check
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors()
                    .stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("errorMsg", errorMsg);
            return "redirect:/admin/dashboard";
        }

        // ✅ Extra password length check for create
        // (password annotation can't be on entity since update allows blank password)
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Password is required.");
            return "redirect:/admin/dashboard";
        }
        if (user.getPassword().length() < 8) {
            redirectAttributes.addFlashAttribute("errorMsg", "Password must be at least 8 characters.");
            return "redirect:/admin/dashboard";
        }

        String result = userService.createUser(user);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "User created successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    // ── Get User by ID (JSON) — password hidden ───────────────────────────────
    @GetMapping("/users/{id}")
    @ResponseBody
    public user getUser(@PathVariable Long id) {
        return userService.getUserById(id).orElse(null);
    }

    // ── Update User ───────────────────────────────────────────────────────────
    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute user user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        // ✅ Backend validation check
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors()
                    .stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("errorMsg", errorMsg);
            return "redirect:/admin/dashboard";
        }

        // ✅ If password is provided, check minimum length
        if (user.getPassword() != null && !user.getPassword().isBlank()
                && user.getPassword().length() < 8) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Password must be at least 8 characters.");
            return "redirect:/admin/dashboard";
        }

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
        String result = userService.deleteUser(id);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "User deleted successfully!");
        }
        return "redirect:/admin/dashboard";
    }
}