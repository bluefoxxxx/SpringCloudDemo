package org.example.productservice.config;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 自定义 Redis value 序列化器，使用 Fastjson2
 * @param <T>
 */
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    static final Filter AUTO_TYPE_FILTER = JSONReader.autoTypeFilter(
            // 按需加上需要支持自动类型的类名前缀，范围越小越安全
            "org.example.userservice.dao.entity"
    );

    private final Class<T> clazz;

    public FastJsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONBytes(t, JSONWriter.Feature.WriteClassName);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        // 传入 auto-type-filter 来保证安全
        return JSON.parseObject(bytes, clazz, AUTO_TYPE_FILTER, JSONReader.Feature.SupportAutoType);
    }
}
