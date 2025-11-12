package io.dnrdl12.remittance.comm.crypto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
/**
 * packageName    : io.dnrdl12.remittance.comm.config
 * fileName       : CryptoProperties
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    : 프로퍼티 값만 받음 충돌방지용
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.crypto")
public class CryptoProperties {
    private String aesKeyBase64; // 256-bit Base64 키
}