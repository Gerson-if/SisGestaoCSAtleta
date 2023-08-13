package sisgestaocsatleta;

// importação das classes 
import modulos.Nioaque;
import modulos.Aquidauana;

// importação de bibliotecas padrao
import static java.awt.SystemColor.menu;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SisGestaoCsAtleta extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        HBox root = new HBox(10);
        root.setStyle("-fx-background-color: #d3ffd3;"); // Fundo verde claro
        
        Button btnNioaque = createMenuButton("Nioaque", "/icones/dinosaur.png");
        Button btnJardim = createMenuButton("Jardim", "/icones/parque-natural.png");
        Button btnAquidauana = createMenuButton("Aquidauana", "/icones/estoque.png");
        Button btnBonito = createMenuButton("Bonito", "/icones/estoque1.png");
        Button btnSidrolandia = createMenuButton("Sidrolandia", "/icones/cidade.png");
        Button btnCampoGrande = createMenuButton("Campo Grande", "/icones/cidade2.png");
        Button btnMaracaju = createMenuButton("Maracaju", "/icones/estoque.png");
        
        root.getChildren().addAll(btnNioaque, btnJardim, btnAquidauana, btnBonito, btnSidrolandia, btnCampoGrande, btnMaracaju);
        
        Scene scene = new Scene(root, 800, 60); // Altura menor para a barra de navegação
        
        primaryStage.setTitle("Sistema de Gestão de Atleta");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
  private Button createMenuButton(String menuName, String iconName) {
        Button button = new Button(menuName);
        button.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: #228B22; -fx-text-fill: white;");
        button.setGraphic(createIcon(iconName));
        
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (menuName.equals("Nioaque")) {
                 
                Nioaque nioaqueMenu = new Nioaque();
                Stage nioaqueStage = new Stage();
                nioaqueMenu.start(nioaqueStage);
                }
                
                if (menuName.equals("Aquidauana")){
                Aquidauana aquidauanaMenu = new Aquidauana();
                Stage aquidaunaStage = new Stage();
                aquidauanaMenu.start(aquidaunaStage);
                
                
                }
                // ... Outros botões ...
            }
        });
        return button;
    }
    
    private ImageView createIcon(String iconName) {
        Image iconImage = new Image(getClass().getResourceAsStream(iconName));
        ImageView imageView = new ImageView(iconImage);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        return imageView;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
