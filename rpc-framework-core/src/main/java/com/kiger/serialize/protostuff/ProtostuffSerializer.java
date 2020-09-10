package com.kiger.serialize.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.kiger.exception.SerializeException;
import com.kiger.serialize.Serializer;

/**
 * @author zk_kiger
 * @date 2020/7/12
 */

public class ProtostuffSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        RuntimeSchema<Object> schema;
        LinkedBuffer buffer;
        byte[] result;
        try {
            schema = RuntimeSchema.createFrom(Object.class);
            buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
            result = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializeException("序列化失败");
        }
        return result;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        RuntimeSchema<T> schema;
        T newInstance;
        try {
            schema = RuntimeSchema.createFrom(clazz);
            newInstance = clazz.newInstance();
            ProtostuffIOUtil.mergeFrom(bytes, newInstance, schema);
        } catch (Exception e) {
            throw new SerializeException("反序列化失败");
        }
        return newInstance;
    }
}
