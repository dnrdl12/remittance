package io.dnrdl12.remittance.comm.config;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
/**
 * packageName    : io.dnrdl12.remittance.comm.config
 * fileName       : SwaggerConfig.java
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    : Springdoc OpenAPI 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI              최초 생성
 */


@Configuration
public class SwaggerConfig {

    private final BuildProperties buildProperties;

    public SwaggerConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Remittance Service")
                        .description("은행 입출금 서비스")
                        .version(buildProperties.getVersion()));
    }
}
