package org.example.productservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.productservice.dao.entity.ProductDO;
import org.example.productservice.dao.mapper.ProductMapper;
import org.example.productservice.service.ProductService;
import org.example.productservice.util.NullValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "product:";
    private static final long CACHE_NULL_TTL = 2;
    private static final long CACHE_TTL = 30;

    @Override
    public Map<String, Object> getProductById(Long id) {
        String key = CACHE_KEY_PREFIX + id;

        // 1. 从缓存获取
        Object cachedProduct = redisTemplate.opsForValue().get(key);
        if (cachedProduct != null) {
            if ("null".equals(cachedProduct)) {
                return null;
            }
            return (Map<String, Object>) cachedProduct;
        }

        // 2. 缓存未命中，查询数据库
        System.out.println("==================== DB QUERY (PRODUCT) ====================");
        ProductDO product = baseMapper.selectById(id);
        System.out.println("============================================================");

        if (product != null) {
            Map<String, Object> productMap = BeanUtil.beanToMap(product);
            // 3. 重建缓存
            redisTemplate.opsForValue().set(key, productMap, CACHE_TTL, TimeUnit.MINUTES);
            return productMap;
        } else {
            // 4. 防穿透：缓存空值
            redisTemplate.opsForValue().set(key, NullValue.INSTANCE, CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
    }
}
