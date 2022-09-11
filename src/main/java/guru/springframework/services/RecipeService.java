package guru.springframework.services;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.domain.Recipe;

import java.util.Set;

public interface RecipeService {

    Set<Recipe> getRecipes();

    Recipe findById(String l);

    RecipeCommand saveRecipeCommand(RecipeCommand recipe);

    Recipe saveRecipe(Recipe recipe);

    RecipeCommand findCommandById(String l);

    void deleteById(String l);
}
