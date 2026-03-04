package edu.farmingdale.CSC490.Food;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class PythonServiceManager {

    private Process pythonProcess;
    private boolean isRunning = false;
    private static final int PYTHON_SERVICE_PORT = 8001;
    /**
     * Start the Python FastAPI service
     */
    public synchronized boolean startPythonService() {
        if (isRunning()) {
            System.out.println("Python service is already running.");
            return true;
        }

        try {
            // 获取项目根目录路径
            String projectRoot = System.getProperty("user.dir");
            if (projectRoot == null || projectRoot.isEmpty()) {
                // 如果无法获取到项目目录，则尝试使用相对路径
                projectRoot = System.getProperty("user.home") + "/AEye-Systems";
            }

            // 构建启动Python服务的命令 - 针对Windows环境，使用新端口
            String pythonScriptPath = projectRoot + "/src/main/resources/AI/ollamaAI.py";
            String[] cmd;
            
            // 检测操作系统
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Windows系统使用cmd
                cmd = new String[]{"cmd", "/c", "python", pythonScriptPath, "--server", String.valueOf(PYTHON_SERVICE_PORT)};
            } else {
                // Unix/Linux/Mac系统使用shell
                cmd = new String[]{"sh", "-c", "python " + pythonScriptPath + " --server " + PYTHON_SERVICE_PORT};
            }

            System.out.println("Starting Python service with command: " + String.join(" ", cmd));
            System.out.println("Looking for Python script at: " + pythonScriptPath);
            System.out.println("Using port: " + PYTHON_SERVICE_PORT);

            // 检查Python是否可用
            ProcessBuilder pb = new ProcessBuilder("python", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python is not available in PATH. Please install Python and make sure it's in PATH.");
                return false;
            }

            // 启动Python进程
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.directory(new File(projectRoot)); // 设置工作目录为项目根目录
            processBuilder.redirectErrorStream(true); // 合并错误流和标准输出流
            pythonProcess = processBuilder.start();

            // 异步读取Python服务输出
            CompletableFuture<Void> outputFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && pythonProcess.isAlive()) {
                        System.out.println("[Python Service] " + line);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading Python service output: " + e.getMessage());
                }
            });

            // 等待服务启动
            Thread.sleep(5000); // 给服务更多时间来启动

            if (pythonProcess.isAlive()) {
                isRunning = true;
                System.out.println("Python service started successfully on port " + PYTHON_SERVICE_PORT);
                
                // 监控进程结束
                CompletableFuture.runAsync(() -> {
                    try {
                        pythonProcess.waitFor();
                        System.out.println("Python service process ended unexpectedly");
                        isRunning = false;
                    } catch (InterruptedException e) {
                        System.out.println("Python service monitoring interrupted");
                    }
                });
                
                return true;
            } else {
                System.err.println("Failed to start Python service. Process exited with code: " + pythonProcess.exitValue());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error starting Python service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 停止Python服务
     */
    public synchronized boolean stopPythonService() {
        if (!isRunning()) {
            System.out.println("Python service is not running.");
            return true;
        }

        try {
            if (pythonProcess != null && pythonProcess.isAlive()) {
                pythonProcess.destroy(); // 尝试优雅地终止进程
                
                // 等待进程结束
                if (!pythonProcess.waitFor(5, TimeUnit.SECONDS)) {
                    pythonProcess.destroyForcibly(); // 如果5秒内未结束，则强制终止
                    pythonProcess.waitFor(2, TimeUnit.SECONDS); // 再等2秒确保进程结束
                }
            }
            isRunning = false;
            System.out.println("Python service stopped successfully");
            return true;
        } catch (Exception e) {
            System.err.println("Error stopping Python service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if the Python service is running
     */
    public boolean isRunning() {
        return isRunning && pythonProcess != null && pythonProcess.isAlive();
    }

    /**
     * Ensure that the service is properly stopped when the app is closed
     */
    public void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopPythonService));
    }
    
    /**
     * Get the URL of the Python service
     */
    public String getServiceUrl() {
        return "http://localhost:" + PYTHON_SERVICE_PORT;
    }
}
