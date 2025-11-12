package io.dnrdl12.remittance.comm.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * packageName    : io.dnrdl12.remittance.comm.api
 * fileName       : PagingProperties
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */

@ConfigurationProperties(prefix = "app.paging")
public record PagingProperties(
        int defaultSize,
        int maxSize
) {}