package com.abnamro.recipemanagementapi.specification;

import com.abnamro.recipemanagementapi.model.Recipe;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Locale;

public class RecipeSpecifications {

    private RecipeSpecifications() {}

    public static Specification<Recipe> vegetarian(Boolean veg) {
        if(veg == null) return null;
        return (root, query, cb) -> cb.equal(root.get("vegetarian"), veg);
    }

    public static Specification<Recipe> servings(Integer servings) {
        if(servings == null) return null;
        return (root, query, cb) -> cb.equal(root.get("servings"), servings);
    }

    public static Specification<Recipe> preparationTime(Integer minutes) {
        if(minutes == null) return null;
        return (root, query, cb) -> cb.equal(root.get("preparationTime"), minutes);
    }

    //include: recipe MUST have all of these ingredients (AND)
    public static Specification<Recipe> includeIngredients(List<String> includes) {
        if(includes == null || includes.isEmpty()) return null;
        //build specification requiring each ingredient is a member of the collection
        Specification<Recipe> spec = null;
        for(String ing : includes) {
            String trimmed = ing.trim().toLowerCase(Locale.ROOT);
            Specification<Recipe> s = (root, query, cb) -> cb.isMember(trimmed, root.get("ingredients"));
            spec = (spec == null) ? s : spec.and(s);
        }
        return spec;
    }

    //exclude: none of these ingredients must be present
    public static Specification<Recipe> excludeIngredients(List<String> excludes) {
        if(excludes == null || excludes.isEmpty()) return null;
        Specification<Recipe> spec = null;
        for(String ing : excludes) {
            String trimmed = ing.trim().toLowerCase(Locale.ROOT);
            //not member -> allow recipes that don't contain ingredient
            Specification<Recipe> s = (root, query, cb) -> cb.not(cb.isMember(trimmed, root.get("ingredients")));
            spec = (spec == null) ? s : spec.and(s);
        }
        return spec;
    }

    //text search on instructions (case-insensitive, contains)
    public static Specification<Recipe> textInInstructions(String text) {
        if(text == null || text.trim().isEmpty()) return null;
        String t = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, quert, cb) -> cb.like(cb.lower(root.get("instructions")), t);
    }

    //helper to build composite spec
    public static Specification<Recipe> buildSpecification(Boolean vegetarian,
                                                           Integer servings,
                                                           List<String> includes,
                                                           List<String> excludes,
                                                           String text,
                                                           Integer preparationTime) {
        Specification<Recipe> spec = Specification.where(vegetarian(vegetarian))
                .and(servings(servings))
                .and(includeIngredients(includes))
                .and(excludeIngredients(excludes))
                .and(textInInstructions(text))
                .and(preparationTime(preparationTime));
        return spec;
    }
}
