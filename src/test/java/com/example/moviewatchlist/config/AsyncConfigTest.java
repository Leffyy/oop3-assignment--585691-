package com.example.moviewatchlist.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@Import(AsyncConfig.class)
class AsyncConfigTest {

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Test
    void contextLoads_andTaskExecutorBeanExists() {
        assertNotNull(taskExecutor);
    }
}