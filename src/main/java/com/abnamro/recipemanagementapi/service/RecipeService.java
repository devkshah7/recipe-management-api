package com.abnamro.recipemanagementapi.service;

import com.abnamro.recipemanagementapi.model.Recipe;
import com.abnamro.recipemanagementapi.repository.RecipeRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Recipe save(Recipe recipe) {
        if(recipe.getIngredients() != null) {
            List<String> normalized = recipe.getIngredients().stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(s -> s.trim().toLowerCase())
                    .collect(Collectors.toList());
            recipe.setIngredients(normalized);
        }
        return recipeRepository.save(recipe);
    }

    public Optional<Recipe> findById(Long id) {
        return recipeRepository.findById(id);
    }

    public void deleteById(Long id) {
        recipeRepository.deleteById(id);
    }

    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    public List<Recipe> findAll(Specification<Recipe> specification) {
        return recipeRepository.findAll(specification);
    }
}
