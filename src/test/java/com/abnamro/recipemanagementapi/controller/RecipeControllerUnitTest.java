package com.abnamro.recipemanagementapi.controller;

import com.abnamro.recipemanagementapi.exception.GlobalExceptionHandler;
import com.abnamro.recipemanagementapi.model.Recipe;
import com.abnamro.recipemanagementapi.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RecipeControllerUnitTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private RecipeController recipeController;

    @BeforeEach
    void setup(){
        //include GlobalExceptionHandler to allow controller advice handling (404 -> JSON)
        mockMvc = MockMvcBuilders.standaloneSetup(recipeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void post_returnsCreated() throws Exception {
        Recipe in = new Recipe(null, "Aloo", true, 2, List.of("potato"), "cook", 15);
        Recipe saved = new Recipe(1L, "Aloo", true, 2, List.of("potato"), "cook", 15);

        when(recipeService.save(any(Recipe.class))).thenReturn(saved);

        mockMvc.perform(post("/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/recipes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Aloo"));

        verify(recipeService, times(1)).save(any(Recipe.class));
    }

    @Test
    void getById_found()  throws Exception {
        Recipe r = new Recipe(1L, "Aloo", true, 2, List.of("potato"), "cook", 15);
        when(recipeService.findById(1L)).thenReturn(Optional.of(r));

        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aloo"));
    }

    @Test
    void getById_notFound()  throws Exception {
        when(recipeService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/recipes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Recipe not found with id: " + 99));
    }

    @Test
    void put_updatesAndReturns()  throws Exception {
        Recipe updated = new Recipe(1L, "Aloo Updated", true, 3, List.of("potato"), "cook", 20);
        when(recipeService.update(eq(1L), any(Recipe.class))).thenReturn(updated);

        mockMvc.perform(put("/recipes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aloo Updated"));
    }

    @Test
    void patch_partialUpdate() throws Exception{
        Recipe after = new Recipe(1L, "Aloo", true, 2, List.of("potato", "salt"), "cook", 15);
        when(recipeService.partialUpdate(eq(1L), any(Map.class), any())).thenReturn(after);

        mockMvc.perform(patch("/recipes/1")
        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Aloo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Aloo"));
    }

    @Test
    void delete_returnsNoContent()  throws Exception {
        doNothing().when(recipeService).deleteById(1L);
        mockMvc.perform(delete("/recipes/1"))
                .andExpect(status().isNoContent());

        verify(recipeService, times(1)).deleteById(1L);
    }

    @Test
    void search_returnsList() throws Exception{
        Recipe r = new Recipe(1L, "Aloo", true, 2, List.of("potato"), "cook", 15);
        when(recipeService.findAll()).thenReturn(List.of(r));

        mockMvc.perform(get("/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aloo"));
    }
}
