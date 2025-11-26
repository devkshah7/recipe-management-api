package com.abnamro.recipemanagementapi.integration;

import com.abnamro.recipemanagementapi.model.Recipe;
import com.abnamro.recipemanagementapi.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RecipeIntegrationTest {
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        recipeRepository.deleteAll();
    }

    @Test
    void post_then_get_by_id() throws Exception {
        Recipe r = new Recipe(null, "IntDish", true, 2, List.of("potato"), "cook", 10);

        //create
        String body = objectMapper.writeValueAsString(r);
        String location = mockMvc.perform(post("/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        //get by id
        String[] parts = location.split("/");
        String id = parts[parts.length-1];

        mockMvc.perform(get("/recipes/"+id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IntDish"));
    }

    @Test
    void put_and_verify() throws Exception {
        Recipe saved = recipeRepository.save(new Recipe(null, "Old", true, 1, List.of("x"), "old", 5));
        saved.setName("NewName");

        mockMvc.perform(put("/recipes/"+saved.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    void patch_and_verify_normalization() throws Exception {
        Recipe saved = recipeRepository.save(new Recipe(null, "Patch", true, 1, List.of("X"), "old", 5));
        //patch ingredients
        mockMvc.perform(patch("/recipes/"+saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ingredients\":[\" Potato \", \"Salt\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredients", hasSize(2)));

        //verify normalization in DB
        mockMvc.perform(get("/recipes/"+saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredients[0]").value("potato"))
                .andExpect(jsonPath("$.ingredients[1]").value("salt"));
    }

    @Test
    void delete_and_verify() throws Exception {
        Recipe saved = recipeRepository.save(new Recipe(null, "ToDelete", true, 1, List.of("x"), "do", 3));

        mockMvc.perform(delete("/recipes/"+saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/recipes/"+saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtering_combination() throws Exception {
        recipeRepository.save(new Recipe(null, "VegPotato", true, 2, List.of("potato", "salt"), "bake in oven", 30));
        recipeRepository.save(new Recipe(null, "NonVeg", false, 4, List.of("chicken", "salt"), "grill", 40));
        recipeRepository.save(new Recipe(null, "VegCarrot", true, 2, List.of("carrot"), "boil", 10));

        //vegetarian AND include=potato
        mockMvc.perform(get("/recipes")
                .param("vegetarian", "true")
                .param("include", "potato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("VegPotato"));

        //preparationTime filter
        mockMvc.perform(get("/recipes").param("preparationTime", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("VegCarrot"));

        //text search "oven"
        mockMvc.perform(get("/recipes").param("text", "oven"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("VegPotato"));

    }

}
