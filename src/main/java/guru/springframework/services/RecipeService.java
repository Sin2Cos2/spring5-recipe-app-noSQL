package guru.springframework.services;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.domain.Recipe;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecipeService {

    Flux<Recipe> getRecipes();

    Mono<Recipe> findById(String l);

    Mono<RecipeCommand> saveRecipeCommand(RecipeCommand recipe);

    Mono<Recipe> saveRecipe(Recipe recipe);

    Mono<RecipeCommand> findCommandById(String l);

    void deleteById(String l);
}
