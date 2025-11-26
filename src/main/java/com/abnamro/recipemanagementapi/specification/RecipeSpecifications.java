package com.abnamro.recipemanagementapi.specification;

import com.abnamro.recipemanagementapi.model.Recipe;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
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
        List<Specification<Recipe>> parts = new ArrayList<>();

        Specification<Recipe> s1 = vegetarian(vegetarian);
        Specification<Recipe> s2 = servings(servings);
        Specification<Recipe> s3 = preparationTime(preparationTime);
        Specification<Recipe> s4 = textInInstructions(text);
        Specification<Recipe> s5 = includeIngredients(includes);
        Specification<Recipe> s6 = excludeIngredients(excludes);

        if(s1 != null) parts.add(s1);
        if(s2 != null) parts.add(s2);
        if(s3 != null) parts.add(s3);
        if(s4 != null) parts.add(s4);
        if(s5 != null) parts.add(s5);
        if(s6 != null) parts.add(s6);

        if(parts.isEmpty()) return null;

        Specification<Recipe> result = parts.get(0);

        for(int i=1; i<parts.size(); i++) {
            result = result.and(parts.get(i));
        }

        return result;
    }
}
