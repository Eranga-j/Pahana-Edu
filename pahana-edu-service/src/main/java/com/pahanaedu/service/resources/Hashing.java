package com.pahanaedu.service.resources;
import java.security.MessageDigest; import java.util.HexFormat;
public class Hashing {
    public static String sha256Hex(String input){
        try { MessageDigest md = MessageDigest.getInstance("SHA-256"); byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)); return HexFormat.of().formatHex(bytes); }
        catch(Exception e){ throw new RuntimeException("Hashing failed", e); }
    }
}
