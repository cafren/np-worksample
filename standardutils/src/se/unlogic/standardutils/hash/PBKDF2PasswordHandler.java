package se.unlogic.standardutils.hash;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2PasswordHandler {

	public static final SecureRandom SECURE_RANDOM = new SecureRandom();
	
	public static String generateStorngPasswordHash(String password) {

		try {
			int iterations = 310000;
			char[] chars = password.toCharArray();
			byte[] salt = generateSalt();

			PBEKeySpec keySpec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
			
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			
			byte[] hash = secretKeyFactory.generateSecret(keySpec).getEncoded();
			
			return iterations + ":" + toHex(salt) + ":" + toHex(hash);
			
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

			throw new RuntimeException(e);
		}
	}	
	
	public static boolean validatePassword(String originalPassword, String storedPassword) {

		try {
			String[] parts = storedPassword.split(":");
			int iterations = Integer.parseInt(parts[0]);
			byte[] salt = fromHex(parts[1]);
			byte[] hash = fromHex(parts[2]);

			PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
			
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			
			byte[] testHash = skf.generateSecret(spec).getEncoded();

			int diff = hash.length ^ testHash.length;
			
			for (int i = 0; i < hash.length && i < testHash.length; i++) {
			
				diff |= hash[i] ^ testHash[i];
			}
			
			return diff == 0;
			
		} catch (Exception e) {
			
			return false;
		}
	}

	private static byte[] fromHex(String hex) throws NoSuchAlgorithmException {

		byte[] bytes = new byte[hex.length() / 2];
		
		for (int i = 0; i < bytes.length; i++) {
		
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		
		return bytes;
	}
	
	private static byte[] generateSalt() throws NoSuchAlgorithmException {

		byte[] salt = new byte[128];
		
		SECURE_RANDOM.nextBytes(salt);
		
		return salt;
	}

	private static String toHex(byte[] array) throws NoSuchAlgorithmException {

		BigInteger bigInteger = new BigInteger(1, array);
		
		String hex = bigInteger.toString(16);
		
		int paddingLength = (array.length * 2) - hex.length();
		
		if (paddingLength > 0) {
		
			return String.format("%0" + paddingLength + "d", 0) + hex;
			
		} else {
			
			return hex;
		}
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {

		String originalPassword = "password";
		String generatedSecuredPasswordHash = generateStorngPasswordHash(originalPassword);

		System.out.println("generatedSecuredPasswordHash:\n" + generatedSecuredPasswordHash);

		System.out.println("length:" + generatedSecuredPasswordHash.length());
		
		boolean matched = validatePassword("password", generatedSecuredPasswordHash);
		System.out.println(matched);

		matched = validatePassword("password1", generatedSecuredPasswordHash);
		System.out.println(matched);
	}

}
