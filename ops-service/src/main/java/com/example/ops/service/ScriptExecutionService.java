package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.io.*;

/**
 * 脚本执行服务
 * 支持 Shell、PowerShell、Batch 脚本执行
 */
@Service
public class ScriptExecutionService {

    /**
     * 脚本类型
     */
    public enum ScriptType {
        SHELL,
        POWERSHELL,
        BATCH,
        PYTHON
    }

    /**
     * 执行结果
     */
    public static class ExecutionResult {
        private final int exitCode;
        private final String output;
        private final String error;
        private final long duration;

        public ExecutionResult(int exitCode, String output, String error, long duration) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
            this.duration = duration;
        }

        public int getExitCode() { return exitCode; }
        public String getOutput() { return output; }
        public String getError() { return error; }
        public long getDuration() { return duration; }
        public boolean isSuccess() { return exitCode == 0; }
    }

    /**
     * 执行脚本
     */
    public ExecutionResult execute(String script, ScriptType type) {
        long startTime = System.currentTimeMillis();
        try {
            ProcessBuilder pb = getProcessBuilder(script, type);
            Process process = pb.start();

            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());

            int exitCode = process.waitFor();
            long duration = System.currentTimeMillis() - startTime;

            return new ExecutionResult(exitCode, output, error, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new ExecutionResult(-1, "", e.getMessage(), duration);
        }
    }

    /**
     * 执行远程脚本 (通过 SSH)
     */
    public ExecutionResult executeRemote(String script, ScriptType type, String host, String user) {
        // 简化实现：本地执行
        // 实际应使用 JSch 或 Apache MINA SSH 连接远程主机
        return execute(script, type);
    }

    private ProcessBuilder getProcessBuilder(String script, ScriptType type) {
        ProcessBuilder pb;
        switch (type) {
            case POWERSHELL:
                pb = new ProcessBuilder("powershell", "-Command", script);
                break;
            case BATCH:
                pb = new ProcessBuilder("cmd", "/c", script);
                break;
            case PYTHON:
                pb = new ProcessBuilder("python", "-c", script);
                break;
            case SHELL:
            default:
                pb = new ProcessBuilder("bash", "-c", script);
                break;
        }
        pb.redirectErrorStream(false);
        return pb;
    }

    private String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
