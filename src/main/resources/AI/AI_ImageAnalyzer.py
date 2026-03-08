import requests
import base64
import json
import sys
import os
import re
import tempfile
import logging
import signal
import atexit
import asyncio
from contextlib import asynccontextmanager
from dotenv import load_dotenv
from fastapi import FastAPI, File, UploadFile, Form
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import configparser
from pathlib import Path

# Configure logging
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')

logger = logging.getLogger(__name__)

def load_config():
    """Read the configuration file in the project"""
    global config_file
    # Get the directory where this script is located
    script_dir = Path(__file__).parent.absolute()

    config_file = script_dir.parent / 'application.properties'

    # Create a custom config parser that can handle Java properties format
    config = {}

    # Read the application.properties file directly
    with open(config_file, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith('#'):
                # Handle Java properties format (key=value pairs)
                if '=' in line:
                    key, value = line.split('=', 1)
                    key = key.strip()
                    value = value.strip()

                    # Handle placeholder replacement like ${VARIABLE:default}
                    import re
                    def replace_placeholders(match):
                        var_name = match.group(1)
                        default_val = match.group(2) if match.group(2) else ''
                        return os.getenv(var_name, default_val)

                    # Replace placeholders like ${VAR:default}
                    value = re.sub(r'\$\{([^}:]+):?([^}]*)\}', replace_placeholders, value)

                    config[key] = value
     # Set environment variables based on config values
    for key, value in config.items():
        # Convert property keys to environment variable names (uppercase with underscores)
        env_var_name = key.upper().replace('.', '_')
        if not os.environ.get(env_var_name):
            os.environ[env_var_name] = value


    return config

config = load_config()


# Global variable to track server state
server_should_exit = False


# Store the lifespan in a variable for later use
@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting up AI Image Analyzer service...")
    yield
    logger.info("Shutting down AI Image Analyzer service...")

app = FastAPI(title="AI Image Analyzer", version="1.1.0", lifespan=lifespan)

class AnalyzeRequest(BaseModel):
    image_path: str
    prompt_file: str

def _verify_file_exists(path: str, name: str) -> tuple[bool, str]:
    """Verify file exists and return (is_valid, error_message)"""
    if not os.path.exists(path):
        error_msg = f"ERROR: {name} file does not exist: {path}"
        logger.error(error_msg)
        return False, error_msg
    return True, ""

def _read_and_encode_image(img_path: str) -> tuple[str, str]:
    """Read and encode image, return (encoded_image, error_message)"""
    with open(img_path, "rb") as f:
        image = base64.b64encode(f.read()).decode('utf-8')

    if not image:
        error_msg = "ERROR: No image provided or empty image file"
        logger.error(error_msg)
        return "", error_msg

    return image, ""

def _read_prompt(prompt_path: str) -> tuple[str, str]:
    """Read prompt from file, return (prompt, error_message)"""
    with open(prompt_path, 'r', encoding='utf-8') as f:
        prompt = f.read().strip()

    if not prompt:
        error_msg = "ERROR: Prompt file is empty"
        logger.error(error_msg)
        return "", error_msg

    return prompt, ""

def _extract_json_response(text: str, img_path: str) -> str:
    """Extract JSON from response text"""
    logger.debug(f"API response: {text[:200]}...")  # Log first 200 chars of response
    match = re.search(r'\{.*\}', text, re.DOTALL)

    if match:
        try:
            json_content = json.loads(match.group())
            logger.info(f"Successfully processed image analysis for {img_path}")
            return json.dumps(json_content)
        except json.JSONDecodeError as e:
            error_msg = f"Invalid JSON in response: {str(e)}, content: {match.group()}"
            logger.error(error_msg)
            return error_msg
    else:
        error_msg = f"No JSON in response: {text}"
        logger.warning(error_msg)
        return error_msg


def analyze(img_path: str, prompt_path: str) -> str:
    """AI model analyzes images using either local Ollama API or remote API based on configuration"""
    logger.info(f"Starting analysis for image: {img_path} with prompt: {prompt_path}")

    # Verify file existence
    for path, name in [(img_path, "image"), (prompt_path, "prompt")]:
        is_valid, error = _verify_file_exists(path, name)
        if not is_valid:
            return error

    # Read and encode image
    image, error = _read_and_encode_image(img_path)
    if error:
        return error

    # Read prompt
    prompt, error = _read_prompt(prompt_path)
    if error:
        return error

    logger.debug(f"Sending request with prompt: {prompt[:100]}...")  # Log first 100 chars of prompt

    # Determine whether to use remote API based on environment variables
    remote_api_key = os.getenv("REMOTE_API_KEY")
    if remote_api_key:
        # Use remote API
        return _analyze_with_remote_api(image, prompt, img_path, remote_api_key)
    else:
        # Use local Ollama API
        return _analyze_with_local_api(image, prompt, img_path)

def _analyze_with_local_api(image: str, prompt: str, img_path: str) -> str:
    """Analyze using local Ollama API"""
    logger.info("Using local Ollama API")

    url = os.getenv("LOCAL_API_URL")
    model = os.getenv("LOCAL_API_MODEL")

    logger.debug(f"Using local Ollama API with URL: {url} and model: {model}")

    if not url or url == "None":
            error_msg = "LOCAL_API_URL is not set properly. Please check your environment variables."
            logger.error(error_msg)
            return error_msg

    try:
        resp = requests.post(
            f"{url}/api/generate",
            json={
                "model": model,
                "prompt": prompt,
                "images": [image],
                "stream": False
            },
            timeout=60
        )

        if resp.status_code != 200:
            error_msg = f"Ollama API request failed: {resp.status_code} - {resp.text}"
            logger.error(error_msg)
            return error_msg

        result = resp.json()
        if 'error' in result:
            error_msg = f"{{\"error\": \"from Ollama: {result['error']}\"}}"
            logger.error(f"Ollama API returned error: {result['error']}")
            return error_msg

        # Extract JSON response
        text = result.get("response", "").strip()
        return _extract_json_response(text, img_path)

    except requests.exceptions.ConnectionError:
        error_msg = "Cannot connect to Ollama server"
        logger.error(error_msg)
        return error_msg
    except requests.exceptions.Timeout:
        error_msg = "Request to Ollama API timed out"
        logger.error(error_msg)
        return error_msg
    except json.JSONDecodeError:
        error_msg = f"Invalid JSON in response: {match.group() if match else text}"
        logger.error(error_msg)
        return error_msg
    except Exception as e:
        error_msg = f"Unexpected error during analysis: {str(e)}"
        logger.exception(error_msg)
        return error_msg

def _analyze_with_remote_api(image: str, prompt: str, img_path: str, api_key: str) -> str:
    """Analyze using remote API such as OpenAI"""

    logger.info("Using remote API")

    # Use environment variables or defaults for API settings
    base_url = os.getenv("REMOTE_API_BASE_URL")
    model = os.getenv("REMOTE_API_MODEL")

    logger.debug(f"Using remote API with base URL: {base_url} and model: {model}")

    # Prepare the payload for the remote API
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": model,
        "messages": [
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": prompt
                    },
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": f"data:image/jpeg;base64,{image}",
                            "detail": "high"
                        }
                    }
                ]
            }
        ],
        "max_tokens": 1000
    }

    try:
        # Make the API request
        response = requests.post(
            f"{base_url}/chat/completions",
            headers=headers,
            json=payload,
            timeout=60
        )

        if response.status_code != 200:
            error_msg = f"Remote API request failed: {response.status_code} - {response.text}"
            logger.error(error_msg)
            return error_msg

        result = response.json()

        if 'error' in result:
            error_msg = f"Remote API returned error: {result['error']}"
            logger.error(error_msg)
            return error_msg

        # Extract the response text
        text = result['choices'][0]['message']['content'].strip()
        return _extract_json_response(text, img_path)

    except requests.exceptions.ConnectionError:
        error_msg = "Cannot connect to remote API server"
        logger.error(error_msg)
        return error_msg
    except requests.exceptions.Timeout:
        error_msg = "Request to remote API timed out"
        logger.error(error_msg)
        return error_msg
    except json.JSONDecodeError:
        error_msg = f"Invalid JSON in response: {match.group() if match else text}"
        logger.error(error_msg)
        return error_msg
    except Exception as e:
        error_msg = f"Unexpected error during analysis: {str(e)}"
        logger.exception(error_msg)
        return error_msg

