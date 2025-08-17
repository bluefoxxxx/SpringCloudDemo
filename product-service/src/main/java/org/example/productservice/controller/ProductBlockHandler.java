package org.example.productservice.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import convention.result.Result;
import convention.result.Results;

/**
 * 一个专门用于处理 Sentinel 阻塞异常的类
 * 注意：所有的方法必须是 static 的
 */
public class ProductBlockHandler {

    /**
     * 为 getProductById 方法定制的阻塞处理器
     * @param id          方法的原始参数
     * @param ex          触发的阻塞异常
     * @return 返回给前端的 Result 对象
     */
    public static Result<Void> handleGetProductByIdBlock(Long id, BlockException ex) {
        System.err.println("=== SENTINEL BLOCK: 对商品ID " + id + " 的访问过于频繁 ===");
        System.err.println("=== 触发的规则: " + ex.getRule() + " ===");
        return Results.failure("A000429", "请求过于频繁，请稍后再试");
    }
}
