package org.example.productservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.productservice.dao.entity.ProductDO;

import java.util.Map;

public interface ProductService extends IService<ProductDO> {
    Map<String, Object> getProductById(Long id);
}
