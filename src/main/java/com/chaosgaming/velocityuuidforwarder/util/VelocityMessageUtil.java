package com.chaosgaming.velocityuuidforwarder.util;

import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VelocityMessageUtil {
    private static final int MODERN_FORWARDING_VERSION = 1;
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    public static class ForwardingData {
        private final String address;
        private final UUID uuid;
        private final String username;
        private final List<Property> properties;
        
        public ForwardingData(String address, UUID uuid, String username, List<Property> properties) {
            this.address = address;
            this.uuid = uuid;
            this.username = username;
            this.properties = properties;
        }
        
        public String getAddress() {
            return address;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public String getUsername() {
            return username;
        }
        
        public List<Property> getProperties() {
            return properties;
        }
    }
    
    /**
     * Read and verify Velocity's modern forwarding data
     */
    public static ForwardingData readForwardingData(ByteBuf data, String secret) throws Exception {
        // Read the signature first
        byte[] signature = new byte[32]; // HMAC-SHA256 produces 32 bytes
        data.readBytes(signature);
        
        // Read the rest of the data for verification
        int dataStartIndex = data.readerIndex();
        
        // Read version
        int version = data.readByte() & 0xFF;
        if (version != MODERN_FORWARDING_VERSION) {
            throw new IllegalStateException("Unsupported forwarding version: " + version);
        }
        
        // Read address
        String address = readString(data);
        
        // Read UUID
        UUID uuid = readUuid(data);
        
        // Read username
        String username = readString(data);
        
        // Read properties
        List<Property> properties = readProperties(data);
        
        // Now verify the signature
        int dataLength = data.readerIndex() - dataStartIndex;
        data.readerIndex(dataStartIndex);
        byte[] dataBytes = new byte[dataLength];
        data.readBytes(dataBytes);
        
        if (!verifySignature(dataBytes, signature, secret)) {
            throw new SecurityException("Invalid forwarding signature!");
        }
        
        return new ForwardingData(address, uuid, username, properties);
    }
    
    /**
     * Verify HMAC-SHA256 signature
     */
    private static boolean verifySignature(byte[] data, byte[] signature, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] computed = mac.doFinal(data);
            return MessageDigest.isEqual(computed, signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to verify signature", e);
        }
    }
    
    /**
     * Read a string from the ByteBuf (VarInt length + UTF-8 bytes)
     */
    private static String readString(ByteBuf buf) {
        int length = readVarInt(buf);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Read a UUID from the ByteBuf
     */
    private static UUID readUuid(ByteBuf buf) {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        return new UUID(mostSigBits, leastSigBits);
    }
    
    /**
     * Read game profile properties
     */
    private static List<Property> readProperties(ByteBuf buf) {
        int count = readVarInt(buf);
        List<Property> properties = new ArrayList<>(count);
        
        for (int i = 0; i < count; i++) {
            String name = readString(buf);
            String value = readString(buf);
            String signature = readString(buf);
            
            if (signature.isEmpty()) {
                properties.add(new Property(name, value));
            } else {
                properties.add(new Property(name, value, signature));
            }
        }
        
        return properties;
    }
    
    /**
     * Read a VarInt from the ByteBuf
     */
    private static int readVarInt(ByteBuf buf) {
        int value = 0;
        int length = 0;
        byte currentByte;
        
        do {
            currentByte = buf.readByte();
            value |= (currentByte & 0x7F) << (length * 7);
            length++;
            
            if (length > 5) {
                throw new RuntimeException("VarInt exceeded maximum length of 5 bytes (got: " + length + " bytes)");
            }
        } while ((currentByte & 0x80) == 0x80);
        
        return value;
    }
}
