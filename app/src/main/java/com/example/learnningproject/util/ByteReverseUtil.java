package com.example.learnningproject.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ByteReverseUtil {
    public ByteReverseUtil() {
    }

    public static byte[] int2BytesBig(int n) {
        byte[] b = new byte[]{(byte)(n >> 24 & 255), (byte)(n >> 16 & 255), (byte)(n >> 8 & 255), (byte)(n & 255)};
        return b;
    }

    public static byte[] int2BytesLittle(int n) {
        byte[] b = new byte[]{(byte)(n & 255), (byte)(n >> 8 & 255), (byte)(n >> 16 & 255), (byte)(n >> 24 & 255)};
        return b;
    }

    public static int bytes2IntLittle(byte[] bytes) {
        int int1 = bytes[0] & 255;
        int int2 = (bytes[1] & 255) << 8;
        int int3 = (bytes[2] & 255) << 16;
        int int4 = (bytes[3] & 255) << 24;
        return int1 | int2 | int3 | int4;
    }

    public static int bytes2IntBig(byte[] bytes) {
        int int1 = bytes[3] & 255;
        int int2 = (bytes[2] & 255) << 8;
        int int3 = (bytes[1] & 255) << 16;
        int int4 = (bytes[0] & 255) << 24;
        return int1 | int2 | int3 | int4;
    }

    public static byte[] short2BytesBig(short n) {
        byte[] b = new byte[]{(byte)(n >> 8 & 255), (byte)(n & 255)};
        return b;
    }

    public static byte[] short2BytesLittle(short n) {
        byte[] b = new byte[]{(byte)(n & 255), (byte)(n >> 8 & 255)};
        return b;
    }

    public static short bytes2ShortLittle(byte[] b) {
        return (short)(b[1] << 8 | b[0] & 255);
    }

    public static short bytes2ShortBig(byte[] b) {
        return (short)(b[0] << 8 | b[1] & 255);
    }

    public static byte[] long2BytesBig(long n) {
        byte[] b = new byte[]{(byte)((int)(n >> 56 & 255L)), (byte)((int)(n >> 48 & 255L)), (byte)((int)(n >> 40 & 255L)), (byte)((int)(n >> 32 & 255L)), (byte)((int)(n >> 24 & 255L)), (byte)((int)(n >> 16 & 255L)), (byte)((int)(n >> 8 & 255L)), (byte)((int)(n & 255L))};
        return b;
    }

    public static byte[] long2BytesLittle(long n) {
        byte[] b = new byte[]{(byte)((int)(n & 255L)), (byte)((int)(n >> 8 & 255L)), (byte)((int)(n >> 16 & 255L)), (byte)((int)(n >> 24 & 255L)), (byte)((int)(n >> 32 & 255L)), (byte)((int)(n >> 40 & 255L)), (byte)((int)(n >> 48 & 255L)), (byte)((int)(n >> 56 & 255L))};
        return b;
    }

    public static long bytes2LongLittle(byte[] array) {
        return (long)array[0] & 255L | ((long)array[1] & 255L) << 8 | ((long)array[2] & 255L) << 16 | ((long)array[3] & 255L) << 24 | ((long)array[4] & 255L) << 32 | ((long)array[5] & 255L) << 40 | ((long)array[6] & 255L) << 48 | ((long)array[7] & 255L) << 56;
    }

    public static long bytes2LongBig(byte[] array) {
        return ((long)array[0] & 255L) << 56 | ((long)array[1] & 255L) << 48 | ((long)array[2] & 255L) << 40 | ((long)array[3] & 255L) << 32 | ((long)array[4] & 255L) << 24 | ((long)array[5] & 255L) << 16 | ((long)array[6] & 255L) << 8 | (long)array[7] & 255L;
    }

    public static byte[] long2BytesBOS(long l) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bao);
        dos.writeLong(l);
        byte[] buf = bao.toByteArray();
        return buf;
    }

    public long bytes2LongBOS(byte[] data) throws IOException {
        ByteArrayInputStream bai = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bai);
        return dis.readLong();
    }

    public static byte[] long2BytesByteBuffer(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public static long bytes2LongByteBuffer(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }

    public static long bytes2Long(byte[] input, int offset, boolean littleEndian) {
        long value = 0L;

        for(int count = 0; count < 8; ++count) {
            int shift = (littleEndian ? count : 7 - count) << 3;
            value |= 255L << shift & (long)input[offset + count] << shift;
        }

        return value;
    }

    public static long bytes2LongByteBuffer(byte[] input, int offset, boolean littleEndian) {
        ByteBuffer buffer = ByteBuffer.wrap(input, offset, 8);
        if (littleEndian) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        return buffer.getLong();
    }

    public static byte int2Byte(int t) {
        return (byte)t;
    }

    public static int byte2Int(byte b) {
        return b & 255;
    }

    public static byte[] object2Bytes(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return bytes;
    }

    public Object bytes2Object(byte[] bytes) {
        Object obj = null;

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (ClassNotFoundException | IOException var5) {
            var5.printStackTrace();
        }

        return obj;
    }

    public static boolean bytesEquals(byte[] data1, byte[] data2) {
        return Arrays.equals(data1, data2);
    }

    public static byte[] subBytes(byte[] data, int position, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(data, position, temp, 0, length);
        return temp;
    }

    public static byte[] bytesMerger(byte[] bytes1, byte[] bytes2) {
        byte[] bytes3 = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, bytes3, 0, bytes1.length);
        System.arraycopy(bytes2, 0, bytes3, bytes1.length, bytes2.length);
        return bytes3;
    }

    public static byte[] bytesMerger(byte byte1, byte[] bytes2) {
        byte[] bytes3 = new byte[1 + bytes2.length];
        bytes3[0] = byte1;
        System.arraycopy(bytes2, 0, bytes3, 1, bytes2.length);
        return bytes3;
    }

    public static byte[] bytesMerger(byte[] bytes1, byte byte2) {
        byte[] bytes3 = new byte[1 + bytes1.length];
        System.arraycopy(bytes1, 0, bytes3, 0, bytes1.length);
        bytes3[bytes3.length - 1] = byte2;
        return bytes3;
    }

    public static byte[] bytesMerger(byte[] bt1, byte[] bt2, byte[] bt3) {
        byte[] data = new byte[bt1.length + bt2.length + bt3.length];
        System.arraycopy(bt1, 0, data, 0, bt1.length);
        System.arraycopy(bt2, 0, data, bt1.length, bt2.length);
        System.arraycopy(bt3, 0, data, bt1.length + bt2.length, bt3.length);
        return data;
    }

    public static String byte2Hex(byte b) {
        String hex = Integer.toHexString(b & 255);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }

        return hex;
    }

    public static String bytes2Hex(byte[] b) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < b.length; ++i) {
            String hex = Integer.toHexString(b[i] & 255);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }

            sb.append(hex.toUpperCase());
        }

        return sb.toString();
    }

    public static String bytes2Hex2(byte[] bytes) {
        String strHex = "";
        StringBuilder stringBuilder = new StringBuilder();

        for(int n = 0; n < bytes.length; ++n) {
            strHex = Integer.toHexString(bytes[n] & 255);
            stringBuilder.append(strHex.length() == 1 ? "0" + strHex : strHex);
        }

        return stringBuilder.toString().trim();
    }

    public static byte hex2Byte(String hex) {
        return (byte)Integer.parseInt(hex, 16);
    }

    public static byte[] hex2Bytes(String hex) {
        if (hex.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hex.length() / 2];
            int j = 0;

            for(int i = 0; i < hex.length(); i += 2) {
                result[j++] = (byte)Integer.parseInt(hex.substring(i, i + 2), 16);
            }

            return result;
        }
    }

    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        byte[] var5 = bs;
        int var6 = bs.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            byte b = var5[var7];
            int bit = (b & 240) >> 4;
            sb.append(chars[bit]);
            bit = b & 15;
            sb.append(chars[bit]);
        }

        return sb.toString().trim();
    }

    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];

        for(int i = 0; i < bytes.length; ++i) {
            int n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte)(n & 255);
        }

        return new String(bytes);
    }

    public static String string2Unicode(String string) {
        StringBuilder unicode = new StringBuilder();

        for(int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            unicode.append("\"u").append(Integer.toHexString(c));
        }

        return unicode.toString();
    }

    public static String unicode2String(String unicode) {
        StringBuilder string = new StringBuilder();
        String[] hex = unicode.split("\"\"u");

        for(int i = 1; i < hex.length; ++i) {
            int data = Integer.parseInt(hex[i], 16);
            string.append((char)data);
        }

        return string.toString();
    }

    public static byte[] bytesReverse(byte[] bytes) {
        if (bytes != null && bytes.length > 1) {
            for(int i = 0; i <= bytes.length / 2 - 1; ++i) {
                byte temp1 = bytes[i];
                byte temp2 = bytes[bytes.length - i - 1];
                bytes[i] = temp2;
                bytes[bytes.length - i - 1] = temp1;
            }

            return bytes;
        } else {
            return bytes;
        }
    }

    public static String bytesReverse2HexStr(byte[] bytes) {
        return bytes2Hex(bytesReverse(bytes));
    }
}
