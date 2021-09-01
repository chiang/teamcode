package io.teamcode.common.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static io.teamcode.common.security.hash.Crypt.cryptUsingStandardDES;
import static io.teamcode.common.security.hash.Crypt.generateSalt;

/**

 * Apache httpd.con 에서 아래 모듈을 활성화해야 한다
 *
 * LoadModule auth_digest_module libexec/apache2/mod_auth_digest.so

 * Simple Htpassword generator
 * <p/>
 * All three hashing methods is avail:
 * - SHA-1
 * - MD-5
 * - Crypt(3)
 *
 * Please address that SHA-1 or Crypt is > MD-5.
 *
 * @author vegaasen
 * @since 1.0-SNAPSHOT
 */
public class HashingDigester {

	private static final Logger LOGGER = LoggerFactory.getLogger(HashingDigester.class);
    private static final String RESULT_IDENTIFIER_SHA = "{SHA}";
    private static final String RESULT_IDENTIFIER_MD5 = "$apr1$";
    private static final String IDENTIFIER_MD5 = "MD5";
    private static final String MD_5_SEPERATOR = ":";
    
    protected static final String E_OBJECT_WAS_NULL = "Object was null";
    protected static final String E_FILE_NOT_EXIST = "File does not exist";
    
    public static String generateHtDigest(final String userName, final String passwd, final String realm) {
        LOGGER.debug("Generating digest using realm: {}", realm);
    	String password = HashingDigester.generateEncryptedPassword(realm, userName, passwd, HtPasswdVariant.ALG_APMD5);
    	password = password.substring(6, password.length());
    	
    	return new StringBuilder(userName).append(":").append(realm).append(":").append(password).toString();
    }

    public static String generateEncryptedPassword(final String realm, final String usr, final String pwd, final HtPasswdVariant type)
            throws NullPointerException {
        if (verifyNotNull(pwd, type)) {
            if (StringUtils.isNotEmpty(pwd)) {
                switch (type) {
                    case ALG_APSHA:
                        LOGGER.debug("Using SHA-1 with BASE-64");
                        return generateSha1Representation(pwd);
                    case ALG_CRYPT:
                        LOGGER.debug("Using CRYPT");
                        return generateCryptRepresentation(pwd);
                    case ALG_APMD5:
                        LOGGER.debug("Using MD5 + SALT");
                        return generateMD5Representation(realm, usr, pwd);
                    default:
                        LOGGER.error("Please select hashing algorithm before continuing.");
                }
            }
        }
        LOGGER.error("Halted. Missing vital parameters.");
        throw new NullPointerException(E_OBJECT_WAS_NULL);
    }

    private static String generateSha1Representation(final String pwd) {
        if (StringUtils.isNotEmpty(pwd)) {
            LOGGER.debug("Hashing complete. Returning result <SHA1>");
            return RESULT_IDENTIFIER_SHA + new String(Base64.encodeBase64(
                    getMessageDigest("SHA1").digest(
                            pwd.getBytes()
                    )
            ));
        }
        LOGGER.debug("Unable to generate password using SHA-1. Missing parameter");
        return "";
    }

    private static String generateMD5Representation(final String realm, final String userName, final String password) {
        try {
            String assembledString = userName + MD_5_SEPERATOR + realm + MD_5_SEPERATOR + password;
            byte digestedBytes[] = getMessageDigest(IDENTIFIER_MD5).digest(assembledString.getBytes());
            BigInteger bigInteger = new BigInteger(1, digestedBytes);
            String result = bigInteger.toString(16);
            while (result.length() < 32) {
                result = "0" + result;
            }
            LOGGER.debug("Hashing complete. Returning result <MD5>: " + RESULT_IDENTIFIER_MD5 + result);
            return RESULT_IDENTIFIER_MD5 + result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String generateCryptRepresentation(final String pwd) {
        LOGGER.debug("Hashing complete. Returning result <Crypt>");
        return cryptUsingStandardDES(generateSalt(), pwd);
    }

    private static MessageDigest getMessageDigest(final String type) {
        try {
            return MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new NullPointerException(E_OBJECT_WAS_NULL);
    }
    
    protected static boolean verifyNotNull(final Object... object) {
        boolean objectWasNotNull = true;
        for(Object o : object) {
            if(o==null) {
                objectWasNotNull = false;
                break;
            }
        }
        return objectWasNotNull;
    }

    protected static boolean verifyNull(final Object object) {
        return (object==null);
    }
}
