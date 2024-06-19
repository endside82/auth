package com.endside.social.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class SocialUserInfo {
    private String socialUniqueId;
    private String email;
}
