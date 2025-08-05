package org.example.productservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.productservice.dao.entity.ProductDO;
import org.example.productservice.dao.mapper.ProductMapper;
import org.example.productservice.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    @Override
    public Map<String, Object> getProductById(Long id) {
        return BeanUtil.beanToMap(baseMapper.selectById(id));
    }
}
