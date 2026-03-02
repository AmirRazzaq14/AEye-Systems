import requests
import base64
import json
import re
import sys
import os

def analyze(image_path, prompt_file):
    """The simplest food identification, returning structured data"""

    # Check if image file exists
    if not os.path.exists(image_path):
        return f"ERROR: Image file does not exist: {image_path}"

    # Check if prompt file exists
    if not os.path.exists(prompt_file):
        return f"ERROR: Prompt file does not exist: {prompt_file}"

    with open(image_path, "rb") as f:
        image_bytes = f.read()

    if image_bytes is None or len(image_bytes) == 0:
        return "ERROR: No image provided or empty image file"

    image_base64 = base64.b64encode(image_bytes).decode('utf-8')

    # Improved prompt file reading with error handling
    try:
        with open(prompt_file, 'r', encoding='utf-8') as f:
            prompt = f.read()

        if prompt is None or prompt.strip() == "":
            return "ERROR: Prompt file is empty or contains only whitespace"

        print(f"DEBUG: Loaded prompt (length: {len(prompt)}): {prompt[:100]}...", file=sys.stderr)

    except FileNotFoundError:
        return f"ERROR: Prompt file not found: {prompt_file}"
    except UnicodeDecodeError:
        # Try reading with different encoding
        try:
            with open(prompt_file, 'r', encoding='latin-1') as f:
                prompt = f.read()
            if prompt is None or prompt.strip() == "":
                return "ERROR: Prompt file is empty or contains only whitespace"
        except Exception as e:
            return f"ERROR: Could not read prompt file due to encoding issues: {str(e)}"
    except Exception as e:
        return f"ERROR: Failed to read prompt file: {str(e)}"

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
            return f"ERROR: Request failed with status code {response.status_code}: {response.text}"

        result = response.json()

        # Check for errors in response from Ollama
        if 'error' in result:
            return f"ERROR from Ollama: {result['error']}"

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
        return "ERROR: Cannot connect to Ollama server. Make sure it's running on http://localhost:11434"
    except requests.exceptions.Timeout:
        return "ERROR: Request timed out. The operation took longer than expected."
    except Exception as e:
        return f"ERROR: {str(e)}"

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python ollamaAI.py <image_path> <prompt_file>", file=sys.stderr)
        sys.exit(1)

    image_path = sys.argv[1]
    prompt_file = sys.argv[2]

    # Verify arguments
    print(f"Debug: Processing image: {image_path}", file=sys.stderr)
    print(f"Debug: Using prompt file: {prompt_file}", file=sys.stderr)
    print(f"Debug: Prompt file exists: {os.path.exists(prompt_file)}", file=sys.stderr)

    result = analyze(image_path, prompt_file)
    print(result)