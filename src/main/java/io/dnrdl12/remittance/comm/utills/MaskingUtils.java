package io.dnrdl12.remittance.comm.utills;

/**
 * packageName    : io.dnrdl12.remittance.comm.utills
 * fileName       : MaskingUtils
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */
public final class MaskingUtils {

    private MaskingUtils() {}

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        // 010-****-1234 형태 (숫자만 들어온다고 가정)
        int len = phone.length();
        return phone.substring(0, 3) + "****" + phone.substring(len - 4);
    }

    public static String maskCi(String ci) {
        if (ci == null) return null;
        if (ci.length() <= 8) return "****";
        return ci.substring(0, 4) + "********" + ci.substring(ci.length() - 4);
    }

    public static String maskDi(String di) {
        if (di == null) return null;
        if (di.length() <= 8) return "****";
        return di.substring(0, 4) + "********" + di.substring(di.length() - 4);
    }
}