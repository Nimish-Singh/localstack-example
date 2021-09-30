package io;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class TestHelper implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static final Lock lock = new ReentrantLock();
    private static boolean started = false;

    private static void runCmd(String... cmd) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(cmd).start();
        int returnCode = process.waitFor();
        if (returnCode != 0) {
            throw new RuntimeException("Failure! Return code: " + returnCode);
        }
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        System.out.println("Trying to start localstack");
        lock.lock();
        try {
            if (!started) {
                System.out.println("Starting fresh localstack");
                runCmd("docker-compose", "up", "-d");
                started = true;
                context.getRoot().getStore(GLOBAL).put(this.getClass().getName(), this);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws Throwable {
        System.out.println("Closing localstack");
        runCmd("docker-compose", "down");
    }
}
