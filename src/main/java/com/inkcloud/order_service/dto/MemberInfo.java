package com.inkcloud.order_service.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberInfo {
    @Column(name = "member_email")
    private String memberEmail;
    @Column(name = "member_contact")
    private String memberContact;
    @Column(name = "member_name")
    private String memberName;
}
