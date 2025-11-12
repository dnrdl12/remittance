package io.dnrdl12.remittance.comm.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * packageName    : io.dnrdl12.remittance.comm
 * fileName       : CommEntity
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI              최초 생성
 */
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public abstract class BaseEntity {

    @Schema(description = "생성자 ID")
    @Column(name = "reg_id", length = 50)
    private String regId;

    @Schema(description = "생성일시 (DB default CURRENT_TIMESTAMP)")
    @Column(name = "reg_date", insertable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime regDate;

    @Schema(description = "수정자 ID")
    @Column(name = "mod_id", length = 50)
    private String modId;

    @Schema(description = "수정일시 (DB default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)")
    @Column(name = "mod_date", insertable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime modDate;
}