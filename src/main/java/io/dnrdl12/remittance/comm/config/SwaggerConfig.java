package io.dnrdl12.remittance.comm.config;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import java.util.Optional;

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
 * 2025-11-16        JW.CHOI              appVersion 오류 수정
 */


@Configuration
public class SwaggerConfig {

    private final String appVersion;

    public SwaggerConfig(Optional<BuildProperties> buildProperties) {
        this.appVersion = buildProperties
                .map(BuildProperties::getVersion)
                .orElse("local");   // BuildProperties가 없으면 기본값
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Remittance API")
                        .version(appVersion)
                        .description("송금/계좌 관리 API 문서")
                );
    }
}