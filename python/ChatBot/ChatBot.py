import ollama
import os

script_dir = os.path.dirname(__file__)
image_path = os.path.join(script_dir, 'images', 'muscle.webp')

response = ollama.chat(
    model='llama3.2-vision',
    messages=[
        {
            'role': 'user',
            'content': 'Describe this image',
            'images': [image_path]
        }
    ],
)

print(response.message.content)