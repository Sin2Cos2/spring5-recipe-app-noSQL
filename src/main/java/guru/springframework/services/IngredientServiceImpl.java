package guru.springframework.services;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import guru.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final RecipeReactiveRepository repository;
    private final IngredientToIngredientCommand toIngredientCommand;
    private final IngredientCommandToIngredient toIngredient;
    private final UnitOfMeasureReactiveRepository uomRepository;


    @Override
    public Mono<IngredientCommand> findByRecipeIdAndIngredientId(String recipeId, String id) {
        return repository
                .findById(recipeId)
                .flatMapIterable(Recipe::getIngredients)
                .filter(ingredient -> ingredient.getId().equalsIgnoreCase(id))
                .single()
                .map(ingredient -> {
                    IngredientCommand command = toIngredientCommand.convert(ingredient);
                    command.setRecipeId(recipeId);
                    return command;
                });
    }

    @Override
    public Mono<IngredientCommand> saveIngredientCommand(IngredientCommand command) {
        Recipe recipe = repository.findById(command.getRecipeId()).block();

        if (recipe == null)
            throw new RuntimeException("Recipe with " + command.getRecipeId() + " id doesn't exits");

        Optional<Ingredient> ingredientOptional = recipe
                .getIngredients()
                .stream()
                .filter(ingredient -> ingredient.getId().equals(command.getId())).findFirst();

        Ingredient savedIngredient;
        if (ingredientOptional.isPresent()) {
            savedIngredient = ingredientOptional.get();
            savedIngredient.setAmount(command.getAmount());
            savedIngredient.setDescription(command.getDescription());
            savedIngredient.setUnitOfMeasure(uomRepository
                    .findById(command.getUom().getId())
                    .block());

            if (savedIngredient.getUnitOfMeasure() == null)
                throw new RuntimeException("Unit of measure not found!");
        } else {
            savedIngredient = toIngredient.convert(command);
            recipe.addIngredient(savedIngredient);
        }

        Recipe savedRecipe = repository.save(recipe).block();

        if (command.getId() == null) {
            command.setId(recipe.getIngredients().stream()
                    .filter(ingredient -> ingredient.getAmount().equals(command.getAmount()))
                    .filter(ingredient -> ingredient.getDescription().equalsIgnoreCase(command.getDescription()))
                    .filter(ingredient -> ingredient.getUnitOfMeasure().getId().equals(command.getUom().getId()))
                    .findFirst()
                    .get()
                    .getId());
        }

        IngredientCommand converted = toIngredientCommand.convert(savedRecipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getId().equals(command.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient with " + command.getId() + " id doesn't  exist")));
        return Mono.just(converted);
    }

    @Override
    public Mono<Void> deleteByRecipeIdAndIngredientId(String recipeId, String id) {
//        Recipe recipe = repository.findById(recipeId);
//
//        Ingredient ingredient = recipe
//                .getIngredients()
//                .stream()
//                .filter(ingredient1 -> ingredient1.getId().equals(id))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Ingredient with " + id + " id doesn't  exist"));
//
//        recipe.getIngredients().remove(ingredient);
//        repository.save(recipe).block();

        repository.findById(recipeId)
                .map(recipe -> {
                    recipe
                            .getIngredients().stream().filter(ingr -> ingr.getId().equals(id))
                            .map(ingr -> recipe.getIngredients().remove(ingr));
                    repository.save(recipe);
                    return Mono.empty();
                });

        return Mono.empty();
    }
}
