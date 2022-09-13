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
        return recipeRepository.findById(l);
    }

    @Override
    public Mono<RecipeCommand> findCommandById(String l) {
        return recipeRepository
                .findById(l)
                .map(recipeToRecipeCommand::convert);
    }

    @Override
    public Mono<RecipeCommand> saveRecipeCommand(RecipeCommand recipe) {
        log.debug(recipe.getDescription() + " was saved");
        if (recipe.getId() != null)
            recipe.setIngredients(findCommandById(recipe.getId()).toProcessor().block().getIngredients());
        return recipeRepository
                .save(recipeCommandToRecipe.convert(recipe))
                .map(recipeToRecipeCommand::convert)
                .doOnNext(savedRecipe -> log.debug("Saved recipe {}", savedRecipe.getId()));
    }

    @Override
    public Mono<Recipe> saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    @Override
    public Mono<Void> deleteById(String l) {
        return recipeRepository.deleteById(l)
                .doOnSuccess(tmp -> log.info("Deleted recipe {}", l));
    }
}