def _process_analysis_response(result: str) -> dict:
    """Process analysis result and return appropriate response"""
    try:
        parsed_result = json.loads(result)
        logger.info(f"Analysis completed successfully: {parsed_result}")
        return parsed_result
    except json.JSONDecodeError:
        logger.error(f"Invalid JSON result: {result}")
        return {"error": f"Invalid JSON result: {result}"}

@app.post("/analyze")
async def analyze_endpoint(request: AnalyzeRequest):
    """API endpoint for analyzing images by path"""
    logger.info(f"Received analyze request for image: {request.image_path} with prompt: {request.prompt_file}")
    result = analyze(request.image_path, request.prompt_file)
    return _process_analysis_response(result)

@app.post("/analyze-upload")
async def analyze_upload(image: UploadFile = File(...), prompt_file: str = Form(...)):
    """API endpoint for analyzing uploaded image files"""
    logger.info(f"Received analyze-upload request for image: {image.filename} with prompt: {prompt_file}")
    temp_dir = None
    temp_image_path = None
    try:
        # Create a temporary file to save the uploaded image
        temp_dir = tempfile.mkdtemp()
        temp_image_path = os.path.join(temp_dir, image.filename)
        logger.debug(f"Created temporary file: {temp_image_path}")

        # Save the uploaded file
        content = await image.read()
        with open(temp_image_path, "wb") as f:
            f.write(content)

        # Now call the analyze function
        result = analyze(temp_image_path, prompt_file)
        return _process_analysis_response(result)
    except Exception as e:
        error_msg = f"Unexpected error during analysis: {str(e)}"
        logger.exception(error_msg)
        return JSONResponse(status_code=500, content={"error": error_msg})
    finally:
        # Ensure cleanup happens even if there's an error
        if temp_image_path and os.path.exists(temp_image_path):
            try:
                os.remove(temp_image_path)
                logger.debug(f"Removed temporary image file: {temp_image_path}")
            except:
                logger.warning(f"Failed to remove temporary image file: {temp_image_path}")
        if temp_dir and os.path.exists(temp_dir):
            try:
                os.rmdir(temp_dir)
                logger.debug(f"Removed temporary directory: {temp_dir}")
            except:
                logger.warning(f"Failed to remove temporary directory: {temp_dir}")

