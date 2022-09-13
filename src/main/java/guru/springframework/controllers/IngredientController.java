package guru.springframework.controllers;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.commands.UnitOfMeasureCommand;
import guru.springframework.services.IngredientService;
import guru.springframework.services.RecipeService;
import guru.springframework.services.UnitOfMeasureService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@Controller
@AllArgsConstructor
public class IngredientController {

    public static final String INGREDIENTFORM = "recipe/ingredient/ingredientForm";
    private final RecipeService recipeService;
    private final IngredientService ingredientService;
    private final UnitOfMeasureService uomService;

    @GetMapping("/recipe/{id}/ingredients")
    public String getIngredients(@PathVariable String id, Model model) {
        model.addAttribute("recipe", recipeService.findById(id));

        return "/recipe/ingredient/list";
    }

    @GetMapping("/recipe/{recipeId}/ingredient/{id}/show")
    public String showIngredient(@PathVariable String recipeId, @PathVariable String id, Model model) {
        model.addAttribute("ingredient", ingredientService.findByRecipeIdAndIngredientId(recipeId, id));

        return "/recipe/ingredient/show";
    }

    @GetMapping("/recipe/{recipeId}/ingredient/{id}/update")
    public String updateIngredient(@PathVariable String recipeId, @PathVariable String id, Model model) {
        log.debug("In the update form");
        model.addAttribute("ingredient", ingredientService.findByRecipeIdAndIngredientId(recipeId, id));

        model.addAttribute("uomList", uomService.findAll());

        return "/recipe/ingredient/ingredientForm";
    }

    @GetMapping("/recipe/{recipeId}/ingredient/new")
    public String newIngredient(@PathVariable String recipeId, Model model) {
        Mono<IngredientCommand> ingredient = recipeService.findCommandById(recipeId)
                .map(recipeCommand -> {
                    IngredientCommand ingredientCommand = new IngredientCommand();
                    ingredientCommand.setRecipeId(recipeCommand.getId());
                    ingredientCommand.setUom(new UnitOfMeasureCommand());
                    return ingredientCommand;
                });

        model.addAttribute("ingredient", ingredient);
        model.addAttribute("uomList", uomService.findAll());
        return INGREDIENTFORM;
    }

    @GetMapping("/recipe/{recipeId}/ingredient/{id}/delete")
    public Mono<String> deleteIngredient(@PathVariable String recipeId, @PathVariable String id) {

        return ingredientService.deleteById(recipeId, id)
                .thenReturn("redirect:/recipe/" + recipeId + "/ingredients");
    }

    @PostMapping("/recipe/{recipeId}/ingredient")
    public Mono<String> saveOrUpdateIngredient(@Valid @ModelAttribute("ingredient") Mono<IngredientCommand> command,
                                               @PathVariable String recipeId, Model model) {
        return command.doOnNext(cmd -> {
                    cmd.setRecipeId(recipeId);
                    if (cmd.getId().isEmpty())
                        cmd.setId(null);
                })
                .flatMap(ingredientService::saveIngredientCommand)
                .doOnNext(sc -> log.debug("saved recipe id:{} and ingredient id:{}", sc.getRecipeId(),
                        sc.getId()))
                .map(sc -> "redirect:/recipe/" + sc.getRecipeId() + "/ingredient/" + sc.getId() + "/show")
                .onErrorResume(WebExchangeBindException.class, thr -> {
                    model.addAttribute("uomList", uomService.findAll());
                    return Mono.just(INGREDIENTFORM);
                })
                .doOnError(thr -> log.error("Error saving ingredient for recipe {}", recipeId));
    }

    @ModelAttribute("uomList")
    public Flux<UnitOfMeasureCommand> getUomList() {
        return uomService.findAll();
    }

}
