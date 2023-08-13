package sisgestaocsatleta;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static javafx.application.Application.launch;
import javafx.scene.Node;

public abstract class SistemaEstoqueMae extends Application implements SistemaEstoqueMaeInterface{

    public List<Product> products = new ArrayList<>();
    public GridPane productGrid;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Define o plano de fundo com uma cor
        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTBLUE, null, null);
        Background background = new Background(backgroundFill);
        root.setBackground(background);

        // Create menu buttons
        Button createButton = new Button("Criar Novo Produto");
        createButton.setOnAction(e -> showCreateProductDialog());
        Button deleteButton = new Button("Excluir Produto");
        deleteButton.setOnAction(e -> showDeleteProductDialog());
        Button editButton = new Button("Editar Produto");
        editButton.setOnAction(e -> showEditProductDialog());
        Button editImageButton = new Button("Editar Imagem do Produto");
        editImageButton.setOnAction(e -> showEditImageDialog());
        Button backButton = new Button("Voltar");
        backButton.setOnAction(e -> goBack());
        Button searchButton = new Button("Pesquisar");
        TextField searchInput = new TextField();
        searchButton.setOnAction(e -> performSearch(searchInput.getText()));
        HBox menu = new HBox(10, createButton, deleteButton, editButton, editImageButton, backButton, searchInput, searchButton);
        menu.setPadding(new Insets(10));
        root.setTop(menu);

        // Create product grid
        productGrid = new GridPane();
        productGrid.setHgap(20);
        productGrid.setVgap(20);
        productGrid.setPadding(new Insets(20));
        root.setCenter(productGrid);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Grade de Produtos");
        primaryStage.show();

        // Carrega os produtos salvos
        loadProducts();
    }
    
    

   public static void main(String[] args) {
    try {
        launch(args);
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    // Custom product card component
    public static class ProductCard extends VBox {
        private Product product;
        private Label nameLabel;
        private Label quantityLabel;

        public ProductCard(Product product) {
            this.product = product;
            setSpacing(10);
            setPadding(new Insets(10));
            setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-border-width: 1px;");

            nameLabel = new Label(product.getName());
            quantityLabel = new Label("Quantidade: " + product.getQuantity());
            Button increaseButton = new Button("+");
            increaseButton.setOnAction(e -> increaseQuantity());
            Button decreaseButton = new Button("-");
            decreaseButton.setOnAction(e -> decreaseQuantity());

            HBox buttons = new HBox(10, increaseButton, decreaseButton);
            buttons.setAlignment(Pos.CENTER);

            getChildren().addAll(nameLabel, quantityLabel, buttons);
        }

        private void increaseQuantity() {
            product.setQuantity(product.getQuantity() + 1);
            updateQuantityLabel();
        }

        private void decreaseQuantity() {
            if (product.getQuantity() > 0) {
                product.setQuantity(product.getQuantity() - 1);
                updateQuantityLabel();
            }
        }

        private void updateQuantityLabel() {
            quantityLabel.setText("Quantidade: " + product.getQuantity());
        }
    }

    public void showCreateProductDialog() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Criar Novo Produto");

        ButtonType createButtonType = new ButtonType("Criar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Nome do Produto");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantidade do Produto");

        gridPane.add(new Label("Nome do Produto:"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("Quantidade do Produto:"), 0, 1);
        gridPane.add(quantityField, 1, 1);

        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                int quantity = 0;
                try {
                    quantity = Integer.parseInt(quantityField.getText().trim());
                } catch (NumberFormatException e) {
                    // Handle invalid quantity
                }
                return new Product(name, quantity);
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(this::addProduct);
    }

    public void addProduct(Product product) {
        if (product.getName().isEmpty() || product.getQuantity() == 0) {
            showWarningDialog("Nome e quantidade do produto são obrigatórios.");
            return;
        }

        products.add(product);
        ProductCard productCard = new ProductCard(product);
        productGrid.getChildren().add(productCard);
        GridPane.setConstraints(productCard, productGrid.getChildren().size() % 3, productGrid.getChildren().size() / 3);

        // Salva os produtos atualizados
        saveProducts();
    }

    public void showDeleteProductDialog() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Excluir Produto");

        ButtonType deleteButtonType = new ButtonType("Excluir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);

        ChoiceBox<Product> productChoiceBox = new ChoiceBox<>();
        productChoiceBox.getItems().addAll(products);

        dialog.getDialogPane().setContent(productChoiceBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == deleteButtonType) {
                return productChoiceBox.getValue();
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(this::deleteProduct);
    }

    public void deleteProduct(Product product) {
        products.remove(product);
        productGrid.getChildren().removeIf(node -> ((ProductCard) node).product.equals(product));

        // Salva os produtos atualizados
        saveProducts();
    }

    public void showEditProductDialog() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Editar Produto");

        ButtonType editButtonType = new ButtonType("Salvar Alterações", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        ChoiceBox<Product> productChoiceBox = new ChoiceBox<>();
        productChoiceBox.getItems().addAll(products);

        TextField nameField = new TextField();
        nameField.setPromptText("Novo Nome do Produto");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Nova Quantidade do Produto");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        gridPane.add(new Label("Produto:"), 0, 0);
        gridPane.add(productChoiceBox , 1, 0);
        gridPane.add(new Label("Novo Nome do Produto:"), 0, 1);
        gridPane.add(nameField, 1, 1);

        gridPane.add(new Label("Nova Quantidade do Produto:"), 0, 2);
        gridPane.add(quantityField, 1, 2);

        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                Product selectedProduct = productChoiceBox.getValue();
                String newName = nameField.getText().trim();
                int newQuantity = Integer.parseInt(quantityField.getText().trim());
                selectedProduct.setName(newName);
                selectedProduct.setQuantity(newQuantity);
                return selectedProduct;
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(this::editProduct);

        // Salva os produtos atualizados
        saveProducts();
    }

    public void editProduct(Product product) {
        if (product.getName().isEmpty() || product.getQuantity() == 0) {
            showWarningDialog("Nome e quantidade do produto são obrigatórios.");
            return;
        }

        for (Node node : productGrid.getChildren()) {
            ProductCard productCard = (ProductCard) node;
            if (productCard.product.equals(product)) {
                productCard.nameLabel.setText(product.getName());
                productCard.updateQuantityLabel();
                break;
            }
        }
    }

    public void showEditImageDialog() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Editar Imagem do Produto");

        ButtonType editButtonType = new ButtonType("Salvar Alterações", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        ChoiceBox<Product> productChoiceBox = new ChoiceBox<>();
        productChoiceBox.getItems().addAll(products);

        TextField imageURLField = new TextField();
        imageURLField.setPromptText("Nova URL da Imagem");

        Button chooseImageButton = new Button("Escolher Imagem");
        chooseImageButton.setOnAction(e -> chooseImage(imageURLField));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        gridPane.add(new Label("Produto:"), 0, 0);
        gridPane.add(productChoiceBox, 1, 0);
        gridPane.add(new Label("Nova URL da Imagem:"), 0, 1);
        gridPane.add(imageURLField, 1, 1);
        gridPane.add(chooseImageButton, 2, 1);

        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                Product selectedProduct = productChoiceBox.getValue();
                String newImageURL = imageURLField.getText().trim();
                selectedProduct.setImageURL(newImageURL);
                return selectedProduct;
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(this::editProductImage);

        // Salva os produtos atualizados
        saveProducts();
    }

    public void chooseImage(TextField imageURLField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.jpeg", "*.png"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String fileURL = selectedFile.toURI().toString();
            imageURLField.setText(fileURL);
        }
    }

    public void editProductImage(Product product) {
        for (Node node : productGrid.getChildren()) {
            ProductCard productCard = (ProductCard) node;
            if (productCard.product.equals(product)) {
                // Remove a imagem anterior, se existir
                productCard.getChildren().removeIf(child -> child instanceof ImageView);
                // Adiciona a nova imagem do produto acima dos outros componentes
                ImageView imageView = new ImageView(product.getImageURL());
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                productCard.getChildren().add(0, imageView);
                break;
            }
        }
    }

    public void goBack() {
        // Implement your desired behavior for the "Voltar" button
    }

    public void performSearch(String query) {
        // Implement your search logic here
        System.out.println("Pesquisar por: " + query);
    }

 public void saveProducts() {
    File file = new File("products.txt");
    try (PrintWriter writer = new PrintWriter(file)) {
        for (Product product : products) {
            writer.println(product.getName() + ";" + product.getQuantity() + ";" + product.getImageURL());
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


public void loadProducts() {
    List<Product> loadedProducts = new ArrayList<>();

    try (FileReader fileReader = new FileReader("products.txt");
         BufferedReader reader = new BufferedReader(fileReader)) {

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(";");
            if (parts.length == 3) {
                String name = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                String imageURL = parts[2];
                Product product = new Product(name, quantity);
                product.setImageURL(imageURL);
                loadedProducts.add(product);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Error loading products.");
        return; // Exit the method if an error occurs
    }

    // Update UI only if loading was successful
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
                // Handle the exception, for example, by showing a default image
            }
        }
    }
}

    public void showWarningDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Product implements Serializable {
        private String name;
        private int quantity;
        private String imageURL;

        public Product(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
