package edu.college.transport.controller;

import edu.college.transport.dto.BusForm;
import edu.college.transport.dto.RouteForm;
import edu.college.transport.entity.*;
import edu.college.transport.service.TransportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@Validated
public class AdminController {
    private final TransportService transport;
    public AdminController(TransportService transport) { this.transport = transport; }

    @GetMapping public String index() { return "redirect:/admin/dashboard"; }
    @GetMapping("/dashboard") public String dashboard(Model model) {
        model.addAttribute("studentCount", transport.studentCount()); model.addAttribute("busCount", transport.busCount());
        model.addAttribute("routeCount", transport.routeCount()); model.addAttribute("tripCount", transport.tripCountToday());
        model.addAttribute("buses", transport.allBuses()); model.addAttribute("updates", transport.recentUpdates());
        model.addAttribute("trips", transport.recentTrips()); model.addAttribute("allocations", transport.allAllocations());
        Map<Long, Long> occupied = new HashMap<>(); transport.allBuses().forEach(b -> occupied.put(b.getId(), transport.occupiedSeats(b.getId())));
        model.addAttribute("occupied", occupied); return "admin/dashboard";
    }

    @GetMapping("/buses") public String buses(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("buses", transport.allBuses(q)); model.addAttribute("routes", transport.allRoutes(null)); model.addAttribute("drivers", transport.allDrivers());
        model.addAttribute("q", q == null ? "" : q);
        if (!model.containsAttribute("form")) model.addAttribute("form", new BusForm());
        Map<Long, Long> occupied = new HashMap<>(); transport.allBuses(q).forEach(b -> occupied.put(b.getId(), transport.occupiedSeats(b.getId())));
        model.addAttribute("occupied", occupied); return "admin/buses";
    }
    @GetMapping("/buses/{id}/edit") public String editBus(@PathVariable Long id, Model model) {
        Bus bus = transport.allBuses().stream().filter(b -> b.getId().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Bus not found."));
        BusForm form = new BusForm(); form.setId(bus.getId()); form.setBusNumber(bus.getBusNumber()); form.setRegistrationNumber(bus.getRegistrationNumber()); form.setCapacity(bus.getCapacity());
        form.setRouteId(bus.getRoute() == null ? null : bus.getRoute().getId()); form.setDriverId(bus.getDriver() == null ? null : bus.getDriver().getId());
        model.addAttribute("form", form); return buses(null, model);
    }
    @PostMapping("/buses/save") public String saveBus(@Valid @ModelAttribute("form") BusForm form, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return buses(null, model);
        transport.saveBus(form.getId(), form.getBusNumber(), form.getRegistrationNumber(), form.getCapacity(), form.getRouteId(), form.getDriverId());
        redirect.addFlashAttribute("success", "Bus details saved."); return "redirect:/admin/buses";
    }
    @PostMapping("/buses/{id}/deactivate") public String deactivateBus(@PathVariable Long id, RedirectAttributes redirect) { transport.deactivateBus(id); redirect.addFlashAttribute("success", "Bus deactivated."); return "redirect:/admin/buses"; }
    @PostMapping("/buses/{id}/restore") public String restoreBus(@PathVariable Long id, RedirectAttributes redirect) { transport.restoreBus(id); redirect.addFlashAttribute("success", "Bus returned to available status."); return "redirect:/admin/buses"; }

    @GetMapping("/routes") public String routes(@RequestParam(required = false) String q, Model model) { if (!model.containsAttribute("form")) model.addAttribute("form", new RouteForm()); model.addAttribute("routes", transport.allRoutes(q)); model.addAttribute("q", q == null ? "" : q); return "admin/routes"; }
    @GetMapping("/routes/{id}/edit") public String editRoute(@PathVariable Long id, Model model) {
        TransportRoute route = transport.allRoutes(null).stream().filter(r -> r.getId().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Route not found."));
        RouteForm form = new RouteForm(); form.setId(route.getId()); form.setCode(route.getCode()); form.setName(route.getName());
        form.setStopLines(route.getStops().stream().map(s -> s.getName() + " | " + s.getTravelMinutesFromPrevious()).reduce((a,b) -> a + "\n" + b).orElse(""));
        model.addAttribute("form", form); return routes(null, model);
    }
    @PostMapping("/routes/save") public String saveRoute(@Valid @ModelAttribute("form") RouteForm form, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return routes(null, model);
        transport.saveRoute(form.getId(), form.getCode(), form.getName(), form.getStopLines()); redirect.addFlashAttribute("success", "Route and ordered stops saved."); return "redirect:/admin/routes";
    }
    @PostMapping("/routes/{id}/deactivate") public String deactivateRoute(@PathVariable Long id, RedirectAttributes redirect) { transport.deactivateRoute(id); redirect.addFlashAttribute("success", "Route deactivated."); return "redirect:/admin/routes"; }
    @PostMapping("/routes/{id}/activate") public String activateRoute(@PathVariable Long id, RedirectAttributes redirect) { transport.activateRoute(id); redirect.addFlashAttribute("success", "Route activated."); return "redirect:/admin/routes"; }

    @GetMapping("/students") public String students(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, Model model) {
        org.springframework.data.domain.Page<Student> studentPage = transport.studentPage(q, page);
        model.addAttribute("students", studentPage.getContent()); model.addAttribute("studentPage", studentPage);
        model.addAttribute("buses", transport.allBuses()); model.addAttribute("allocations", transport.allAllocations());
        Map<Long, Long> occupied = new HashMap<>(); transport.allBuses().forEach(bus -> occupied.put(bus.getId(), transport.occupiedSeats(bus.getId())));
        model.addAttribute("occupied", occupied);
        model.addAttribute("q", q == null ? "" : q); return "admin/students";
    }
    @PostMapping("/allocations") public String allocate(@RequestParam Long studentId, @RequestParam Long busId, RedirectAttributes redirect) {
        transport.allocate(studentId, busId); redirect.addFlashAttribute("success", "Student bus allocation saved."); return "redirect:/admin/students";
    }
    @PostMapping("/students/{id}/status") public String studentStatus(@PathVariable Long id, @RequestParam boolean enabled, RedirectAttributes redirect) {
        transport.setAccountEnabled(id, enabled); redirect.addFlashAttribute("success", "Student account status updated."); return "redirect:/admin/students";
    }
    @PostMapping("/students/{id}/recommend") public String recommendAllocation(@PathVariable Long id, RedirectAttributes redirect) {
        boolean assigned = transport.recommendAllocation(id);
        redirect.addFlashAttribute(assigned ? "success" : "error", assigned ? "A matching bus was recommended and assigned." : "No available bus serves this pickup stop right now.");
        return "redirect:/admin/students";
    }

    @GetMapping("/drivers") public String drivers(Model model) { model.addAttribute("drivers", transport.allDrivers()); model.addAttribute("buses", transport.allBuses()); return "admin/drivers"; }
    @PostMapping("/drivers") public String addDriver(@RequestParam @NotBlank @Size(max=80) String username,
            @RequestParam @NotBlank @Size(max=120) String fullName, @RequestParam @NotBlank @Size(min=8,max=72) String password,
            @RequestParam @NotBlank @Size(max=40) String licenseNumber, RedirectAttributes redirect) {
        transport.saveDriver(username, fullName, password, licenseNumber); redirect.addFlashAttribute("success", "Driver profile created."); return "redirect:/admin/drivers";
    }

    @GetMapping("/updates") public String updates(Model model) { model.addAttribute("updates", transport.recentUpdates()); model.addAttribute("buses", transport.allBuses()); model.addAttribute("routes", transport.activeRoutes()); model.addAttribute("types", UpdateType.values()); return "admin/updates"; }
    @PostMapping("/updates") public String addUpdate(@RequestParam UpdateType type, @RequestParam @NotBlank @Size(max=700) String message,
            @RequestParam(required=false) Long busId, @RequestParam(required=false) Long routeId, Authentication auth, RedirectAttributes redirect) {
        transport.createUpdate(type, message, busId, routeId, auth.getName()); redirect.addFlashAttribute("success", "Transport update published."); return "redirect:/admin/updates";
    }

    @GetMapping("/recovery") public String recovery(Model model) { model.addAttribute("requests", transport.allRecoveries()); return "admin/recovery"; }
    @PostMapping("/recovery/{id}") public String reviewRecovery(@PathVariable Long id, @RequestParam boolean approve, Authentication auth, RedirectAttributes redirect) {
        transport.reviewRecovery(id, approve, auth.getName()); redirect.addFlashAttribute("success", "Recovery request reviewed."); return "redirect:/admin/recovery";
    }

    @GetMapping("/events") public String events(Model model) { model.addAttribute("events", transport.allEvents()); return "admin/events"; }
    @PostMapping("/events") public String addEvent(@RequestParam @NotBlank @Size(max=140) String name, @RequestParam @NotNull @FutureOrPresent LocalDate eventDate,
            @RequestParam @Min(1) int expectedAttendees, @RequestParam(required=false) @Size(max=500) String notes, RedirectAttributes redirect) {
        transport.saveEvent(name, eventDate, expectedAttendees, notes); redirect.addFlashAttribute("success", "Event plan saved with estimated bus requirement."); return "redirect:/admin/events";
    }
    @PostMapping("/events/{id}/delete") public String deleteEvent(@PathVariable Long id, RedirectAttributes redirect) { transport.deleteEvent(id); redirect.addFlashAttribute("success", "Event removed."); return "redirect:/admin/events"; }

    @GetMapping("/reports") public String reports(Model model) {
        model.addAttribute("allocations", transport.allAllocations()); model.addAttribute("buses", transport.allBuses());
        model.addAttribute("routes", transport.activeRoutes()); model.addAttribute("trips", transport.recentTrips());
        Map<Long, Long> occupied = new HashMap<>(); transport.allBuses().forEach(b -> occupied.put(b.getId(), transport.occupiedSeats(b.getId())));
        model.addAttribute("occupied", occupied); return "admin/reports";
    }
}
