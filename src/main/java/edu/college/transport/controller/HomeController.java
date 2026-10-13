package edu.college.transport.controller;

import edu.college.transport.dto.RegistrationForm;
import edu.college.transport.service.RegistrationService;
import edu.college.transport.service.TransportService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    private final TransportService transport;
    private final RegistrationService registration;
    public HomeController(TransportService transport, RegistrationService registration) { this.transport = transport; this.registration = registration; }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("routes", transport.activeRoutes());
        model.addAttribute("buses", transport.allBuses().stream().filter(b -> b.getStatus().name().equals("AVAILABLE")).toList());
        model.addAttribute("studentCount", transport.studentCount());
        return "home";
    }

    @GetMapping("/explore")
    public String explore(Model model) { model.addAttribute("routes", transport.activeRoutes()); model.addAttribute("buses", transport.allBuses()); return "explore"; }

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/admin-login")
    public String adminLogin() { return "auth/admin-login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm()); model.addAttribute("stops", transport.activeStops()); return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { model.addAttribute("stops", transport.activeStops()); return "auth/register"; }
        try { registration.register(form); redirect.addFlashAttribute("success", "Your student account is ready. Sign in to continue."); return "redirect:/login?registered"; }
        catch (IllegalArgumentException ex) { result.reject("registration", ex.getMessage()); model.addAttribute("stops", transport.activeStops()); return "auth/register"; }
    }

    @GetMapping("/access-denied")
    public String denied() { return "access-denied"; }
}
