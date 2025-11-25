package com.abnamro.recipemanagementapi.service;

import com.abnamro.recipemanagementapi.exception.ResourceNotFoundException;
import com.abnamro.recipemanagementapi.model.Recipe;
import com.abnamro.recipemanagementapi.repository.RecipeRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Recipe save(Recipe recipe) {
        normalizeIngredients(recipe);
        return recipeRepository.save(recipe);
    }

    public Recipe update(Long id, Recipe updatedRecipe) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id " + id));

        //update fields (selective)
        existingRecipe.setName(updatedRecipe.getName());
        existingRecipe.setVegetarian(updatedRecipe.isVegetarian());
        existingRecipe.setServings(updatedRecipe.getServings());
        existingRecipe.setInstructions(updatedRecipe.getInstructions());
        existingRecipe.setPreparationTime(updatedRecipe.getPreparationTime());

        //normalize and set ingredients
        existingRecipe.setIngredients(normalizeList(updatedRecipe.getIngredients()));
        return recipeRepository.save(existingRecipe);
    }

    public Recipe partialUpdate(Long id, Map<String, Object> updates, ObjectMapper objectMapper) {
        Recipe existing = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id " + id));

        //Prevent id being updated
        updates.remove("id");

        //Convert map -> JsonNode and merge into existing object using Jackson
        JsonNode updatesNode = objectMapper.valueToTree(updates);

        try {
            //readerForUpdating will map only provided fields onto existing object
            objectMapper.readerForUpdating(existing).readValue(updatesNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply patch", e);
        }

        //Normalize ingredients (if they were provided or already present)
        if(existing.getIngredients() != null) {
            existing.setIngredients(normalizeList(existing.getIngredients()));
        }

        return recipeRepository.save(existing);
    }

    private void normalizeIngredients(Recipe recipe) {
        if(recipe.getIngredients() != null) {
            recipe.setIngredients(normalizeList(recipe.getIngredients()));
        }
    }

    private List<String> normalizeList(List<String> list) {
        if(list == null) return null;
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toList());
    }

    public Optional<Recipe> findById(Long id) {
        return recipeRepository.findById(id);
    }

    public void deleteById(Long id) {
        if(!recipeRepository.existsById(id)) throw new ResourceNotFoundException("Recipe not found with id " + id);
        recipeRepository.deleteById(id);
    }

    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    public List<Recipe> findAll(Specification<Recipe> specification) {
        return recipeRepository.findAll(specification);
    }

}
