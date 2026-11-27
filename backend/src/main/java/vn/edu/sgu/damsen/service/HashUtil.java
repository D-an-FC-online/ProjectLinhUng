package vn.edu.sgu.damsen.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashUtil {
    private HashUtil() {}

    /**
     * content_hash của một phiên bản thuyết minh: SHA-256 (hex thường) của UTF-8(title + "\n" + body).
     * Script V2__seed_data.sql tính đúng theo cùng công thức.
     */
    public static String contentHash(String title, String body) {
        return sha256(((title == null ? "" : title) + "\n" + (body == null ? "" : body))
                .getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
