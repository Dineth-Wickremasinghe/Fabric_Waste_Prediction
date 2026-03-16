package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.dto.PredictionInputDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("predictionInput", new PredictionInputDTO());
        return "redirect:/sustainability/dashboard";
    }

    @ModelAttribute("fabricTypes")
    public String[] getFabricTypes() {
        return new String[]{"Cotton", "Linen", "Silk", "Polyester", "Viscose", "Rayon",
                "Denim", "Twill", "Poplin", "Blended"};
    }

    @ModelAttribute("fabricPatterns")
    public String[] getFabricPatterns() {
        return new String[]{"Solid", "Striped", "Checked", "Floral", "Printed"};
    }

    @ModelAttribute("cuttingMethods")
    public String[] getCuttingMethods() {
        return new String[]{"Manual", "Auto"};
    }

    @ModelAttribute("shifts")
    public String[] getShifts() {
        return new String[]{"Day", "Night"};
    }
}