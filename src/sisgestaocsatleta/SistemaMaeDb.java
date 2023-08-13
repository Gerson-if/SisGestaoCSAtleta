package sisgestaocsatleta;
import sisgestaocsatleta.SistemaEstoqueMae;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SistemaMaeDb {

    private Connection connection;

    public SistemaMaeDb() {
        createDatabaseConnection();
    }

    private void createDatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:products.db");
            System.out.println("Conexão com o banco de dados estabelecida.");

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS products (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "image_url TEXT)");
                System.out.println("Tabela 'products' criada ou já existe.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    public void saveProducts(List<SistemaEstoqueMae.Product> products) {
        createDatabaseConnection();

        try (PreparedStatement selectStatement = connection.prepareStatement(
                     "SELECT name FROM products WHERE name = ?");
             PreparedStatement insertStatement = connection.prepareStatement(
                     "INSERT OR REPLACE INTO products (name, quantity, image_url) VALUES (?, ?, ?)")) {

            for (SistemaEstoqueMae.Product product : products) {
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

    public void removeProductFromDatabase(String productName) {
        createDatabaseConnection();

        try {
            String query = "DELETE FROM products WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, productName);
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

    public List<SistemaEstoqueMae.Product> loadProducts() {
        createDatabaseConnection();
        List<SistemaEstoqueMae.Product> products = new ArrayList<>();

        try {
            String query = "SELECT * FROM products";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                String imageURL = resultSet.getString("image_url");
                SistemaEstoqueMae.Product product = new SistemaEstoqueMae.Product(name, quantity);
                product.setImageURL(imageURL);
                products.add(product);
            }

            statement.close();
            resultSet.close();

            System.out.println("Produtos carregados do banco de dados com sucesso.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar produtos do banco de dados: " + e.getMessage());
        } finally {
            closeDatabaseConnection();
        }

        return products;
    }
}
