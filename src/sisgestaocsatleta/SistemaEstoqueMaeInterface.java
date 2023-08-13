package sisgestaocsatleta;
import javafx.scene.control.TextField;
public interface SistemaEstoqueMaeInterface {
   public  void showCreateProductDialog();
    public void addProduct(SistemaEstoqueMae.Product product);
    public void showDeleteProductDialog();
    public void deleteProduct(SistemaEstoqueMae.Product product);
    public void showEditProductDialog();
    public void editProduct(SistemaEstoqueMae.Product product);
    public void showEditImageDialog();
    public void chooseImage(TextField imageURLField);
    public void editProductImage(SistemaEstoqueMae.Product product);
    public void goBack();
    public void performSearch(String query);
    public void saveProducts();
    public void loadProducts();
    public void showWarningDialog(String message);
}
