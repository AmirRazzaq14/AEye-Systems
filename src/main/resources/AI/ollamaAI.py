import requests
import base64
import json
import re
import sys
import os
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
import io

def create_error(message):
    """Create standardized error message"""
    return f"ERROR: {message}"

def validate_image_file(image_path):
    """Validate image file exists and is not empty"""
    if not os.path.exists(image_path):
        return create_error(f"Image file does not exist: {image_path}")

    with open(image_path, "rb") as f:
        image_bytes = f.read()

    if image_bytes is None or len(image_bytes) == 0:
        return create_error("No image provided or empty image file")

    return None, image_bytes

def read_prompt_file(prompt_file):
    """Read prompt file with error handling for various encoding issues"""
    # Try reading with UTF-8 encoding first
    try:
        with open(prompt_file, 'r', encoding='utf-8') as f:
            prompt = f.read()

        if prompt is None or prompt.strip() == "":
            return create_error("Prompt file is empty or contains only whitespace"), None

        print(f"DEBUG: Loaded prompt (length: {len(prompt)}): {prompt[:100]}...", file=sys.stderr)
        return None, prompt

    except FileNotFoundError:
        return create_error(f"Prompt file not found: {prompt_file}"), None
    except UnicodeDecodeError:
        # Try reading with different encoding
        try:
            with open(prompt_file, 'r', encoding='latin-1') as f:
                prompt = f.read()
            if prompt is None or prompt.strip() == "":
                return create_error("Prompt file is empty or contains only whitespace"), None
            return None, prompt
        except Exception as e:
            return create_error(f"Could not read prompt file due to encoding issues: {str(e)}"), None
    except Exception as e:
        return create_error(f"Failed to read prompt file: {str(e)}"), None

def handle_api_response_errors(response):
    """Handle API response errors"""
    if response.status_code != 200:
        return create_error(f"Request failed with status code {response.status_code}: {response.text}")
    return None

def handle_ollama_response_errors(result):
    """Handle errors from Ollama response"""
    if 'error' in result:
        return create_error(f"from Ollama: {result['error']}")
    return None

def extract_json_from_response(response_text):
    """Try to extract JSON from response text"""
    # Look for JSON object between curly braces
    match = re.search(r'\{.*\}', response_text, re.DOTALL)
    if match:
        json_str = match.group()
        try:
            # Validate that it's proper JSON
            parsed = json.loads(json_str)
            return json.dumps(parsed)  # Return clean JSON string
        except json.JSONDecodeError:
            # If JSON parsing fails, return the matched string as is
            return json_str
    else:
        # If no JSON object found, return original response
        return response_text

def analyze(image_path, prompt_file):
    """The simplest food identification, returning structured data"""

    # Check if image file exists
    if not os.path.exists(image_path):
        return create_error(f"Image file does not exist: {image_path}")

    # Check if prompt file exists
    if not os.path.exists(prompt_file):
        return create_error(f"Prompt file does not exist: {prompt_file}")

    with open(image_path, "rb") as f:
        image_bytes = f.read()

    if image_bytes is None or len(image_bytes) == 0:
        return create_error("No image provided or empty image file")

    image_base64 = base64.b64encode(image_bytes).decode('utf-8')

    # Improved prompt file reading with error handling
    try:
        with open(prompt_file, 'r', encoding='utf-8') as f:
            prompt = f.read()

        if prompt is None or prompt.strip() == "":
            return create_error("Prompt file is empty or contains only whitespace")

        print(f"DEBUG: Loaded prompt (length: {len(prompt)}): {prompt[:100]}...", file=sys.stderr)

    except FileNotFoundError:
        return create_error(f"Prompt file not found: {prompt_file}")
    except UnicodeDecodeError:
        # Try reading with different encoding
        try:
            with open(prompt_file, 'r', encoding='latin-1') as f:
                prompt = f.read()
            if prompt is None or prompt.strip() == "":
                return create_error("Prompt file is empty or contains only whitespace")
        except Exception as e:
            return create_error(f"Could not read prompt file due to encoding issues: {str(e)}")
    except Exception as e:
        return create_error(f"Failed to read prompt file: {str(e)}")

    # using Ollama
    url = "http://localhost:11434/api/generate"

    payload = {
        "model": "llava",
        "prompt": prompt,
        "images": [image_base64],
        "stream": False
    }

    try:
        response = requests.post(url, json=payload, timeout=60)  # Add timeout

        # Check if request was successful
        if response.status_code != 200:
            return create_error(f"Request failed with status code {response.status_code}: {response.text}")

        result = response.json()

        # Check for errors in response from Ollama
        if 'error' in result:
            return create_error(f"from Ollama: {result['error']}")

        # Extract the response text
        response_text = result.get("response", "").strip()

        # Try to extract JSON from the response
        # Look for JSON object between curly braces
        match = re.search(r'\{.*\}', response_text, re.DOTALL)
        if match:
            json_str = match.group()
            try:
                # Validate that it's proper JSON
                parsed = json.loads(json_str)
                return json.dumps(parsed)  # Return clean JSON string
            except json.JSONDecodeError:
                # If JSON parsing fails, return the matched string as is
                return json_str
        else:
            # If no JSON object found, return original response
            return response_text

    except requests.exceptions.ConnectionError:
        return create_error("Cannot connect to Ollama server. Make sure it's running on http://localhost:11434")
    except requests.exceptions.Timeout:
        return create_error("Request timed out. The operation took longer than expected.")
    except Exception as e:
        return create_error(f"{str(e)}")

class RequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        # Parse the URL and headers
        parsed_path = urlparse(self.path)

        if parsed_path.path == '/analyze':
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)

            try:
                # Parse JSON request
                request_data = json.loads(post_data.decode('utf-8'))

                # Extract image path and prompt file from request
                image_path = request_data.get('image_path')
                prompt_file = request_data.get('prompt_file')

                if not image_path or not prompt_file:
                    self.send_response(400)
                    self.send_header('Content-type', 'application/json')
                    self.end_headers()
                    error_response = json.dumps({
                        "error": "Missing required fields: image_path and prompt_file"
                    })
                    self.wfile.write(error_response.encode('utf-8'))
                    return

                # Call the analyze function
                result = analyze(image_path, prompt_file)

                # Send response
                self.send_response(200)
                self.send_header('Content-type', 'application/json')
                self.end_headers()

                response_data = {
                    "result": result
                }
                self.wfile.write(json.dumps(response_data).encode('utf-8'))

            except json.JSONDecodeError:
                self.send_response(400)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                error_response = json.dumps({
                    "error": "Invalid JSON in request body"
                })
                self.wfile.write(error_response.encode('utf-8'))
            except Exception as e:
                self.send_response(500)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                error_response = json.dumps({
                    "error": f"Internal server error: {str(e)}"
                })
                self.wfile.write(error_response.encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()

    def do_GET(self):
        # Simple endpoint to check if the server is running
        parsed_path = urlparse(self.path)

        if parsed_path.path == '/health':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            response = json.dumps({"status": "healthy"})
            self.wfile.write(response.encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()

def start_server(port=8080):
    """Start the HTTP server"""
    server_address = ('', port)
    httpd = HTTPServer(server_address, RequestHandler)
    print(f"Starting server on port {port}...")
    httpd.serve_forever()

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "--server":
        # Run as HTTP server
        port = int(sys.argv[2]) if len(sys.argv) > 2 else 8080
        start_server(port)
    else:
        # Original command-line functionality
        if len(sys.argv) < 3:
            print("Usage: python ollamaAI.py <image_path> <prompt_file>", file=sys.stderr)
            print("Or run as server: python ollamaAI.py --server [port]", file=sys.stderr)
            sys.exit(1)

        image_path = sys.argv[1]
        prompt_file = sys.argv[2]

        # Verify arguments
        print(f"Debug: Processing image: {image_path}", file=sys.stderr)
        print(f"Debug: Using prompt file: {prompt_file}", file=sys.stderr)
        print(f"Debug: Prompt file exists: {os.path.exists(prompt_file)}", file=sys.stderr)

        result = analyze(image_path, prompt_file)
        print(result)
