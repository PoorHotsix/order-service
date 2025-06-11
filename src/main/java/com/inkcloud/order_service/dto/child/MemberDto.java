package com.inkcloud.order_service.dto.child;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MemberDto {
    private String memberEmail;
    private String memberContact;
    private String memberName;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MemberDto memberDto = (MemberDto) obj;
        return Objects.equals(memberEmail, memberDto.memberEmail) &&
                Objects.equals(memberContact, memberDto.memberContact) &&
                Objects.equals(memberName, memberDto.memberName);
    }
}