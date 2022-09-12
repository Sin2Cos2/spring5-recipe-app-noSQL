package guru.springframework.controllers;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.services.RecipeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Slf4j
@Controller
@AllArgsConstructor
public class RecipeController {

    private final RecipeService service;

//    @InitBinder
//    public void binder(WebDataBinder wdb) {
//        wdb.setDisallowedFields("id");
//    }

    @GetMapping("/recipe/{id}/show")
    public String showById(@PathVariable String id, Model model) {

        model.addAttribute("recipe", service.findById(id).block());

        return "/recipe/show";
    }

    @GetMapping("/recipe/{id}/update")
    public String updateRecipe(@PathVariable String id, Model model) {
        model.addAttribute("recipe", service.findCommandById(id).block());

        return "recipe/recipeForm";
    }

    @GetMapping({"/recipe/new"})
    public String newRecipe(Model model) {
        model.addAttribute("recipe", new RecipeCommand());
        return "recipe/recipeForm";
    }

    @GetMapping("/recipe/{id}/delete")
    public String deleteById(@PathVariable String id) {
        log.debug("Deleting id: " + id);
        service.deleteById(id);

        return "redirect:/";
    }

    @PostMapping("/recipe/new")
    public String saveNewRecipe(@Valid @ModelAttribute("recipe") RecipeCommand recipe, BindingResult result) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(err -> log.debug(err.toString()));

            return "/recipe/recipeForm";
        }
        recipe.setId(null);
        RecipeCommand savedCommand = service.saveRecipeCommand(recipe).block();

        return "redirect:/recipe/" + savedCommand.getId() + "/ingredients";
    }

    @PostMapping("recipe")
    public String saveOrUpdate(@Valid @ModelAttribute("recipe") RecipeCommand command, BindingResult result) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(err -> log.debug(err.toString()));
            RecipeCommand recipe = service.findCommandById(command.getId()).block();
            command.setIngredients(recipe.getIngredients());

            return "/recipe/recipeForm";
        }

        log.debug("In the recipe POST");
        RecipeCommand recipe = service.findCommandById(command.getId()).block();
        command.setIngredients(recipe.getIngredients());
        command.setCategories(recipe.getCategories());
        RecipeCommand savedCommand = service.saveRecipeCommand(command).block();

        return "redirect:/recipe/" + savedCommand.getId() + "/show" ;
    }
}
