package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RiderProfileVO {

    private Long riderId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String realName;
    private String idCardMasked;
    private String idCardFront;
    private String idCardBack;
    private Integer auditStatus;
    private String auditStatusText;
    private Integer riderStatus;
    private String riderStatusText;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime updateTime;
}
