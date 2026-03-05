import requests
import base64
import json
import sys
import os
import re
import tempfile
import logging
from fastapi import FastAPI, File, UploadFile, Form
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = FastAPI(title="Ollama AI Image Analyzer", version="1.0.0")

class AnalyzeRequest(BaseModel):
    image_path: str
    prompt_file: str

def analyze(img_path: str, prompt_path: str) -> str:
    """AI model analyzes images"""
    logger.info(f"Starting analysis for image: {img_path} with prompt: {prompt_path}")

    # Verify file existence
    for path, name in [(img_path, "image"), (prompt_path, "prompt")]:
        if not os.path.exists(path):
            error_msg = f"ERROR: {name} file does not exist: {path}"
            logger.error(error_msg)
            return error_msg

    # Read image and encode
    with open(img_path, "rb") as f:
        image = base64.b64encode(f.read()).decode('utf-8')

    if not image:
        error_msg = "ERROR: No image provided or empty image file"
        logger.error(error_msg)
        return error_msg

    # Read prompt
    with open(prompt_path, 'r', encoding='utf-8') as f:
        prompt = f.read().strip()

    if not prompt:
        error_msg = "ERROR: Prompt file is empty"
        logger.error(error_msg)
        return error_msg

    logger.debug(f"Sending request to Ollama API with prompt: {prompt[:100]}...")  # Log first 100 chars of prompt

    # Call Ollama API
    try:
        resp = requests.post(
            "http://localhost:11434/api/generate",
            json={
                "model": "llava",
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
        logger.debug(f"Ollama API response: {text[:200]}...")  # Log first 200 chars of response
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

@app.post("/analyze")
async def analyze_endpoint(request: AnalyzeRequest):
    """API endpoint for analyzing images by path"""
    logger.info(f"Received analyze request for image: {request.image_path} with prompt: {request.prompt_file}")
    result = analyze(request.image_path, request.prompt_file)

    try:
        # Parse the result from analyze function
        parsed_result = json.loads(result)

        logger.info(f"Analysis completed successfully: {parsed_result}")
        return {"result": parsed_result}
    except json.JSONDecodeError:
        # If result is not valid JSON, it might be an error message in string format
        # In this case, the result is probably an error string wrapped in quotes
        logger.error(f"Invalid JSON result: {result}")
        return JSONResponse(status_code=400, content={"error": f"Invalid JSON result: {result}"})

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

        try:
            # Parse the result from analyze function
            parsed_result = json.loads(result)
            logger.info(f"Analysis completed successfully: {parsed_result}")
            return {"result": parsed_result}
        except json.JSONDecodeError:
            # If result is not valid JSON, it might be an error message in string format
            error_msg = f"Invalid JSON in response: {result}"
            logger.error(error_msg)
            return JSONResponse(status_code=400, content={error_msg})
    except Exception as e:
        error_msg  = f"Unexpected error during analysis: {str(e)}"
        logger.exception(error_msg)
        return JSONResponse(status_code=500, content={error_msg})
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

# Maintain command line compatibility
if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "--server":
        import uvicorn
        port = int(sys.argv[2]) if len(sys.argv) > 2 else 8000
        logger.info(f"Starting server on port {port}")
        uvicorn.run(app, host="localhost", port=port)
    else:
        if len(sys.argv) < 3:
            print("Usage: python ollamaAI.py <image_path> <prompt_file>", file=sys.stderr)
            print("Or run as server: python ollamaAI.py --server [port]", file=sys.stderr)
            sys.exit(1)

        result = analyze(sys.argv[1], sys.argv[2])
        print(result)




