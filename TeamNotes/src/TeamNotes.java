import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import bsh.EvalError;
import bsh.Interpreter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class TeamNotes extends Application {
	// The base size of the GUI
	private static final int MENU_SIZE = 800;
	// Create the HashMap which will map tabs (documents) to their file
	private final Map<Tab, File> documents = new HashMap<>();
	// Create the HashMap which will map tabs to their HTMLEditor
	private final Map<Tab, HTMLEditor> editors = new HashMap<>();
	// Script that allows us to get the selected (highlighted) text from an HTMLEditor (Credit: https://gist.github.com/jewelsea/7819195)
	private static final String SELECT_TEXT =
	            "(function getSelectionText() {\n" +
	            "    var text = \"\";\n" +
	            "    if (window.getSelection) {\n" +
	            "        text = window.getSelection().toString();\n" +
	            "    } else if (document.selection && document.selection.type != \"Control\") {\n" +
	            "        text = document.selection.createRange().text;\n" +
	            "    }\n" +
	            "    if (window.getSelection) {\n" +
	            "      if (window.getSelection().empty) {  // Chrome\n" +
	            "        window.getSelection().empty();\n" +
	            "      } else if (window.getSelection().removeAllRanges) {  // Firefox\n" +
	            "        window.getSelection().removeAllRanges();\n" +
	            "      }\n" +
	            "    } else if (document.selection) {  // IE?\n" +
	            "      document.selection.empty();\n" +
	            "    }" +
	            "    return text;\n" +
	            "})()";
	
	// Initializes the Open file Button
	private Button initializeOpenButton(Stage stage, TabPane tabPane) {
		final Button open = new Button("Open File");
		open.setTooltip(new Tooltip("Click to open an HTML file"));
		open.setOnAction(event -> {
			// Create the FileChooser and set its filter to only show HTML files
			final FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
			
			// Get the File the user selected and the contents into a single String then create the tab. Also check if they did not choose a file
			final File file = fileChooser.showOpenDialog(stage);
			if (file == null) {
				return;
			}
			
			createTabFromFile(tabPane, file);
		});
		
		return open;
	}
	
	// Initializes the Save file Button
	private Button initializeSaveButton(Stage stage, TabPane tabPane) {
		final Button save = new Button("Save File");
		save.setTooltip(new Tooltip("Click to save an existing file or create a new one"));
		save.setOnAction(event -> {
			// Get the File associated with the Tab. If the File does not exist, allow the user to create the File
			final Tab current = tabPane.getSelectionModel().getSelectedItem();
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
			
			// Write the HTMLEditors contents to the File
			try {
			    final BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
			    writer.write(editors.get(current).getHtmlText());
			    writer.close();
			} catch (Exception e) {
			}

			// Check if the File is null (this is a special case if the user opens the dialog menu then immediately presses escape)
			if (file == null) {
				return;
			}

			// Close and reopen the Tab after saving
			closeAndReopenTab(tabPane, current, file);
		});
		
		return save;
	}
	
	// Initializes the new document Button
	private Button initializeDocButton(TabPane tabPane) {
		final Button newTab = new Button("New Document");
		newTab.setTooltip(new Tooltip("Click to create a new, blank document"));
		newTab.setOnAction(event -> createNewTab(tabPane));
		return newTab;
	}
	
	// Initializes the run code Button
	private Button initializeCodeButton(TabPane tabPane) {
		final Button code = new Button("Run Code");
		code.setTooltip(new Tooltip("Highlight and click this button to execute Java code"));
		code.setOnAction(event -> {
			// Check if the user selected (highlighted) any text
			final Tab current = tabPane.getSelectionModel().getSelectedItem();
			final String selected = getSelectedText(current);
			if (selected == null || selected.isEmpty()) {
				return;
			}
			
			// BeanShell 
			final Interpreter i = new Interpreter();
			String output = "Input:\n> " + selected + "\n\nOutput:\n> ";
			try {
				output += i.eval(selected);
				//output += 
			} catch (EvalError e) {
				//output += e.getMessage();
				output += e.toString();
			}
			
			// Create the components for the Stage that has the 
			final BorderPane borderPane = new BorderPane();
			borderPane.setStyle("-fx-background-color: BLACK");
			final Text text = new Text(output);
			text.setFill(Color.WHITE);
			text.setStyle("-fx-font-size: 16px;");
			borderPane.setCenter(text);
			
			final Scene outputScene = new Scene(borderPane, 500, 500);
			final Stage outputStage = new Stage();
			outputStage.getIcons().add(new Image("Icon.png"));
			outputStage.setTitle(current.getText() + " Output");
			outputStage.setScene(outputScene);
			outputStage.show();
        });
		
		return code;
	}
	
	// Creates a new Tab (document) on a TabPane
	private void createNewTab(TabPane tabPane) {
		// Create the Tab and HTMLEditor components
        final Tab tab = addCloseListener(new Tab("Untitled Doc"), tabPane); 
        final HTMLEditor editor = new HTMLEditor();
  
        // Add the HTMLEditor 
        tab.setContent(editor); 
        tabPane.getTabs().add(tab); 
            
        documents.put(tab, null);
        editors.put(tab, editor);
	}
	
	// Creates a new Tab (document) and fills the document with the File's contents
	private void createTabFromFile(TabPane tabPane, File file) {
        // Create the Tab and the HTMLEditor component
        final Tab tab = addCloseListener(new Tab(file.getName()), tabPane); 
        final HTMLEditor editor = new HTMLEditor();
		final StringBuilder builder = new StringBuilder();
		
		try {
		    final BufferedReader in = new BufferedReader(new FileReader(file));
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
	
	// Adds a close listener to a tab that when closed checks if its the last remaining file. If so it opens a new doc so there is always a doc open
	private Tab addCloseListener(Tab tab, TabPane tabPane) {
		tab.setOnClosed(event -> {
			if (tabPane.getTabs().size() == 0) {
				createNewTab(tabPane);
			}
		});
		return tab;
	}
	
	// Closes a Tab and removes it from the internal maps
	private void closeTab(TabPane tabPane, Tab tab) {
		documents.remove(tab);
		editors.remove(tab);
		tabPane.getTabs().remove(tab);
	}
	
	// Closes, saves, and reopens a Tab 
	private void closeAndReopenTab(TabPane tabPane, Tab tab, File file) {
		// Get the index of the Tab from the TabPane's getTabs() List
		final int index = tabPane.getTabs().indexOf(tab);
		
		// Remove the Tab from the TabPane and remove the Tab from the documents/editors Maps
		tabPane.getTabs().remove(tab);
		documents.remove(tab);
		editors.remove(tab);
		
		// Create the new Tab and its HTML Editor then set the HTMLEditors text to the contents of the file
		final Tab newTab = addCloseListener(new Tab(file.getName()), tabPane);
		final HTMLEditor editor = new HTMLEditor();
		final StringBuilder builder = new StringBuilder();
		try {
		    final BufferedReader in = new BufferedReader(new FileReader(file));
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
		
        documents.put(newTab, file);
        editors.put(newTab, editor);
	}
	
	// Method to get the selected (highlighted) text in a HTMLEditor
	private String getSelectedText(Tab tab) {
        final WebView webView = (WebView) editors.get(tab).lookup("WebView");
        if (webView == null) {
        	return null;
        }
        
        final WebEngine engine = webView.getEngine();
        final Object selection = engine.executeScript(SELECT_TEXT);
     	return (selection instanceof String) ? (String) selection : null;
	}

	
	@Override
	public void start(Stage stage) {
		// Create the TabPane (and create blank tab so the GUI isnt empty when run) and HBox pane (will be the button bar)
		final TabPane tabPane = new TabPane();
		createNewTab(tabPane);
		
		// Create the HBox which will act as the button bar, create all the buttons, and add all the buttons to the bar (HBox)
		final HBox buttonPane = new HBox();
		final Button open = initializeOpenButton(stage, tabPane);
		final Button save = initializeSaveButton(stage, tabPane);
		final Button doc = initializeDocButton(tabPane);		
		final Button code = initializeCodeButton(tabPane);
		buttonPane.getChildren().addAll(open, save, doc, code);
		
		// Create the root VBox pane, set the background color to white and add the button bar/tab pane to the root pane
		final VBox root = new VBox();
		root.setStyle("-fx-background-color: WHITE");
        root.getChildren().add(buttonPane);
        root.getChildren().add(tabPane);
        
        // Add an icon and title to the Stage
        stage.getIcons().add(new Image("Icon.png"));
        stage.setTitle("JNotes");
        
        // Create the Scene, set the Stage's Scene, and make the Stage visible
		final Scene scene = new Scene(root, MENU_SIZE, MENU_SIZE);
		stage.setScene(scene);
		stage.show();		
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
}
	
