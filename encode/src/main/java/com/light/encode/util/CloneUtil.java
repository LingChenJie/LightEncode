package com.light.encode.util;

import com.google.gson.Gson;
import com.light.encode.ios8583.Field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

@SuppressWarnings({"unchecked", "unused"})
public final class CloneUtil {

    private CloneUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static <T extends Serializable> T deepClone(final Map<String, Field> src) throws Exception {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            byte[] byteArray = byteOut.toByteArray();
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteArray);
            in = new ObjectInputStream(byteIn);
            return (T) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(out);
            close(in);
        }
        throw new RuntimeException("DeepClone fail");
    }

    public static <T> T deepClone(final T data, final Type type) {
        try {
            Gson gson = new Gson();
            String jsonString = gson.toJson(data);
            return gson.fromJson(jsonString, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 关闭流
     */
    private static void close(Closeable src) {
        if (src != null) {
            try {
                src.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}