package guru.springframework.services;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.converters.RecipeCommandToRecipe;
import guru.springframework.converters.RecipeToRecipeCommand;
import guru.springframework.domain.Recipe;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeReactiveRepository recipeRepository;
    private final RecipeCommandToRecipe recipeCommandToRecipe;
    private final RecipeToRecipeCommand recipeToRecipeCommand;

    @Override
    public Flux<Recipe> getRecipes() {
        log.debug("I'm in the service");

        return recipeRepository.findAll();
    }

    @Override
    public Mono<Recipe> findById(String l) {
        Mono<Recipe> recipe = recipeRepository.findById(l);
        if (recipe.block() == null) {
            throw new RuntimeException("Recipe was not found!");
        }

        return recipe;
    }

    @Override
    public Mono<RecipeCommand> saveRecipeCommand(RecipeCommand recipe) {
        log.debug(recipe.getDescription() + " was saved");
        return recipeRepository
                .save(recipeCommandToRecipe.convert(recipe))
                .map(recipeToRecipeCommand::convert);
    }

    @Override
    public Mono<Recipe> saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    @Override
    public Mono<RecipeCommand> findCommandById(String l) {
        Mono<Recipe> recipe = recipeRepository.findById(l);
        if (recipe.block() == null)
            throw new RuntimeException("Recipe with " + l + " doesn't exist");

        return recipe.map(recipeToRecipeCommand::convert);
    }

    @Override
    public void deleteById(String l) {
        recipeRepository.deleteById(l).block();
    }
}
