import json
import numpy as np
import spacy
from sklearn.neighbors import NearestNeighbors
from sklearn.metrics.pairwise import cosine_similarity

nlp = spacy.load("en_core_web_md")

user_data = {
    "vegan": "lacto",
    "allergy": ["a", "b"],
    "meatConsumption": 1,
    "fishConsumption": 2,
    "vegeConsumption": 3,
    "spiciness": 4
}

vegan_dict = {
    "strict": {"meat": -1, "fish": -1, "egg": -1, "dairy": -1, "vegetable": 1},
    "lacto": {"meat": -1, "fish": -1, "egg": -1, "dairy": 1, "vegetable": 1},
    "ovo": {"meat": -1, "fish": -1, "egg": 1, "dairy": -1, "vegetable": 1},
    "pescatarian": {"meat": -1, "fish": 1, "egg": 1, "dairy": 1, "vegetable": 1},
    "flexitarian": {"meat": 0.5, "fish": 0.5, "egg": 1, "dairy": 1, "vegetable": 1},
    "none": {"meat": 1, "fish": 1, "egg": 1, "dairy": 1, "vegetable": 1}
}

vegan_score = vegan_dict[user_data["vegan"]]

json_path = "/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/recipes.json"
with open(json_path, "r", encoding="utf-8") as file:
    recipe_data = json.load(file)

def get_spacy_similarity(word1, word2):
    vec1 = nlp(word1).vector
    vec2 = nlp(word2).vector
    similarity = cosine_similarity([vec1], [vec2])[0][0]
    return similarity

def create_recipe_feature_vector(recipe):
    ingredients = [item["name"] for item in recipe["ingredients"]]

    meat_score = sum(get_spacy_similarity("meat", ing) * vegan_score["meat"] for ing in ingredients if get_spacy_similarity("meat", ing) > 0.3)
    fish_score = sum(get_spacy_similarity("fish", ing) * vegan_score["fish"] for ing in ingredients if get_spacy_similarity("fish", ing) > 0.3)
    egg_score = sum(get_spacy_similarity("egg", ing) * vegan_score["egg"] for ing in ingredients if get_spacy_similarity("egg", ing) > 0.3)
    dairy_score = sum(get_spacy_similarity("dairy", ing) * vegan_score["dairy"] for ing in ingredients if get_spacy_similarity("dairy", ing) > 0.3)
    vege_score = sum(get_spacy_similarity("vegetable", ing) * vegan_score["vegetable"] for ing in ingredients if get_spacy_similarity("vegetable", ing) > 0.3)

    penalty = min(meat_score + fish_score + egg_score + dairy_score, -5)
    reward = vege_score + 1 if vege_score > 2 else vege_score

    for allergic_food in user_data["allergy"]:
        for ingredient in ingredients:
            similarity = get_spacy_similarity(allergic_food, ingredient)
            if similarity > 0.15:
                penalty += -max(5, 10 * similarity)

    reward += 0.5 if abs(recipe["spice_level"] - user_data["spiciness"]) <= 1 else 0

    return [meat_score, fish_score, egg_score, dairy_score, vege_score, penalty, reward]

recipe_features = np.array([create_recipe_feature_vector(recipe) for recipe in recipe_data])

user_vector = np.array([
    user_data["meatConsumption"],
    user_data["fishConsumption"],
    0, 0,
    user_data["vegeConsumption"],
    0, 0
]).reshape(1, -1)

nn_model = NearestNeighbors(n_neighbors=len(recipe_features), metric='euclidean')
nn_model.fit(recipe_features)

distances, indices = nn_model.kneighbors(user_vector)

max_distance = max(distances.flatten())
scores = [(10 - (dist / max_distance) * 10) for dist in distances.flatten()]

sorted_recipes = sorted(
    [{"recipe": recipe_data[recipe_idx], "score": scores[i]} for i, recipe_idx in enumerate(indices.flatten())],
    key=lambda x: x["score"],
    reverse=True
)

result_data = []
for item in sorted_recipes:
    recipe = item["recipe"]
    formatted_recipe = {
        "name": recipe["name"],
        "description": recipe.get("description", "A brief description of the dish."),
        "nutrition": recipe.get("nutrition", {}),
        "ingredients": [{"name": ing["name"], "quantity": ing.get("quantity", "unknown")} for ing in recipe["ingredients"]],
        "steps": recipe.get("steps", []),
        "reference": recipe.get("reference", "No reference available")
    }
    result_data.append(formatted_recipe)

output_path = "/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/results.json"
with open(output_path, "w", encoding="utf-8") as outfile:
    json.dump(result_data, outfile, indent=4, ensure_ascii=False)