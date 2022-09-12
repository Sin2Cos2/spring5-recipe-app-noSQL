package guru.springframework.services;

import guru.springframework.domain.Recipe;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@AllArgsConstructor
@Service
public class ImageServiceImpl implements ImageService {

    private final RecipeService recipeService;


    //TODO: Если изменить что то в рецепте, изображение удаляется из БД
    @Override
    public void saveImageFile(String id, MultipartFile image) {
        Recipe recipe = recipeService.findById(id).block();

        try {
            Byte[] arr = new Byte[image.getBytes().length];

            int i = 0;
            for (byte aByte : image.getBytes()) {
                arr[i++] = aByte;
            }

            assert recipe != null;
            recipe.setImage(arr);
            recipeService.saveRecipe(recipe).block();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
