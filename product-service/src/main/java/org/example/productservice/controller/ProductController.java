package org.example.productservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import convention.result.Result;
import convention.result.Results;
import org.example.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @GetMapping("/products/{id}")
    @SentinelResource(
            value = "getProductById",
            blockHandlerClass = ProductBlockHandler.class, // 处理阻塞的类
            blockHandler = "handleGetProductByIdBlock" // 具体处理方法
    )
    public Result<Map<String, Object>> getProductById(@PathVariable("id") Long id) {
        return Results.success(productService.getProductById(id));
    }
}
