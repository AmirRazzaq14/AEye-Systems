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
    private static final int PYTHON_SERVICE_PORT = 8000;
    /**
     * Start the Python FastAPI service
     */
    public synchronized boolean startPythonService() {
        if (isRunning()) {
            System.out.println("Python service is already running.");
            return true;
        }

        try {
            // Get the project root path
            String projectRoot = System.getProperty("user.dir");
            if (projectRoot == null || projectRoot.isEmpty()) {
                // If you can't get the project directory, try using the relative path
                projectRoot = System.getProperty("user.home") + "/AEye-Systems";
            }

            // Build a command to start a Python service - for a Windows environment, using a new port
            String pythonScriptPath = projectRoot + "/src/main/resources/AI/ollamaAI.py";
            String[] cmd;
            
            // Detect the operating system
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Windows systems use cmd
                cmd = new String[]{"cmd", "/c", "python", pythonScriptPath, "--server", String.valueOf(PYTHON_SERVICE_PORT)};
            } else {
                // Unix/Linux/Mac systems use shells
                cmd = new String[]{"sh", "-c", "python " + pythonScriptPath + " --server " + PYTHON_SERVICE_PORT};
            }

            System.out.println("Starting Python service with command: " + String.join(" ", cmd));
            System.out.println("Looking for Python script at: " + pythonScriptPath);
            System.out.println("Using port: " + PYTHON_SERVICE_PORT);

            // Check if Python is available
            ProcessBuilder pb = new ProcessBuilder("python", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python is not available in PATH. Please install Python and make sure it's in PATH.");
                return false;
            }

            // Start the Python process
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            // Set the working directory as the project root
            processBuilder.directory(new File(projectRoot));
            // Merge error flows and standard output streams
            processBuilder.redirectErrorStream(true);
            pythonProcess = processBuilder.start();

            // Read Python service output asynchronously
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

            // Wait for the service to start
            Thread.sleep(5000);

            if (pythonProcess.isAlive()) {
                isRunning = true;
                System.out.println("Python service started successfully on port " + PYTHON_SERVICE_PORT);
                
                // The monitoring process ends
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
     * Stop the Python service
     */
    public synchronized boolean stopPythonService() {
        if (!isRunning()) {
            System.out.println("Python service is not running.");
            return true;
        }

        try {
            if (pythonProcess != null && pythonProcess.isAlive()) {
                pythonProcess.destroy(); // Try to terminate the process
                
                // Wait for the process to end
                if (!pythonProcess.waitFor(5, TimeUnit.SECONDS)) {
                    // If it does not end within 5 seconds, it is forced to terminate
                    pythonProcess.destroyForcibly();
                    // Wait another 2 seconds to ensure the process is over
                    pythonProcess.waitFor(2, TimeUnit.SECONDS);
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
