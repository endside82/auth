package com.endside.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MemberInfo {

    private String id;

    private long userId;

    private String email;

    private String mobile;

    @JsonFormat(pattern="yyyyMMdd")
    private Date birthDate;

    private int status;
}



