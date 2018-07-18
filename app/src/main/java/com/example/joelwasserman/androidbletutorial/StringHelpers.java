package com.example.joelwasserman.androidbletutorial;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by TheSwifter on 3/21/18.
 */

public class StringHelpers {

    public static UUID makeUuid(String uuidString) {
        String[] parts = {
                uuidString.substring(0, 7),
                uuidString.substring(9, 12),
                uuidString.substring(14, 17),
                uuidString.substring(19, 22),
                uuidString.substring(24, 35)
        };
        long m1 = Long.parseLong(parts[0], 16);
        long m2 = Long.parseLong(parts[1], 16);
        long m3 = Long.parseLong(parts[2], 16);
        long lsb1 = Long.parseLong(parts[3], 16);
        long lsb2 = Long.parseLong(parts[4], 16);
        long msb = (m1 << 32) | (m2 << 16) | m3;
        long lsb = (lsb1 << 48) | lsb2;
        return new UUID(msb, lsb);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }



    public static String to16Hex(int decimal) {

        String hexValue = Integer.toHexString(decimal);

        List<String> ajustment = Arrays.asList("", "0", "00", "000");

        int ajustmmenIndex = 4 - hexValue.length();

        return ajustment.get(ajustmmenIndex) + hexValue;
    }

    public static String to8Hex(int decimal) {

        String hexValue = Integer.toHexString(decimal);

        List<String> ajustment = Arrays.asList("", "0");

        int ajustmmenIndex = 2 - hexValue.length();

        return ajustment.get(ajustmmenIndex) + hexValue;
    }

    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    public static  byte[] parseHex(String hexString) {

        hexString = hexString.replaceAll("\\s", "").toUpperCase();
        String filtered = new String();
        for(int i = 0; i != hexString.length(); ++i) {
            if (hexVal(hexString.charAt(i)) != -1)
                filtered += hexString.charAt(i);
        }

        if (filtered.length() % 2 != 0) {
            char last = filtered.charAt(filtered.length() - 1);
            filtered = filtered.substring(0, filtered.length() - 1) + '0' + last;
        }

        return hexToByteArray(filtered);
    }

    public static byte[] hexToByteArray(String hex) {
        hex = hex.length()%2 != 0?"0"+hex:hex;

        byte[] b = new byte[hex.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    private static int hexVal(char ch) {
        return Character.digit(ch, 16);
    }


    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    static String hexToBinary(String hex) {
        int i = Integer.parseInt(hex, 16);
        String bin = Integer.toBinaryString(i);
        return bin;
    }

    public static String fromDecimalToBinary(int decimal){
       String bin = String.format("%8s", Integer.toBinaryString(decimal)).replace(' ', '0') ;
       return bin;
    }

    public static byte[] fromHexString(final String encoded) {
        if ((encoded.length() % 2) != 0)
            throw new IllegalArgumentException("Input string must contain an even number of characters");

        final byte result[] = new byte[encoded.length()/2];
        final char enc[] = encoded.toCharArray();
        for (int i = 0; i < enc.length; i += 2) {
            StringBuilder curr = new StringBuilder(2);
            curr.append(enc[i]).append(enc[i + 1]);
            result[i/2] = (byte) Integer.parseInt(curr.toString(), 16);
        }
        return result;
    }
}
