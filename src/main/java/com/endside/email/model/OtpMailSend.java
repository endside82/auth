package com.endside.email.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtpMailSend {
    private String email;
    private String id;
    private String uniqueId;
    private boolean checkOnly;

}
