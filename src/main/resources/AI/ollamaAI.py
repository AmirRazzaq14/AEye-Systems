import requests
import base64
import json
import sys
import os
import re
import tempfile
from fastapi import FastAPI, File, UploadFile, Form
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional

app = FastAPI(title="Ollama AI Image Analyzer", version="1.0.0")

class AnalyzeRequest(BaseModel):
    image_path: str
    prompt_file: str

def analyze(img_path: str, prompt_path: str) -> str:
    """AI model analyzes images"""

    # Verify file existence
    for path, name in [(img_path, "image"), (prompt_path, "prompt")]:
        if not os.path.exists(path):
            return f"ERROR: {name} file does not exist: {path}"

    # Read image and encode
    with open(img_path, "rb") as f:
        image = base64.b64encode(f.read()).decode('utf-8')

    if not image:
        return "ERROR: No image provided or empty image file"

    # Read prompt
    with open(prompt_path, 'r', encoding='utf-8') as f:
        prompt = f.read().strip()

    if not prompt:
        return "ERROR: Prompt file is empty"

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
            return f"{{\"error\": \"Request failed with status {resp.status_code}: {resp.text}\"}}"

        result = resp.json()
        if 'error' in result:
            return f"{{\"error\": \"from Ollama: {result['error']}\"}}"

        # Extract JSON response
        text = result.get("response", "").strip()
        match = re.search(r'\{.*\}', text, re.DOTALL)

        if match:
            try:
                json_content = json.loads(match.group())
                return json.dumps(json_content)
            except json.JSONDecodeError as e:
                return f"{{\"error\": \"Invalid JSON in response: {str(e)}, content: {match.group()}\"}}"
        else:
            return f"{{\"error\": \"No JSON in response: {text}\"}}"

    except requests.exceptions.ConnectionError:
        return "{\"error\": \"Cannot connect to Ollama server\"}"
    except requests.exceptions.Timeout:
        return "{\"error\": \"Request timed out\"}"
    except json.JSONDecodeError:
        return f"{{\"error\": \"Invalid JSON in response: {match.group() if match else text}\"}}"
    except Exception as e:
        return f"{{\"error\": \"{str(e)}\"}}"

@app.post("/analyze")
async def analyze_endpoint(request: AnalyzeRequest):
    """API endpoint for analyzing images by path"""
    result = analyze(request.image_path, request.prompt_file)

    try:
        # Parse the result from analyze function
        parsed_result = json.loads(result)

        # Check if result is an error (contains error key)
        if 'error' in parsed_result:
            return JSONResponse(status_code=400, content=parsed_result)
        else:
            return {"result": parsed_result}
    except json.JSONDecodeError:
        # If result is not valid JSON, it might be an error message in string format
        # In this case, the result is probably an error string wrapped in quotes
        return JSONResponse(status_code=400, content={"error": f"Invalid JSON result: {result}"})

@app.post("/analyze-upload")
async def analyze_upload(image: UploadFile = File(...), prompt_file: str = Form(...)):
    """API endpoint for analyzing uploaded image files"""
    temp_dir = None
    temp_image_path = None
    try:
        # Create a temporary file to save the uploaded image
        temp_dir = tempfile.mkdtemp()
        temp_image_path = os.path.join(temp_dir, image.filename)

        # Save the uploaded file
        content = await image.read()
        with open(temp_image_path, "wb") as f:
            f.write(content)

        # Now call the analyze function
        result = analyze(temp_image_path, prompt_file)

        try:
            # Parse the result from analyze function
            parsed_result = json.loads(result)

            # Check if result is an error (contains error key)
            if 'error' in parsed_result:
                return JSONResponse(status_code=400, content=parsed_result)
            else:
                return {"result": parsed_result}
        except json.JSONDecodeError:
            # If result is not valid JSON, it might be an error message in string format
            return JSONResponse(status_code=400, content={"error": f"Invalid JSON result: {result}"})
    except Exception as e:
        return JSONResponse(status_code=500, content={"error": f"Internal Server Error: {str(e)}"})
    finally:
        # Ensure cleanup happens even if there's an error
        if temp_image_path and os.path.exists(temp_image_path):
            try:
                os.remove(temp_image_path)
            except:
                pass  # Ignore errors during cleanup
        if temp_dir and os.path.exists(temp_dir):
            try:
                os.rmdir(temp_dir)
            except:
                pass  # Ignore errors during cleanup

@app.get("/health")
async def health():
    """Health check endpoint"""
    return {"status": "healthy"}

@app.get("/")  # 添加根路径处理
async def root():
    return {"message": "Ollama AI Image Analyzer API is running!"}

@app.get("/docs")  # 添加文档路径
async def docs():
    return {"message": "API documentation is available at /docs when running with uvicorn --docs"}

# Maintain command line compatibility
if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "--server":
        import uvicorn
        port = int(sys.argv[2]) if len(sys.argv) > 2 else 8000
        uvicorn.run(app, host="localhost", port=port)
    else:
        if len(sys.argv) < 3:
            print("Usage: python ollamaAI.py <image_path> <prompt_file>", file=sys.stderr)
            print("Or run as server: python ollamaAI.py --server [port]", file=sys.stderr)
            sys.exit(1)

        result = analyze(sys.argv[1], sys.argv[2])
        print(result)




