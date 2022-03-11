package com.example.igecuser.cryptography;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAUtil {

    public static String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJZX0I6UiXNsqy9TkNCpay0RlEnqg6ZLoO+Wx+on/Ccxhr+3P+/lXGG8hT6lRGs0q/9lCtrkjrX6TrKHUUlZfWa+4gNcx5AEgx+hnNSs/IeqlAmDzyo6WysyLImHM+UWx0QGVz82K6I+bSC+93Jkqdt+sShU9Vbs43qV6Fh6PKtlAgMBAAECgYAIq0mgXxpJ6JM6aGQqVAqVD7VwSbCLr0K5bgVbEDEvBWESvvtV1vDrxdYdFaSLVf8w+9+TXaiI/8T38GuUgi+D3PbX4V5tKSANlnHgn9fw0IrJtl1CHqU0q0VbbWCxUMdnjzKBs1Rxn8w/KpF30wWfi4jc0ryl+f33nOsaDHslAQJBAPbDze8vdKXl2u9v+aK6cIWgBwT9N6yhtPHlSaa/XC434mRg1/m4+SnM3Bbs4KpnnUxPrh0UDQfTQVdRDGdzbKUCQQCb+Dcsz7AYaxcjnOXDKF8N8pHlhSl/4gS/52n2h/9tJtTSh5U+mxskn0EldTzQCqfMD8FgFlnFXRIYz1Ka3kfBAkEA2aIHVhQ8hLQcS2Augt57rt5cUoIhQBe+Rjk6o93RptGS0YS39n61AbCzy2RPIPsRN+RuYybz9xrSXfgVQIgEIQJAbKhi//P8sTZCG4xdwTUp65SXMbgwbiguuObmOlsKhqdr0vOj9MaoBT5xa0Aeyqzxs0cyp9dKWSX6yo/882lxwQJABoYwSmoDgyBFjqwg79UQOztxL1OASREj9NG6u3bc+vpBJadES5AdQlF1A1YahOlIOIEwQmOKwn1/0+yUbVXgwg==";


    public static PrivateKey getPrivateKey(String base64PrivateKey) {
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            assert keyFactory != null;
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    public static String decrypt(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(data));
    }

    public static String decrypt(String data, String base64PrivateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return decrypt(Base64.getDecoder().decode(data.getBytes()), getPrivateKey(base64PrivateKey));
    }
}