package com.endside.user.service.join;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class JoinHelperService {

    public String generateAlphanumericRandomString(int targetStringLength) {
        int leftLimit = 48;           // numeral '0'
        int rightLimit = 122;         // letter 'z'
        Random random = new Random(); // random

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public int makeRandomFourDigitValue() {
        return (int) (Math.random() * (9999 - 1000 + 1) + 1000);
    }
}