@app.get("/health")
async def health():
    """Health check endpoint"""
    logger.info("Health check requested")
    return {"status": "healthy"}

def signal_handler(signum, frame):
    """Handle termination signals"""
    global server_should_exit
    logger.info(f"Received signal {signum}, initiating graceful shutdown...")
    server_should_exit = True
    # Perform any cleanup here if needed
    sys.exit(0)

# Register signal handlers for graceful shutdown
signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)

# Also register cleanup at exit
def cleanup_on_exit():
    logger.info("Performing cleanup on exit...")

atexit.register(cleanup_on_exit)

# Maintain command line compatibility
if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "--server":
        import uvicorn
        port = int(sys.argv[2]) if len(sys.argv) > 2 else 8000
        logger.info(f"Starting server on port {port}")

        # Use Uvicorn with proper signal handling
        config = uvicorn.Config(
            app,
            host="localhost",
            port=port,
            log_level="info",
            reload=False  # Disable reload in production
        )
        server = uvicorn.Server(config)

        try:
            # Run the server
            import asyncio
            asyncio.run(server.serve())
        except KeyboardInterrupt:
            logger.info("Server interrupted by user")
        except Exception as e:
            logger.error(f"Server error: {e}")
        finally:
            logger.info("Server shutdown complete")
    else:
        if len(sys.argv) < 3:
            print("Usage: python AI_ImageAnalyzer.py <image_path> <prompt_file>", file=sys.stderr)
            print("Or run as server: python AI_ImageAnalyzer.py --server [port]", file=sys.stderr)
            sys.exit(1)

        result = analyze(sys.argv[1], sys.argv[2])
        print(result)






