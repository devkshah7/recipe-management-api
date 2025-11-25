package com.abnamro.recipemanagementapi.controller;

import com.abnamro.recipemanagementapi.model.Recipe;
import com.abnamro.recipemanagementapi.service.RecipeService;
import com.abnamro.recipemanagementapi.specification.RecipeSpecifications;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<Recipe> create(@Valid @RequestBody Recipe recipe) {
        Recipe saved = recipeService.save(recipe);
        return ResponseEntity.created(URI.create("/recipes/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> get(@PathVariable Long id) {
        return recipeService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recipe> update(@PathVariable Long id, @Valid @RequestBody Recipe recipe) {
        return recipeService.findById(id).map(existing -> {
            recipe.setId(id);
            return ResponseEntity.ok(recipeService.save(recipe));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recipeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> search(
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) Integer servings,
            @RequestParam(required = false) List<String> include,
            @RequestParam(required = false) List<String> exclude,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Integer preparationTime) {
        //normalize ingredient inputs to lower-case and trimmed
        List<String> includesNormalized = (include == null) ? null : include.stream().map(s -> s.trim().toLowerCase()).collect(Collectors.toList());
        List<String> excludesNormalized = (exclude == null) ? null : exclude.stream().map(s -> s.trim().toLowerCase()).collect(Collectors.toList());
        Specification<Recipe> spec = RecipeSpecifications.buildSpecification(vegetarian, servings, includesNormalized, excludesNormalized, text, preparationTime);
        List<Recipe> list;
        if(spec == null){
            list = recipeService.findAll();
        } else {
            list = recipeService.findAll(spec);
        }
        return ResponseEntity.ok(list);
    }
}