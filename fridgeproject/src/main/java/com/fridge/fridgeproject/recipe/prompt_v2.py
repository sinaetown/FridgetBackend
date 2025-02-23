import openai
import json
import re

OPENAI_API_KEY = "sk-882TXyUTZEIIQpiNLjjFczS4cHeHVDyS3RxnMrcyqGT3BlbkFJP50yt0DukdrwbloOWNlly-H0nPDyjMAn_S7K36NKYA"
client = openai.OpenAI(api_key=OPENAI_API_KEY)

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
        { "name": "pork", "quantity": "30g" },
        { "name": "lettuce", "quantity": "100g" }
    ], 
    "steps": [
        "Step 1",
        "Step 2"
    ],
    "spice_level": "an integer",
    "reference": "Recipe reference website url",
}

user_ingredients = [
    "chicken", "tomato", "tofu", "onion", "garlic", "beef", "potato", "carrot",
    "sugar", "red pepper powder", "salt", "sesame oil", "cooking oil"
]

prompt = f"""
You have access to a list of ingredients currently available in a user's refrigerator.

**Available ingredients:** {", ".join(user_ingredients)}

Based on these ingredients, suggest **twelve recipes** that can be made.

### **Recipe Selection Criteria**
1. Prioritize recipes that utilize the most available ingredients.
2. Add numbers to each step of the recipe.
3. Keep the recipes simple but provide **precise quantity measurements** for all ingredients.
4. Ensure that the **quantities listed in the ingredients section match the quantities used in the steps**.
5. Use **common kitchen measurements** such as grams (g), milliliters (ml), teaspoons (tsp), tablespoons (tbsp), and cups.

### **Output Format (Valid JSON)**
[
    {json.dumps(recipe_form, indent=4, ensure_ascii=False)}, ...
]
"""

def get_recipes(ingredients):
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[{"role": "user", "content": prompt}],
    )

    recipe_data = response.choices[0].message.content
    recipe_data = re.sub(r"```json\n(.*?)\n```", r"\1", recipe_data, flags=re.DOTALL).strip()

    try:
        recipes = json.loads(recipe_data)
        return recipes
    except json.JSONDecodeError:
        print("Error: Invalid JSON output from GPT.")
        return None

def main():
    recipes = get_recipes(user_ingredients)
    if recipes:
        with open("/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/recipes.json", "w") as f:
            json.dump(recipes, f, indent=4)

        for recipe in recipes:
            print(f"\nRecipe: {recipe['name']}")
            ingredient_names = ', '.join([ingredient['name'] for ingredient in recipe['ingredients']])
            print(f"Ingredients: {ingredient_names}")
            print(f"Main Steps: {recipe['steps'][0]} ... {recipe['steps'][-1]}")

if __name__ == "__main__":
    main()
