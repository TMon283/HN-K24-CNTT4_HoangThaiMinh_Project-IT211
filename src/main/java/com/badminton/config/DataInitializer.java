package com.badminton.config;

import com.badminton.constant.RoleType;
import com.badminton.entity.Court;
import com.badminton.entity.Role;
import com.badminton.entity.TimeSlot;
import com.badminton.entity.User;
import com.badminton.repository.CourtRepository;
import com.badminton.repository.RoleRepository;
import com.badminton.repository.TimeSlotRepository;
import com.badminton.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
        seedManagerUser();
        seedCourts();
        seedTimeSlots();
    }

    private void seedRoles() {
        Arrays.stream(RoleType.values())
                .filter(roleType -> roleRepository.findByName(roleType).isEmpty())
                .map(roleType -> Role.builder().name(roleType).build())
                .forEach(roleRepository::save);
    }

    private void seedAdminUser() {
        if (userRepository.findByEmail("admin@badminton.com").isPresent()) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseThrow();

        User admin = User.builder()
                .email("admin@badminton.com")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("System Administrator")
                .phone("0900000000")
                .enabled(true)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(admin);
    }

    private void seedManagerUser() {
        if (userRepository.findByEmail("manager@badminton.com").isPresent()) {
            return;
        }

        Role managerRole = roleRepository.findByName(RoleType.ROLE_MANAGER)
                .orElseThrow();

        User manager = User.builder()
                .email("manager@badminton.com")
                .password(passwordEncoder.encode("Manager@123"))
                .fullName("Court Manager")
                .phone("0900000001")
                .enabled(true)
                .roles(new HashSet<>(Set.of(managerRole)))
                .build();

        userRepository.save(manager);
    }

    private void seedCourts() {
        if (courtRepository.count() > 0) {
            return;
        }

        List<Court> courts = List.of(
                Court.builder()
                        .name("Court A")
                        .description("Premium indoor court with professional flooring")
                        .pricePerHour(new BigDecimal("150000"))
                        .active(true)
                        .build(),
                Court.builder()
                        .name("Court B")
                        .description("Standard indoor court")
                        .pricePerHour(new BigDecimal("120000"))
                        .active(true)
                        .build(),
                Court.builder()
                        .name("Court C")
                        .description("Training court for beginners")
                        .pricePerHour(new BigDecimal("100000"))
                        .active(true)
                        .build()
        );

        courtRepository.saveAll(courts);
    }

    private void seedTimeSlots() {
        if (timeSlotRepository.count() > 0) {
            return;
        }

        List<TimeSlot> slots = List.of(
                TimeSlot.builder().startTime(LocalTime.of(6, 0)).endTime(LocalTime.of(7, 0)).active(true).build(),
                TimeSlot.builder().startTime(LocalTime.of(7, 0)).endTime(LocalTime.of(8, 0)).active(true).build(),
                TimeSlot.builder().startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(9, 0)).active(true).build(),
                TimeSlot.builder().startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0)).active(true).build(),
                TimeSlot.builder().startTime(LocalTime.of(17, 0)).endTime(LocalTime.of(18, 0)).active(true).build(),
                TimeSlot.builder().startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(19, 0)).active(true).build(),
                TimeSlot.builder().startTime(LocalTime.of(19, 0)).endTime(LocalTime.of(20, 0)).active(true).build(),
                TimeSlot.builder().startTime(LocalTime.of(20, 0)).endTime(LocalTime.of(21, 0)).active(true).build()
        );

        timeSlotRepository.saveAll(slots);
    }
}
