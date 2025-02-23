import openai
import json
import re
import sys

OPENAI_API_KEY = "sk-882TXyUTZEIIQpiNLjjFczS4cHeHVDyS3RxnMrcyqGT3BlbkFJP50yt0DukdrwbloOWNlly-H0nPDyjMAn_S7K36NKYA"
client = openai.OpenAI(api_key=OPENAI_API_KEY)

# Read user ingredients passed from Java
input_data = sys.stdin.read()  # Read JSON input from stdin
user_ingredients = json.loads(input_data)  # Convert JSON string to list

recipe_form = {
    "name": "Recipe Name",
    "description": "A brief description of the dish.",
    "nutrition": {
        "calories": "Calories info",
        "protein": "Protein g",
        "carbs": "Carbohydrates g",
        "fat": "Fat g",
        "fiber": "Fiber g",
        "sugar": "Sugar g",
        "sodium": "Sodium mg"
    },
    "ingredients": [
        "Ingredient 1",
        "Ingredient 2"
    ],
    "steps": [
        "Step 1",
        "Step 2"
    ],
    "reference": "Recipe reference website url"
}

# user_ingredients = [
#     "chicken", "tomato", "tofu", "onion", "garlic", "beef", "potato", "carrot",
#     "sugar", "red pepper powder", "salt", "sesame oil", "cooking oil"
# ]

prompt = f"""
You have access to a list of ingredients currently available in a user's refrigerator.

**Available ingredients:** {", ".join(user_ingredients)}

Based on these ingredients, suggest **two recipes** that can be made.

### **Recipe Selection Criteria**
1. Maximize the use of available ingredients.
2. If any ingredient is missing, suggest a reasonable substitute.
3. Keep the recipes simple and executable with common kitchen tools.

### **Output Format (Valid JSON)**
```json
[
    {json.dumps(recipe_form, indent=4)},
    {json.dumps(recipe_form, indent=4)}
]
"""

def get_recipes(ingredients):
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[{"role": "user", "content": prompt}],
        max_tokens=1000,
    )

    recipe_data = response.choices[0].message.content
    recipe_data = re.sub(r"```json\n(.*?)\n```", r"\1", recipe_data, flags=re.DOTALL).strip()

    try:
        recipes = json.loads(recipe_data)
        return recipes
    except json.JSONDecodeError:
        print("Error: Invalid JSON output from GPT.")
        return None


recipes = get_recipes(user_ingredients)
if recipes:
    with open("/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/recipe.json", "w") as f:
        json.dump(recipes, f, indent=4)

    # Print summary
    for recipe in recipes:
        print(f"\nRecipe: {recipe['name']}")
        print(f"Ingredients: {', '.join(recipe['ingredients'])}")
        print(f"Main Steps: {recipe['steps'][0]} ... {recipe['steps'][-1]}")
