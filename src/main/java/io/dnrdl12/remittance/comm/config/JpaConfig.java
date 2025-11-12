package io.dnrdl12.remittance.comm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
/**
 * packageName    : io.dnrdl12.remittance.comm.config
 * fileName       : JpaConfig
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI              최초 생성
 */
@EnableJpaAuditing
@Configuration
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("SYSTEM");
    }
}