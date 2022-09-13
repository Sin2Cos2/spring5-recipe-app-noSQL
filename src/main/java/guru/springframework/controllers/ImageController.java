package guru.springframework.controllers;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.services.ImageService;
import guru.springframework.services.RecipeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

//import javax.servlet.http.HttpServletResponse;

@AllArgsConstructor
@Controller
public class ImageController {

    private final RecipeService recipeService;
    private final ImageService imageService;

    @GetMapping("/recipe/{id}/image")
    public String imageForm(@PathVariable String id, Model model) {
        model.addAttribute("recipe", recipeService.findCommandById(id));

        return "/recipe/imageUploadForm";
    }

    @PostMapping("/recipe/{id}/image")
    public String saveImage(@PathVariable String id, @RequestParam("imagefile") MultipartFile image) {
        imageService.saveImageFile(id, image);

        return "redirect:/recipe/" + id + "/show";
    }

    @GetMapping("/recipe/{id}/recipeImage")
    public void renderImageFromDB(@PathVariable String id) {
//        RecipeCommand recipe = recipeService.findCommandById(id).block();
//
//        if(recipe.getImage() == null)
//            return;
//        byte[] img = new byte[recipe.getImage().length];
//
//        int i = 0;
//        for (Byte aByte : recipe.getImage()) {
//            img[i++] = aByte;
//        }
//
//        response.setContentType("image/jpeg");
//        InputStream is = new ByteArrayInputStream(img);
//        try {
//            IOUtils.copy(is, response.getOutputStream());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }
}
