package io;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;

public class TestHelper implements BeforeAllCallback, AfterAllCallback {

    private static void runCmd(String... cmd) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(cmd).start();
        int returnCode = process.waitFor();
        if (returnCode != 0) {
            throw new RuntimeException("Failure! Return code: " + returnCode);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        runCmd("docker-compose", "up", "-d");
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        runCmd("docker-compose", "down");
    }
}
