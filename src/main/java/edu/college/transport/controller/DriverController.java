package edu.college.transport.controller;

import edu.college.transport.entity.Bus;
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
@RequestMapping("/driver")
@Validated
public class DriverController {
    private final TransportService transport;
    public DriverController(TransportService transport) { this.transport = transport; }

    @GetMapping
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("driver", transport.driverFor(auth.getName()));
        model.addAttribute("trip", transport.activeTrip(auth.getName()).orElse(null));
        model.addAttribute("trips", transport.driverTrips(auth.getName()));
        Bus bus = transport.busForDriver(auth.getName());
        model.addAttribute("bus", bus);
        model.addAttribute("stops", bus != null && bus.getRoute() != null ? transport.stopsForRoute(bus.getRoute().getId()) : java.util.List.of());
        return "driver/dashboard";
    }
    @PostMapping("/trip/start") public String start(Authentication auth, RedirectAttributes redirect) { transport.startTrip(auth.getName()); redirect.addFlashAttribute("success", "Trip started."); return "redirect:/driver"; }
    @PostMapping("/trip/end") public String end(Authentication auth, RedirectAttributes redirect) { transport.endTrip(auth.getName()); redirect.addFlashAttribute("success", "Trip completed."); return "redirect:/driver"; }
    @PostMapping("/trip/stop") public String stop(@RequestParam Long stopId, Authentication auth, RedirectAttributes redirect) { transport.updateCurrentStop(auth.getName(), stopId); redirect.addFlashAttribute("success", "Current stop updated."); return "redirect:/driver"; }
    @PostMapping("/report") public String report(@RequestParam boolean breakdown, @RequestParam @NotBlank @Size(max = 700) String message, Authentication auth, RedirectAttributes redirect) {
        transport.reportIssue(auth.getName(), breakdown, message); redirect.addFlashAttribute("success", "Transport control has received your report."); return "redirect:/driver";
    }
}
