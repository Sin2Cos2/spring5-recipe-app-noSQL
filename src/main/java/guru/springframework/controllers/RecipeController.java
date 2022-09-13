package guru.springframework.controllers;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.services.RecipeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@Controller
@AllArgsConstructor
public class RecipeController {

    public static final String RECIPE_RECIPEFORM = "recipe/recipeForm";
    private final RecipeService service;

//    @InitBinder
//    public void binder(WebDataBinder wdb) {
//        wdb.setDisallowedFields("id");
//    }

    @GetMapping("/recipe/{id}/show")
    public Mono<String> showById(@PathVariable String id, Model model) {
        return service.findById(id)
                .doOnNext(recipe -> model.addAttribute("recipe", recipe))
                .map(rec -> "recipe/show")
                .switchIfEmpty(Mono.error(new RuntimeException("recipe not found!")));
    }

    @GetMapping("/recipe/{id}/update")
    public String updateRecipe(@PathVariable String id, Model model) {
        model.addAttribute("recipe", service.findCommandById(id));

        return RECIPE_RECIPEFORM;
    }

    @GetMapping({"/recipe/new"})
    public String newRecipe(Model model) {
        model.addAttribute("recipe", new RecipeCommand());
        return RECIPE_RECIPEFORM;
    }

    @GetMapping("/recipe/{id}/delete")
    public Mono<String> deleteById(@PathVariable String id) {
        log.debug("Deleting id: " + id);
        return service.deleteById(id).thenReturn("redirect:/");
    }

    @PostMapping("/recipe")
    public Mono<String> saveOrUpdate(@Valid @ModelAttribute("recipe") Mono<RecipeCommand> recipe) {

        return recipe
                .flatMap(cmd -> {
                    if (cmd.getId().isEmpty())
                        cmd.setId(null);

                    return service.saveRecipeCommand(cmd);
                })
                .map(rec -> "redirect:/recipe/" + rec.getId() + "/show")
                .doOnError(thr -> log.error("Error saving recipe"))
                .onErrorResume(WebExchangeBindException.class, thr -> Mono.just(RECIPE_RECIPEFORM));
    }
}
