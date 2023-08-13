package modulos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import sisgestaocsatleta.SistemaEstoqueMae;

public class Aquidauana extends SistemaEstoqueMae {

    private final Path dataFilePath = Paths.get("data", "aquidauana.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void saveProducts() {
        try {
            String json = gson.toJson(products);
            Files.write(dataFilePath, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadProducts() {
        List<Product> loadedProducts = new ArrayList<>();

        if (Files.exists(dataFilePath)) {
            try {
                String json = new String(Files.readAllBytes(dataFilePath));
                loadedProducts = gson.fromJson(json, new TypeToken<List<Product>>(){}.getType());

                // Atualize a UI com os produtos carregados
                products.clear();
                products.addAll(loadedProducts);

                productGrid.getChildren().clear();
                for (Product product : products) {
                    ProductCard productCard = new ProductCard(product);
                    productGrid.getChildren().add(productCard);
                    GridPane.setConstraints(productCard, productGrid.getChildren().size() % 3, productGrid.getChildren().size() / 3);

                    String imageURL = product.getImageURL();
                    if (imageURL != null && !imageURL.isEmpty()) {
                        try {
                            ImageView imageView = new ImageView(new Image(imageURL));
                            imageView.setFitWidth(100);
                            imageView.setFitHeight(100);
                            productCard.getChildren().add(0, imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Lidar com a exceção, por exemplo, exibindo uma imagem padrão
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error loading products.");
            }
        }
    }

    // Restante das funções da classe
    // ...
}
