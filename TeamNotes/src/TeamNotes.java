import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

public class TeamNotes extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}
	
	@Override
	public void start(Stage stage) {
		final VBox root = new VBox();
		
		final HBox buttonPane = new HBox();
		for (String str : new String[]{"File", "Edit", "Run"} ) {
			buttonPane.getChildren().add(new Button(str));
		}
		
		final TabPane tabPane = new TabPane();
        for (int i = 0; i < 10; i++) { 
        	  
            // create Tab 
            Tab tab = new Tab("Tab_" + (int)(i + 1)); 
            
            final HTMLEditor editor = new HTMLEditor();
  
            // add label to the tab  
            tab.setContent(editor); 
  
            // add tab 
            tabPane.getTabs().add(tab); 
        } 

        root.getChildren().add(buttonPane);
        root.getChildren().add(tabPane);
		
		final Scene scene = new Scene(root, 800, 800);
		stage.setScene(scene);
		stage.show();
	}
	
	
}
