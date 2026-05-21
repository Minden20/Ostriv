package com.minden.infrastructure.hashing;

public interface HashingService {
    String hash(String value);
    boolean verify(String value, String hashedValue);
}
