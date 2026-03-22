package org.example.fabric_waste_prediction.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.fabric_waste_prediction.Entity.CuttingJob;
// REMOVED: ModelPerformance import
import org.example.fabric_waste_prediction.Service.HistoricalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/historical")
public class HistoricalDataController {

    @Autowired
    private HistoricalDataService historicalDataService;

    @GetMapping
    public String historicalDashboard(HttpSession session, Model model,
                                      @RequestParam(defaultValue = "cutting") String tab) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("cuttingJobs", historicalDataService.getAllCuttingJobs());
        model.addAttribute("activeTab", tab);
        return "historical-data";
    }

    @PostMapping("/cutting/update/{id}")
    public String updateCuttingJob(@PathVariable Long id,
                                   @ModelAttribute CuttingJob cuttingJob,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";
        String result = historicalDataService.updateCuttingJob(id, cuttingJob);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "Cutting record updated successfully!");
        }
        return "redirect:/user/historical?tab=cutting";
    }

    @PostMapping("/cutting/delete/{id}")
    public String deleteCuttingJob(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";
        historicalDataService.deleteCuttingJob(id);
        redirectAttributes.addFlashAttribute("successMsg", "Cutting record deleted successfully!");
        return "redirect:/user/historical?tab=cutting";
    }

}