package edu.farmingdale.CSC490.Food;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class PythonServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(PythonServiceManager.class);

    private Process pythonProcess;
    private boolean isRunning = false;

    @Value("${PYTHON_API_PORT:8000}")
    private int PYTHON_SERVICE_PORT;
    private Integer currentPort = null;

    @Value("${PYTHON_FILE_PATH:/src/main/resources/AI/AI_ImageAnalyzer.py}")
    private String PYTHON_File_Path;


    /**
     * Kill any existing Python processes that might be running the AI service
     */
    private void killExistingPythonServices() {
        logger.info("Checking for existing Python services to kill...");
        try {
            // On Windows, kill Python processes that might be running our script
            String os = System.getProperty("os.name").toLowerCase();
            Process p;
            if (os.contains("win")){
                // List all Python processes
                p = Runtime.getRuntime().exec("wmic process where \"name='python.exe'\" get commandline,process id");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("AI_ImageAnalyzer.py") && line.contains("--server")) {
                        // Extract process ID from the line (it's usually at the end)
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 0) {
                            String pid = parts[parts.length - 1]; // PID should bethe last element
                            if (pid.matches("\\d+")) { // Check if it's a number
                                logger.info("Windows - Killing existing Python service with PID: {}", pid);
                                Runtime.getRuntime().exec("task kill /F /PID " + pid);
                            }
                        }
                    }
                }
            } else {
                // On Unix-like systems, use pgrep/pkill
                p = Runtime.getRuntime().exec("pgrep -f 'AI_ImageAnalyzer.py.*--server'");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String pid;
                while ((pid = reader.readLine()) != null){
                    if (pid.trim().matches("\\d+")) {
                        logger.info("Unix-like systems - Killing existing Python service with PID: {}", pid.trim());
                        Runtime.getRuntime().exec("kill -9 " + pid.trim());
                    }
                }
            }
            p.waitFor();
            // Give a moment for processes to terminate
            Thread.sleep(1000);
        } catch (Exception e) {
            logger.warn("Could not kill existing Python services: {}", e.getMessage());
        }
    }

    /**
     * Start the Python FastAPI service
     */
    public synchronized void startPythonService() {
        // Kill any existing Python services before starting a new one
        killExistingPythonServices();


        if (isRunning()) {
            logger.warn("Python service is already running.");
            return;
        }

        try {
            logger.info("Starting Python service on port {}...", PYTHON_SERVICE_PORT);
            // Get the project root path
            String projectRoot = System.getProperty("user.dir");
            if (projectRoot == null || projectRoot.isEmpty()) {
                // If you can't get the project directory, try using the relative path
                logger.warn("Could not get project directory. Using default path.");
                projectRoot = System.getProperty("user.home") + "/AEye-Systems";
            }

            // Build a command to start a Python service - for a Windows environment, using a new port
            String pythonScriptPath = projectRoot +  PYTHON_File_Path;
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

            logger.debug("Constructing Python service command: {}", String.join(" ", cmd));
            logger.debug("Looking for Python script at:{} " , pythonScriptPath);

            // Check if Python is available
            ProcessBuilder pb = new ProcessBuilder("python", "--version");

            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Python is not available in PATH. Please install Python and make sure it's in PATH.");
                return;
            }


            // Start the Python process
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            // Set the working directory as the project root
            processBuilder.directory(new File(projectRoot));
            // Merge error flows and standard output streams
            processBuilder.redirectErrorStream(true);

            // Add the Spring Boot configuration property to the child process environment variables
            Map<String, String> env = processBuilder.environment();
            env.putAll(System.getenv());


            pythonProcess = processBuilder.start();
            logger.info("Python service started successfully.");

            // Read Python service output asynchronously
            CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && pythonProcess.isAlive()) {
                        logger.debug("Python service output: {}", line);
                    }
                } catch (IOException e) {
                    logger.error("Error reading Python service output: {}", e.getMessage());
                }
            });

            // Read Python service error output asynchronously
           CompletableFuture.runAsync(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null && pythonProcess.isAlive()) {
                        logger.error("Python service error: {}", line);
                    }
                } catch (IOException e) {
                    logger.error("Error reading Python service error stream: {}", e.getMessage());
                }
            });

            // Wait for the service to start
            Thread.sleep(5000);


            if (pythonProcess.isAlive()) {
                isRunning = true;
                currentPort = PYTHON_SERVICE_PORT;  // Track the current port
                logger.info("Python service started successfully on port {}", PYTHON_SERVICE_PORT);
                
                // The monitoring process ends
                CompletableFuture.runAsync(() -> {

                    try {
                        pythonProcess.waitFor();
                        logger.error("Python service process ended with exit code: {}. Process was running on port {}", exitCode, currentPort);
                        isRunning = false;
                    } catch (InterruptedException e) {
                        logger.error("Python service monitoring interrupted {}", e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                });

            } else {
                logger.error("Failed to start Python service. Process exited with code: {}", pythonProcess.exitValue());
            }
        } catch (Exception e) {
            logger.error("Error starting Python service: {}", e.getMessage());
        }
    }

    /**
     * Stop the Python service
     */
    public synchronized void stopPythonService() {
        if (!isRunning()) {
            logger.warn("Python service is not running.");
            return;
        }

        try {
            if (pythonProcess != null && pythonProcess.isAlive()) {
                pythonProcess.destroy();
                logger.info("Python service terminated.");
                // Wait for the process to end
                if (!pythonProcess.waitFor(5, TimeUnit.SECONDS)) {
                    // If it does not end within 5 seconds, it is forced to terminate
                    pythonProcess.destroyForcibly();
                    // Wait another 2 seconds to ensure the process is over
                    pythonProcess.waitFor(2, TimeUnit.SECONDS);
                }
            }
            isRunning = false;
            if (currentPort != null) {
                logger.info("Python service on port {} stopped successfully.", currentPort);
                currentPort = null;
            }
        } catch (Exception e) {
            logger.error("Error stopping Python service: {}", e.getMessage());
        }
    }


    /**
     * Check if the Python service is running
     */
    public boolean isRunning() {
        return isRunning && pythonProcess != null && pythonProcess.isAlive();
    }


}
