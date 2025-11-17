package io.dnrdl12.remittance.utills;
import io.dnrdl12.remittance.comm.crypto.CryptoUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * packageName    : io.dnrdl12.remittance.utills
 * fileName       : CryptoInitTest
 * author         : JW.CHOI
 * date           : 2025-11-17
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-17        JW.CHOI              최초 생성
 */


@SpringBootTest
class CryptoInitTest {

    @Autowired
    private CryptoUtil cryptoUtil;


    @Test
    void SystemCIDIPhone() {
        String ciPlain = "SYSTEM-CI";
        String diPlain = "SYSTEM-DI";
        String phonePlain = "00000000000";   // SYSTEM_MEMBER 전화번호

        String ciEnc = cryptoUtil.encrypt(ciPlain);
        String ciHash = cryptoUtil.hmacHash(ciPlain);

        String diEnc = cryptoUtil.encrypt(diPlain);
        String diHash = cryptoUtil.hmacHash(diPlain);

        String phoneEnc = cryptoUtil.encrypt(phonePlain);
        String phoneHash = cryptoUtil.hmacHash(phonePlain);

        System.out.println("=== SYSTEM MEMBER CIDI 값 생성 ===");
        System.out.println("member_ci        = '" + ciEnc + "'");
        System.out.println("member_ci_hash   = '" + ciHash + "'");
        System.out.println("member_di        = '" + diEnc + "'");
        System.out.println("member_di_hash   = '" + diHash + "'");
        System.out.println("phoneEnc         = '" + phoneEnc + "'");
        System.out.println("member_phone_hash= '" + phoneHash + "'");

    }
}
