import requests
import base64
import json
import re

def analyze_food(image_path):
    """The simplest food identification, returning structured data"""

    with open(image_path, "rb") as f:
        image_bytes = f.read()
    image_base64 = base64.b64encode(image_bytes).decode('utf-8')

    # using Ollama
    url = "http://localhost:11434/api/generate"
    prompt = """
        You are a professional nutritionist.
        Analyze this food image and return the results strictly according to the following format:

        Food Name: [Text, such as "Apple", "Tomato", "Chicken Breast"]
        Food Type: [Text, such as "Fruit", "Vegetable", "Meat"]
        Estimated Serving Size (grams): [number, such as 150]
        Calories: [number, if it's a range, take the maximum value, such as 90-150, select 150]
        Protein (grams): [number, such as 15]
        Carbohydrates (grams): [number, such as 15]
        Fat (grams): [number, such as 15]

        Do not return any other information！！！
    """

    payload={
        "model": "llava",
        "prompt": prompt,
        "images": [image_base64],
        "stream": False
    }
    response = requests.post(url,json = payload)
    result = response.json()

    # Parse text responses and create structured data objects
    response_text = result.get("response", "").strip()
    structured_data = parse_food_response(response_text)

    return structured_data

def parse_food_response(response_text):
    """Parse the text returned by the API and convert it into structured data"""

    patterns = {
        'foodName': r'Food Name:\s*(.+?)(?:\n|\\n|$)',
        'foodType': r'Food Type:\s*(.+?)(?:\n|\\n|$)',
        'servingSize': r'Estimated Serving Size \(grams\):\s*(\d+)',
        'calories': r'Calories:\s*(\d+)',
        'protein_grams': r'Protein \(grams\):\s*(\d+)',
        'carbs_grams': r'Carbohydrates \(grams\):\s*(\d+)',
        'fat_grams': r'Fat \(grams\):\s*(\d+)'
    }

    result = {}

    # Extract each field using the corresponding pattern
    for key, pattern in patterns.items():
        match = re.search(pattern, response_text)
        if match:
            value = match.group(1).strip()
            if key in ['servingSize', 'calories', 'protein_grams', 'carbs_grams', 'fat_grams']:
                result[key] = value
            else:
                result[key] = 0

    # To match the Java class property name, some fields are remapped
    final_result = {
        "foodName": result.get('foodName', 'Unknown'),
        "foodType": result.get('foodType', 'Unknown'),
        "servingSize": result.get('servingSize', 0),
        "calories": result.get('calories', 0),
        "protein_grams": result.get('protein_grams', 0),
        "carbs_grams": result.get('carbs_grams', 0),
        "fat_grams": result.get('fat_grams', 0)
    }

    return final_result

if __name__ == "__main__":
    import sys

    image_path = sys.argv[1]
    result = analyze_food(image_path)

    # Output JSON, and Java will read this
    print(json.dumps(result))