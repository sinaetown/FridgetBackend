package com.fridge.fridgeproject.ingredient;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserIngredientRepository extends MongoRepository<UserIngredient, String>  {

}
