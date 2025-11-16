package io.dnrdl12.remittance.comm.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * packageName    : io.dnrdl12.remittance.comm.config
 * fileName       : FlywayConfig
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */

@Configuration
@Profile("dev")
public class FlywayConfig {
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.clean();    // 전체 삭제
            flyway.migrate();  // V1, V2... 순서대로 다시 실행
        };
    }
}