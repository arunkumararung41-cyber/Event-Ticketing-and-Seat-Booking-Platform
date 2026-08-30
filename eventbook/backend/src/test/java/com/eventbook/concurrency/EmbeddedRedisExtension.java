package com.eventbook.concurrency;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import redis.embedded.RedisServer;

/**
 * Spins up a real (embedded) Redis instance on port 6380 for the duration of the
 * concurrency test class, so the seat-locking race condition is exercised against
 * an actual Redis engine rather than a mocked one.
 */
public class EmbeddedRedisExtension implements BeforeAllCallback, AfterAllCallback {

    private RedisServer redisServer;

    @Override
    public void beforeAll(ExtensionContext context) {
        redisServer = new RedisServer(6380);
        redisServer.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
