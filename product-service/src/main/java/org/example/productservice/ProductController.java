package org.example.productservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @GetMapping("/products/{id}")
    public Map<String, Object> getProductById(@PathVariable("id") Long id) {
        log.info("成功接收到获取用户信息的请求, 用户ID: {}", id);
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", "apple mac pro 2T");
        product.put("price", 9999.00);
        return product;
    }
}
