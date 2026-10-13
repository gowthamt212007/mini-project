package edu.college.transport.service;

import edu.college.transport.dto.RegistrationForm;
import edu.college.transport.entity.*;
import edu.college.transport.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final UserAccountRepository users;
    private final StudentRepository students;
    private final RouteStopRepository stops;
    private final PasswordEncoder encoder;
    private final TransportService transport;

    public RegistrationService(UserAccountRepository users, StudentRepository students, RouteStopRepository stops, PasswordEncoder encoder, TransportService transport) {
        this.users = users; this.students = students; this.stops = stops; this.encoder = encoder; this.transport = transport;
    }

    @Transactional
    public void register(RegistrationForm form) {
        if (users.existsByUsername(form.getUsername().trim())) throw new IllegalArgumentException("That login ID is already registered.");
        if (students.existsByRegisterNumber(form.getRegisterNumber().trim())) throw new IllegalArgumentException("That register number is already in use.");
        RouteStop stop = stops.findById(form.getPickupStopId()).orElseThrow(() -> new IllegalArgumentException("Choose a valid pickup stop."));
        if (!stop.getRoute().isActive()) throw new IllegalArgumentException("Choose a stop on an active transport route.");
        UserAccount account = new UserAccount();
        account.setUsername(form.getUsername().trim()); account.setFullName(form.getFullName().trim());
        account.setPassword(encoder.encode(form.getPassword())); account.setRole(Role.STUDENT); account.setEnabled(true);
        users.save(account);
        Student student = new Student(); student.setUser(account); student.setRegisterNumber(form.getRegisterNumber().trim()); student.setPickupStop(stop);
        Student saved = students.save(student);
        transport.recommendAllocation(saved.getId());
    }
}
