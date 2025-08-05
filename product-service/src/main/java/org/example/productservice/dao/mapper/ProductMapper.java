package org.example.productservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.productservice.dao.entity.ProductDO;

@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {
}
