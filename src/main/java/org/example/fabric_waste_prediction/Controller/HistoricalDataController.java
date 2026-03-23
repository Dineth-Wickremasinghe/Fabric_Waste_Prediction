package org.example.fabric_waste_prediction.Controller;

import jakarta.validation.Valid;
import org.example.fabric_waste_prediction.Entity.CuttingJob;
import org.example.fabric_waste_prediction.Service.HistoricalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/historical")
public class HistoricalDataController {

    @Autowired
    private HistoricalDataService historicalDataService;

    // ── Main Dashboard ────────────────────────────────────────────────────────
    @GetMapping
    public String historicalDashboard(Model model,
                                      @RequestParam(defaultValue = "cutting") String tab) {
        model.addAttribute("cuttingJobs", historicalDataService.getAllCuttingJobs());
        model.addAttribute("activeTab", tab);
        return "historical-data";
    }

    // ── Update Cutting Job ────────────────────────────────────────────────────
    @PostMapping("/cutting/update/{id}")
    public String updateCuttingJob(@PathVariable Long id,
                                   @Valid @ModelAttribute CuttingJob cuttingJob,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {

        // ✅ Backend validation check
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors()
                    .stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("errorMsg", errorMsg);
            return "redirect:/user/historical?tab=cutting";
        }

        String result = historicalDataService.updateCuttingJob(id, cuttingJob);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "Cutting record updated successfully!");
        }
        return "redirect:/user/historical?tab=cutting";
    }

    // ── Delete Cutting Job ────────────────────────────────────────────────────
    @PostMapping("/cutting/delete/{id}")
    public String deleteCuttingJob(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        historicalDataService.deleteCuttingJob(id);
        redirectAttributes.addFlashAttribute("successMsg", "Cutting record deleted successfully!");
        return "redirect:/user/historical?tab=cutting";
    }
}