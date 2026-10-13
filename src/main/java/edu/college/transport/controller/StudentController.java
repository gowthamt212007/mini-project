package edu.college.transport.controller;

import edu.college.transport.service.TransportService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
@Validated
public class StudentController {
    private final TransportService transport;
    public StudentController(TransportService transport) { this.transport = transport; }

    @GetMapping
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("student", transport.studentFor(auth.getName()));
        model.addAttribute("allocation", transport.allocationFor(auth.getName()).orElse(null));
        model.addAttribute("eta", transport.estimatedArrival(auth.getName()));
        model.addAttribute("nearbyBuses", transport.nearbyBusSuggestions(auth.getName()));
        model.addAttribute("updates", transport.recentUpdates());
        model.addAttribute("requests", transport.recoveriesForStudent(auth.getName()));
        return "student/dashboard";
    }

    @PostMapping("/recovery")
    public String recovery(@RequestParam @NotBlank @Size(max = 500) String reason, Authentication auth, RedirectAttributes redirect) {
        transport.requestRecovery(auth.getName(), reason); redirect.addFlashAttribute("success", "Your missed-bus request has been sent to transport administration.");
        return "redirect:/student";
    }
}
