package org.example.orderservice.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);       // 核心线程数
        executor.setMaxPoolSize(20);        // 最大线程数
        executor.setQueueCapacity(200);     // 队列容量
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        // 使用 TtlExecutors 对我们创建的线程池进行包装,使线程具备上下文传递能力
        return TtlExecutors.getTtlExecutor(executor);
        // return executor;
    }
}
