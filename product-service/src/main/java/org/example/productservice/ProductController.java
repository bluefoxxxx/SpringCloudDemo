package org.example.productservice;

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
    public Map<String, Object> getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }
}
