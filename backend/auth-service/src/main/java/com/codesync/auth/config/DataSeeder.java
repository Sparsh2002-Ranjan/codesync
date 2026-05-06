package com.codesync.auth.config;

import com.codesync.auth.entity.User;
import com.codesync.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@codesync.io")) {
            userRepository.save(User.builder()
                .username("admin").email("admin@codesync.io")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("Platform Admin").role(User.Role.ADMIN)
                .provider(User.Provider.LOCAL).active(true)
                .bio("CodeSync platform administrator").build());
            log.info("✅ Admin seeded — admin@codesync.io / admin123");
        }
        if (!userRepository.existsByEmail("dev@codesync.io")) {
            userRepository.save(User.builder()
                .username("demo_dev").email("dev@codesync.io")
                .passwordHash(passwordEncoder.encode("dev123"))
                .fullName("Demo Developer").role(User.Role.DEVELOPER)
                .provider(User.Provider.LOCAL).active(true)
                .bio("Demo developer account").build());
            log.info("✅ Developer seeded — dev@codesync.io / dev123");
        }
        if (!userRepository.existsByEmail("collab@codesync.io")) {
            userRepository.save(User.builder()
                .username("collab_user").email("collab@codesync.io")
                .passwordHash(passwordEncoder.encode("collab123"))
                .fullName("Collaborator User").role(User.Role.DEVELOPER)
                .provider(User.Provider.LOCAL).active(true)
                .bio("Second user for testing collaboration").build());
            log.info("✅ Collab user seeded — collab@codesync.io / collab123");
        }
    }
}
