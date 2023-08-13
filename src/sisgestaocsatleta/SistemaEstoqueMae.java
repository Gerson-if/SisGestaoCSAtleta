package sisgestaocsatleta;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static javafx.application.Application.launch;
import javafx.scene.Node;

public abstract class SistemaEstoqueMae extends Application implements SistemaEstoqueMaeInterface{

public Connection connection;


public void createDatabaseConnection() {
    try {
        // Tenta estabelecer a conexão com o banco de dados SQLite
        connection = DriverManager.getConnection("jdbc:sqlite:products.db");
        System.out.println("Conexão com o banco de dados estabelecida.");

        // Cria a tabela "products" se ela não existir
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "image_url TEXT)");
            System.out.println("Tabela 'products' criada ou já existe.");
        }

    } catch (SQLException e) {
        // Em caso de erro, imprime o rastreamento do erro
        e.printStackTrace();
    }
}


// Método para fechar a conexão com o banco de dados
public void closeDatabaseConnection() {
    if (connection != null) {
        try {
            connection.close();
            System.out.println("Conexão com o banco de dados fechada.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

    
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
    public class ProductCard extends VBox {
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

            public void increaseQuantity() {
            product.setQuantity(product.getQuantity() + 1);
            updateQuantityLabel();
            saveProductToDatabase(product); // Salva a nova quantidade no banco de dados
            }

        public void decreaseQuantity() {
            if (product.getQuantity() > 0) {
                product.setQuantity(product.getQuantity() - 1);
                updateQuantityLabel();
                saveProductToDatabase(product); // Salva a nova quantidade no banco de dados
            }
        }


        public void updateQuantityLabel() {
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

    // Remove o produto do banco de dados também
    removeProductFromDatabase(product);

    // Salva os produtos atualizados (opcional, dependendo do fluxo do seu programa)
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
// salva imagem no banco de dados
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

            // Salva as alterações no banco de dados, incluindo a nova URL da imagem
            saveProductToDatabase(product);

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
    createDatabaseConnection();

    try (PreparedStatement selectStatement = connection.prepareStatement(
            "SELECT name FROM products WHERE name = ?");
         PreparedStatement insertStatement = connection.prepareStatement(
            "INSERT OR REPLACE INTO products (name, quantity, image_url) VALUES (?, ?, ?)")) {

        for (Product product : products) {
            String productName = product.getName();

            // Verifica se o produto já existe no banco de dados
            selectStatement.setString(1, productName);
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                // O produto já existe, então não fazemos nada
                continue;
            }
            resultSet.close();

            // Insere o novo produto no banco de dados
            insertStatement.setString(1, productName);
            insertStatement.setInt(2, product.getQuantity());
            insertStatement.setString(3, product.getImageURL());
            insertStatement.executeUpdate();
        }
    } catch (SQLException e) {
        System.err.println("Erro ao salvar produtos: " + e.getMessage());
        e.printStackTrace();
    } finally {
        closeDatabaseConnection();
    }
}

public void loadProducts() {
    createDatabaseConnection();

    try {
        String query = "SELECT * FROM products";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        products.clear(); // Limpa a lista de produtos existente

        while (resultSet.next()) {
            String name = resultSet.getString("name");
            int quantity = resultSet.getInt("quantity");
            String imageURL = resultSet.getString("image_url");
            Product product = new Product(name, quantity);
            product.setImageURL(imageURL);
            products.add(product);
        }

        statement.close();
        resultSet.close();

        // Atualiza a exibição dos produtos na tela
        updateProductGrid();

        System.out.println("Produtos carregados do banco de dados com sucesso.");
    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Erro ao carregar produtos do banco de dados: " + e.getMessage());
    } finally {
        closeDatabaseConnection();
    }
}

// motodo para atualizar imagem e quantidade
public void saveProductToDatabase(Product product) {
    createDatabaseConnection();

    String updateQuery = "UPDATE products SET quantity = ?, image_url = ? WHERE name = ?";
    
    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
        updateStatement.setInt(1, product.getQuantity());
        updateStatement.setString(2, product.getImageURL());
        updateStatement.setString(3, product.getName());
        
        int rowsAffected = updateStatement.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Produto atualizado no banco de dados com sucesso.");
        } else {
            System.err.println("Produto não encontrado no banco de dados.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Erro ao atualizar produto no banco de dados: " + e.getMessage());
    } finally {
        closeDatabaseConnection();
    }
}


// metodo de apagar product 
public void removeProductFromDatabase(Product product) {
    createDatabaseConnection();

    try {
        String query = "DELETE FROM products WHERE name = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, product.getName());
        statement.executeUpdate();

        statement.close();

        System.out.println("Produto removido do banco de dados com sucesso.");
    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Erro ao remover produto do banco de dados: " + e.getMessage());
    } finally {
        closeDatabaseConnection();
    }
}

// Método para atualizar a exibição dos produtos na tela
public void updateProductGrid() {
    productGrid.getChildren().clear(); // Remove os produtos atuais da exibição

    for (Product product : products) {
        ProductCard productCard = new ProductCard(product);
        productGrid.getChildren().add(productCard);
        GridPane.setConstraints(productCard, productGrid.getChildren().size() % 3, productGrid.getChildren().size() / 3);
    }
}

    public void showWarningDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public  class Product implements Serializable {
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
