package guru.springframework.converters;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.domain.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@AllArgsConstructor
@Component
public class IngredientCommandToIngredient implements Converter<IngredientCommand, Ingredient> {

    private final UnitOfMeasureCommandToUnitOfMeasure converter;

    @Synchronized
    @NonNull
    @Override
    public Ingredient convert(IngredientCommand source) {

        if (source == null)
            throw new RuntimeException("Source is null!");

        final Ingredient ingredient = new Ingredient();
        ingredient.setId(source.getId() == null ? UUID.randomUUID().toString() : source.getId());
        ingredient.setRecipeId(source.getRecipeId());
        ingredient.setAmount(source.getAmount());
        ingredient.setDescription(source.getDescription());
        ingredient.setUnitOfMeasure(converter.convert(source.getUom()));

        return ingredient;
    }
}
