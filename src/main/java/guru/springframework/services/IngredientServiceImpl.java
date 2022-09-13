package guru.springframework.services;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.domain.UnitOfMeasure;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import guru.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeReactiveRepository recipeReactiveRepository;
    private final UnitOfMeasureReactiveRepository unitOfMeasureRepository;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                 IngredientCommandToIngredient ingredientCommandToIngredient,
                                 RecipeReactiveRepository recipeReactiveRepository, UnitOfMeasureReactiveRepository unitOfMeasureRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Override
    public Mono<IngredientCommand> findByRecipeIdAndIngredientId(String recipeId, String ingredientId) {

        return recipeReactiveRepository.findById(recipeId)
                .flatMapIterable(Recipe::getIngredients)
                .filter(ingredient -> ingredient.getId().equals(ingredientId))
                .single()
                .map(ingredient -> {
                    IngredientCommand command = ingredientToIngredientCommand.convert(ingredient);
                    command.setRecipeId(recipeId);

                    return command;
                });
    }

    @Override
    @Transactional
    public Mono<IngredientCommand> saveIngredientCommand(IngredientCommand command) {
        Recipe recipe = recipeReactiveRepository.findById(command.getRecipeId())
                .switchIfEmpty(Mono.error(new RuntimeException("Recipe not found: " + command.getRecipeId())))
                .doOnError(thr -> log.error("error saving ingredient {}", command, thr))
                .toProcessor().block();

        unitOfMeasureRepository.findById(command.getUom().getId())
                .doOnNext(uom -> command.getUom().setDescription(uom.getUnitOfMeasure())).toProcessor().block();

        addOrUpdateIngredient(recipe, command);

        Recipe savedRecipe = recipeReactiveRepository.save(recipe).toProcessor().block();

        Ingredient savedIngredient = findIngredient(recipe, command.getId())
                .orElseGet(() -> findIngredientByValue(recipe, command));
        IngredientCommand savedCommand = ingredientToIngredientCommand.convert(savedIngredient);
        savedCommand.setRecipeId(recipe.getId());
        return Mono.just(savedCommand);
    }

    private void addOrUpdateIngredient(Recipe recipe, IngredientCommand command) {
        Optional<Ingredient> ingredientOpt = findIngredient(recipe, command.getId());
        if (ingredientOpt.isPresent()) {
            updateIngredient(ingredientOpt.get(), command);
        } else {
            recipe.addIngredient(ingredientCommandToIngredient.convert(command));
        }
    }

    private void updateIngredient(Ingredient ingredient, IngredientCommand command) {
        ingredient.setDescription(command.getDescription());
        ingredient.setAmount(command.getAmount());
        UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(command.getUom().getId()).toProcessor().block();
        ingredient.setUnitOfMeasure(unitOfMeasure);
    }

    private Optional<Ingredient> findIngredient(Recipe recipe, String id) {
        return recipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getId().equals(id))
                .findAny();
    }

    private Ingredient findIngredientByValue(Recipe recipe, IngredientCommand command) {
        return recipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getDescription().equals(command.getDescription()))
                .filter(ingredient -> ingredient.getAmount().equals(command.getAmount()))
                .filter(ingredient -> ingredient.getUnitOfMeasure().getId().equals(command.getUom().getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Mono<Void> deleteById(String recipeId, String id) {

        log.info("deleting ingredient {} from recipe {}", id, recipeId);
        return recipeReactiveRepository.findById(recipeId)
                .map(recipe -> {
                    recipe.getIngredients().removeIf(ingredient -> ingredient.getId().equals(id));
                    return recipe;
                })
                .flatMap(recipeReactiveRepository::save)
                .doOnError(error -> log.error("error deleting ingredient {} from recipe {}", id, recipeId, error))
                .then();
    }
}