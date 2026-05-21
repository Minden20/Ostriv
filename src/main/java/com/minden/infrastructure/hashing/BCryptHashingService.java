package com.minden.infrastructure.hashing;

import com.minden.util.BCrypt;

public class BCryptHashingService implements HashingService {

    @Override
    public String hash(String value) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(value, salt);
    }

    @Override
    public boolean verify(String value, String hashedValue) {
        try {
            return BCrypt.checkpw(value, hashedValue);
        } catch (Exception e) {
            return false;
        }
    }
}
