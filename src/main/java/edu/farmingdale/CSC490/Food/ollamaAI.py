import requests
import base64
import json
import re
import sys

def analyze_food(image_path):
    """The simplest food identification, returning structured data"""

    with open(image_path, "rb") as f:
        image_bytes = f.read()
    image_base64 = base64.b64encode(image_bytes).decode('utf-8')

    # using Ollama
    url = "http://localhost:11434/api/generate"
    prompt = """
        You are a professional nutritionist.
        Analyze this food image and return the results strictly as JSON format:

        {
            "foodName": "text, such as Apple, Tomato, Chicken Breast",
            "mealType": "text, such as Fruit, Vegetable, Meat",
            "calories": number, if it's a range, take the maximum value, such as 90-150, select 150,
            "protein_grams": number in grams, such as 15,
            "carbs_grams": number in grams, such as 15,
            "fat_grams": number in grams, such as 15
        }

        Return ONLY the JSON object, nothing else. Do not include markdown formatting, backticks, or any other text.
    """

    payload = {
        "model": "llava",
        "prompt": prompt,
        "images": [image_base64],
        "stream": False
    }
    response = requests.post(url, json=payload)
    result = response.json()

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

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python ollamaAI.py <image_path>", file=sys.stderr)
        sys.exit(1)

    image_path = sys.argv[1]
    result = analyze_food(image_path)
    print(result)