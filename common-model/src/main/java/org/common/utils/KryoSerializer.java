package org.common.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();

        // 是否要求注册类。先设为 false，方便你调试
        kryo.setRegistrationRequired(false);

        // 可选：提高引用对象处理能力
        kryo.setReferences(true);

        return kryo;
    });

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);

        try {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.writeClassAndObject(output, obj);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        } finally {
            output.close();
        }
    }

    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);

        try {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            return kryo.readClassAndObject(input);
        } finally {
            input.close();
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Object obj = deserialize(bytes);
        return clazz.cast(obj);
    }
}