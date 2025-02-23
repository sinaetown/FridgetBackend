package com.fridge.fridgeproject.ingredient;

import com.fridge.fridgeproject.common.EntityNotFoundException;
import com.fridge.fridgeproject.ingredient.dto.IngredientsReqDto;
import com.fridge.fridgeproject.user.User;
import com.fridge.fridgeproject.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Transient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class IngredientService {
    private final UserRepository userRepository;
    private final UserIngredientRepository userIngredientRepository;

    public IngredientService(UserRepository userRepository,
                             UserIngredientRepository userIngredientRepository) {
        this.userRepository = userRepository;
        this.userIngredientRepository = userIngredientRepository;
    }

    public List<UserIngredient> findMyIngredients() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException(
                "There's no such user."));
        return user.getUserIngredients();
    }

    public List<UserIngredient> createIngredients(@RequestBody List<IngredientsReqDto> ingredientsReqDtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException(
                "There's no such user."));
        List<UserIngredient> newIngredients = ingredientsReqDtos.stream()
                .map(UserIngredient::toEntity)
                .map(userIngredientRepository::save)
                .collect(Collectors.toList());
        user.getUserIngredients().addAll(newIngredients);
        userRepository.save(user);
        return user.getUserIngredients();
    }

    public List<UserIngredient> deleteIngredients(List<IngredientsReqDto> ingredientsReqDtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException(
                "There's no such user."));
        List<UserIngredient> userIngredients = user.getUserIngredients();
        List<UserIngredient> ingredientsToRemove = userIngredients.stream()
                .filter(ingredient -> ingredientsReqDtos.stream()
                        .anyMatch(dto -> dto.getName().equals(ingredient.getName())
                                && dto.getCategory().equals(ingredient.getCategory())))
                .collect(Collectors.toList());
        if (ingredientsToRemove.isEmpty()) {
            throw new EntityNotFoundException("No matching ingredients found for deletion!");
        }
        userIngredients.removeAll(ingredientsToRemove);
        userIngredientRepository.deleteAll(ingredientsToRemove);
        userRepository.save(user);
        return ingredientsToRemove;
    }
}
