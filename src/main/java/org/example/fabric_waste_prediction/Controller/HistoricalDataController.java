package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.Entity.CuttingJob;
import org.example.fabric_waste_prediction.Entity.ModelPerformance;
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
    public String historicalDashboard(Model model,
                                      @RequestParam(defaultValue = "cutting") String tab) {
        model.addAttribute("cuttingJobs", historicalDataService.getAllCuttingJobs());
        model.addAttribute("modelPerformances", historicalDataService.getAllModelPerformance());
        model.addAttribute("activeTab", tab);
        return "historical-data";
    }

    @PostMapping("/cutting/update/{id}")
    public String updateCuttingJob(@PathVariable Long id,
                                   @ModelAttribute CuttingJob cuttingJob,
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
                                   RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";
        historicalDataService.deleteCuttingJob(id);
        redirectAttributes.addFlashAttribute("successMsg", "Cutting record deleted successfully!");
        return "redirect:/user/historical?tab=cutting";
    }

    @PostMapping("/performance/update/{id}")
    public String updateModelPerformance(@PathVariable Long id,
                                         @ModelAttribute ModelPerformance modelPerformance,
                                         RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";
        String result = historicalDataService.updateModelPerformance(id, modelPerformance);
        if (!result.equals("success")) {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "Performance record updated successfully!");
        }
        return "redirect:/user/historical?tab=performance";
    }

    @PostMapping("/performance/delete/{id}")
    public String deleteModelPerformance(@PathVariable Long id,
                                         RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";
        historicalDataService.deleteModelPerformance(id);
        redirectAttributes.addFlashAttribute("successMsg", "Performance record deleted successfully!");
        return "redirect:/user/historical?tab=performance";
    }
}