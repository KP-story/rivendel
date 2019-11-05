package com.kp.scripting.groovy;

import com.kp.scripting.BaseScript;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class GroovyScript extends BaseScript {


    public GroovyScript(String content) {
        this.setContent(content);

        byte[] sha512;
        try {
            sha512 = MessageDigest.getInstance("SHA-512").digest(content.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot encrypt content to make name with SHA-512", e);
        }
        String name = Hex.encodeHexString(sha512);
        this.setName(name);
    }

    public GroovyScript(String content, String name) {
        this.setContent(content);
        this.setName(name);
    }
}
