import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class TeamNotes extends Application {
	private static final int MENU_SIZE = 800;
	// Create the HashMap which will map tabs (documents) to their file
	private final Map<Tab, File> documents = new HashMap<>();
	// Create the HashMap which will map tabs to their HTMLEditor
	private final Map<Tab, HTMLEditor> editors = new HashMap<>();

	public static void main(String[] args) {
		Application.launch(args);
	}
	
	private void createNewTab(TabPane tabPane) {
            // Create the Tab and HTMLEditor components
            final Tab tab = new Tab("Untitled Doc"); 
            final HTMLEditor editor = new HTMLEditor();
  
            // Add the HTMLEditor 
            tab.setContent(editor); 
            tabPane.getTabs().add(tab); 
            
            documents.put(tab, null);
            editors.put(tab, editor);
	}
	
	private void createTab(TabPane tabPane, File file) {
        // Create the Tab and dont allow it to be closed if the TabPane has a length of 
        final Tab tab = new Tab(file.getName()); 
        
        // Create the HTMLEditor component
        final HTMLEditor editor = new HTMLEditor();
        
		final StringBuilder builder = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new FileReader(file));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	builder.append(str);
		    }
		    in.close();
		} catch (IOException e) {
		}

        // Set the HTMLEditors content, add the HTMLEditor to the Tab, add the Tab to the TabPane
        editor.setHtmlText(builder.toString());
        tab.setContent(editor); 
        tabPane.getTabs().add(tab); 
        
        documents.put(tab, file);
        editors.put(tab, editor);
	}
	
	private void closeTab(TabPane tabPane, Tab tab) {
		documents.remove(tab);
		editors.remove(tab);
		tabPane.getTabs().remove(tab);
	}
	
	private void closeAndReopenTab(TabPane tabPane, Tab tab, File file) {
		// Get the index of the Tab from the TabPane's getTabs() List
		final int index = tabPane.getTabs().indexOf(tab);
		
		// Remove the Tab from the TabPane and remove the Tab from the documents/editors Maps
		tabPane.getTabs().remove(tab);
		documents.remove(tab);
		editors.remove(tab);
		
		// Create the new Tab and its HTML Editor then set the HTMLEditors text to the contents of the file
		final Tab newTab = new Tab(file.getName());
		final HTMLEditor editor = new HTMLEditor();
		final StringBuilder builder = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new FileReader(file));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	builder.append(str);
		    }
		    in.close();
		} catch (IOException e) {
		}
        editor.setHtmlText(builder.toString());
		
        // Set the HTMLEditor to the Tab and add the Tab to the TabPane
        newTab.setContent(editor);
		tabPane.getTabs().add(index, newTab);
		
		// Map the Tab to its File/HTMLEditor
        documents.put(tab, file);
        editors.put(tab, editor);
	}
	
	@Override
	public void start(Stage stage) {
		// Create the root VBox pane
		final VBox root = new VBox();
		root.setStyle("-fx-background-color: WHITE");
		
		// Create the TabPane (and create blank tab so the GUI isnt empty when run) and HBox pane (will be the button bar)
		final TabPane tabPane = new TabPane();
		createNewTab(tabPane);
		final HBox buttonPane = new HBox();
		
		// Create the Save, Open, and New Doc buttons and define their listeners
		final Button save = new Button("Save");
		save.setOnAction(event -> {
			// Get the current Tab, make sure it exists (the current Tab would not exist if no Tabs are opened in the TabPane)
			final Tab current = tabPane.getSelectionModel().getSelectedItem();
			if (current == null) {
				return;
			}
		
			// Get the File associated with the Tab. If the File does not exist, allow the user to create the File
			File file = documents.get(current);
			if (file == null) {
				final FileChooser chooser = new FileChooser();
				chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
				try {
					// Set the FileChooser to save dialog, and allow the user to enter the file they want to create. Then check if the file ends with .html
					file = chooser.showSaveDialog(stage);
					file.createNewFile();
				} catch (Exception e) {
				}
					
			} 
			
			try {
			    final BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
			    writer.write(editors.get(current).getHtmlText());
			    writer.close();
			} catch (Exception e) {
			}

			if (file == null) {
				return;
			}
			closeAndReopenTab(tabPane, current, file);
		});
		final Button open = new Button("Open");
		open.setOnAction(event -> {
			// Create the FileChooser and set its filter to only show HTML files
			final FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
			
			// Get the File the user selected and the contents into a single String then create the tab. Also check if they did not choose a file
			final File file = fileChooser.showOpenDialog(stage);
			if (file == null) {
				return;
			}
			
			createTab(tabPane, file);
		});
		final Button newTab = new Button("New");
		newTab.setOnAction(event -> createNewTab(tabPane));
		
		// Add the Buttons to the HBox and add the two panes to the root pane
		buttonPane.getChildren().addAll(open, save, newTab);
        root.getChildren().add(buttonPane);
        root.getChildren().add(tabPane);
		
        // Create the Scene, set the Stage's Scene, and make the Stage visible
		final Scene scene = new Scene(root, MENU_SIZE, MENU_SIZE);
		stage.setScene(scene);
		stage.show();
	}
	
	
}
