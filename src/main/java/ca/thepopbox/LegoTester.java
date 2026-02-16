package ca.thepopbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

public class LegoTester extends Application implements Serializable {
    //global variables
    LegoColor selectedColor = null; //selected color for the part;
    private static final String FILE_PATH = System.getProperty("user.home") + "/lego_sets.dat";
    Button dashboardButton = new Button("Dashboard"); //button to add a new set
    LegoSet[] legoSets = new LegoSet[5]; //lego set list
    LegoCompletePart[] completeParts = new LegoCompletePart[5]; //lego complete part list
    int currentSetIndex = 0; //current index of the set list
    int currentCompletePartIndex = 0; //current index of the complete part list
    VBox leftBox = new VBox(); //main box of the left side of the UI
    //creates the border pane
    //the border pane is the main layout of the application
    BorderPane root = new BorderPane();
    LegoSet activeLegoSet; //the active lego set that is being displayed in the center of the UI
    VBox mainCenterBox = new VBox(); //the main center box of the UI
    ScrollPane centerScrollPane = new ScrollPane(); //creates a new scroll pane
    ScrollPane completePartOverviewScrollPane = new ScrollPane(); //creates a new scroll pane for the complete part overview
    ScrollPane setScrollBox = new ScrollPane(); // creates a new scroll pane for the sets
    VBox setsVbox = new VBox(); //vbox for holding the sets
    HBox setsHBox = new HBox(); //hbox for holding the sets header
    public static void main(String[] args) {
        launch(args);
    } 

    @Override
    public void start(Stage primaryStage) {
        
        //sets the top section of the border pane
        VBox topBoxHolder = new VBox(); //creates a new vbox for the top section
        
        HBox topBox = new HBox(); //reference to the top section of the border pane
        topBox.setStyle("-fx-alignment: center; -fx-padding: 10px;"); // Set padding and alignment
        Text topText = new Text("Part Management System"); //title text
        topText.setId("page-title");
        topBox.getChildren().add(topText);
        HBox navBar = new HBox(); //creates a new hbox for the nav bar   
        navBar.setPadding(new Insets(0, 0, 10, 0)); // 10 pixels bottom padding
        navBar.setAlignment(javafx.geometry.Pos.CENTER);
        navBar.setSpacing(10); // Set spacing between elements
        dashboardButton.setOnAction(e -> {
            saveLegoSets(legoSets); //calls the save method to save the sets
            createMainDashBoard(); //calls the create main dashboard method
        });
        Button addNewSetButton = new Button("Add New Set"); // Create a button to add a new set
        addNewSetButton.setOnAction(e -> {
            Stage newSetStage = new Stage(); //creates a new stage for the image
            newSetStage.setTitle("Add New Set"); //sets the title of the stage
            newSetStage.initModality(Modality.APPLICATION_MODAL); //sets the modality of the stage
            newSetStage.initStyle(StageStyle.UTILITY); //sets the style of the stage
            StackPane popupImage = new StackPane((VBox) addNewSetGUI()); //creates a new stack pane for the image
            popupImage.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            newSetStage.setScene(new Scene(popupImage, 250, 150)); //sets the scene of the stage
            newSetStage.show(); //shows the stage
        });

        Button imageManagerButton = new Button("Image Manager");
        imageManagerButton.setOnAction(e -> {
            // Create a new Stage (window)
            Stage imageManagerStage = new Stage();
            imageManagerStage.setTitle("Upload Part Image");

            // Get or create directory
            String userHome = System.getProperty("user.home");
            File imageDir = new File(userHome, "partmanagerimages");
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            // UI Components
            TextField itemIdField = new TextField();
            itemIdField.setPromptText("Enter Part ID");

            Text promptText = new Text("Enter Part ID");
            promptText.setId("scroll-entry");

            Text title = new Text("Upload Part Image");
            title.setId("scroll-title");

            // Create an instance of ColorManager
            ColorManager colorManager = new ColorManager();

            // Create ComboBox and set prompt text
            ComboBox<String> colorComboBox = new ComboBox<>();
            colorComboBox.setEditable(true);  // Allow user to type and filter
            colorComboBox.setPromptText("Select Color");

            // Create a FilteredList to handle filtering of items based on input
            ObservableList<String> colors = colorManager.getColorList();
            FilteredList<String> filteredList = new FilteredList<>(colors, s -> true);

            // Bind the filtered list to the ComboBox
            colorComboBox.setItems(filteredList);

            // Flag to prevent filtering when the value is programmatically changed
            BooleanProperty programmaticChange = new SimpleBooleanProperty(false);

            // Add a listener to filter the ComboBox based on user input
            colorComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                if (!programmaticChange.get()) {
                    // Only filter if it's not a programmatic change
                    filteredList.setPredicate(color -> {
                        // Check if the color name contains the entered text (case-insensitive)
                        return newValue == null || color.toLowerCase().contains(newValue.toLowerCase());
                    });
                }
            });

            // Prevent value setting from triggering filter again
            colorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !programmaticChange.get()) {
                    programmaticChange.set(true);  // Set the flag to prevent filtering
                    colorComboBox.getEditor().setText(newVal);  // Set the text manually without triggering the filter
                    programmaticChange.set(false);  // Reset the flag
                }
            });

            Text statusLabel = new Text(); // For feedback
            statusLabel.setId("scroll-title");

            Button uploadImageButton = new Button("Select and Upload PNG Image");
            uploadImageButton.setOnAction(uploadEvent -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select PNG Image");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Images", "*.png"));
                File selectedFile = fileChooser.showOpenDialog(imageManagerStage);

                if (selectedFile != null) {
                    String itemId = itemIdField.getText().trim();
                    String selectedColor = colorComboBox.getValue();

                    if (itemId.isEmpty() || selectedColor == null) {
                        statusLabel.setText("Please enter part ID and select a color.");
                        return;
                    }

                    // Save image as ITEMID-COLOR.png
                    File targetFile = new File(imageDir, itemId + "-" + selectedColor.toLowerCase() + ".png");
                    try {
                        Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        statusLabel.setText("Image uploaded successfully.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        statusLabel.setText("Failed to save image.");
                    }
                } else {
                    statusLabel.setText("No image selected.");
                }
            });

            //close button
            Button closeButton = new Button("Close");
            closeButton.setOnAction(i -> {
                updateAllSetPartPhotos();
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
                
            });

            VBox content = new VBox(10, title,promptText,itemIdField, colorComboBox, uploadImageButton, statusLabel, closeButton);
            content.setAlignment(Pos.CENTER);
            content.setPadding(new Insets(20));

            StackPane stackPane = new StackPane(content);
            Scene scene = new Scene(stackPane, 475, 300);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            imageManagerStage.setScene(scene);
            imageManagerStage.initOwner(primaryStage); // optional: makes it modal to main window
            imageManagerStage.show();
        });



        Button exportButton = new Button("Export"); // Create a button to export parts for bricklink
        exportButton.setOnAction(e -> {
            generateXML();
        });
        exportButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        exportButton.setAlignment(Pos.CENTER);  // Ensure text is centered
        addNewSetButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        addNewSetButton.setAlignment(Pos.CENTER);  // Ensure text is centered
        dashboardButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        dashboardButton.setAlignment(Pos.CENTER);  // Ensure text is centered
        imageManagerButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        imageManagerButton.setAlignment(Pos.CENTER);  // Ensure text is centered
        navBar.getChildren().addAll(dashboardButton, addNewSetButton, imageManagerButton, exportButton); //adds the dashboard button to the nav bar
        topBoxHolder.getChildren().addAll(topBox, navBar);
        root.setTop(topBoxHolder);

        //creates the left side of the screen for sets to be entered
        createLeftSideOfUI();
        setScrollBox.setFitToHeight(true); //sets the scroll pane to fit to height
        setScrollBox.setFitToWidth(true); //sets the scroll pane to fit to width
        setScrollBox.setId("set-scroll-bg");
        setScrollBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide horizontal scrollbar
        setScrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide vertical scrollbar
        setsVbox.setPadding(new Insets(5,10,0,5));
        setsVbox.setId("set-scroll-bg");
        setScrollBox.setContent(setsVbox);
        leftBox.getChildren().add(setsHBox); //adds the dashboard button & header to the left vbox
        leftBox.getChildren().add(setScrollBox); //adds the scroll pane to the left side of the screen for sets

        Scene scene = new Scene(root);
        Font.loadFont(getClass().getResourceAsStream("/Solway-Bold.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/Solway-Regular.ttf"), 12);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Part Management System, Created by ThePopBox"); //sets the title of the window
        primaryStage.setScene(scene); 
        primaryStage.minWidthProperty().set(1080); //sets the minimum width of the window
        primaryStage.minHeightProperty().set(400); //sets the minimum height of the window
        primaryStage.show();

        //loads the lego sets from the .dat file in the home directory
        loadLegoSetsToUI();

        createMainDashBoard(); //creates the main dashboard
    }

    //updates the lego part pictures
    public void updateAllSetPartPhotos() {
    for (LegoSet set : legoSets) { // allLegoSets should be your list of sets
        if (set != null) {
            set.updatePartPhoto();
            } 
        }
    createMainDashBoard(); //creates the main dashboard
    }

    public void loadLegoSetsToUI(){
        LegoSet[] myLegoSets = loadLegoSets();
        if (myLegoSets != null && myLegoSets.length > 0 ) {
            legoSets = myLegoSets; //loads the lego sets from the file
            for (LegoSet legoSet : legoSets) { //loops through every set in the list
                if (legoSet != null) { //checks if the set is not null
                    updateUIAfterSetCreation(currentSetIndex); //updates the UI after the set is loaded
                    currentSetIndex++; //increases the current index
                }else {
                 break; // stop at first null since your array is tightly packed
            }
            }
        } else {
            legoSets = new LegoSet[5]; //creates a new lego set array
            currentSetIndex = 0;
        }
    }

    public void createLeftSideOfUI(){
        //sets the left section of the border pane
        
        leftBox.setPadding(new Insets(0, 10, 0, 30)); // Set padding
        HBox textBox = new HBox(); //creates a new hbox for the text
        Text leftBoxHeader = new Text("Set List"); //header text for left vbox
        leftBoxHeader.setId("page-title");
        textBox.setAlignment(Pos.CENTER);
        textBox.getChildren().add(leftBoxHeader); //adds the text to the hbox
        setsHBox.getChildren().add(leftBoxHeader);
        root.setLeft(leftBox);
    }

    //creates a HBox to add a new set #used else where.
    public VBox addNewSetGUI() {
        VBox newSetCreationBox = new VBox(4); //creates a new vbox for the new set creation
        newSetCreationBox.setAlignment(Pos.CENTER);
        Text title = new Text("Add New Set"); //text for the set number
        title.setId("scroll-title");
        Text setNumbertext = new Text("Set Number");
        setNumbertext.setId("scroll-entry");
        TextField setNumberField = new TextField(); //text field for the set number

        //hbox to center and restrict the input field from taking up too much space
        HBox textInputBox = new HBox();
        textInputBox.setAlignment(Pos.CENTER);
        textInputBox.setPadding(new Insets(0,20,0,20));
        textInputBox.getChildren().add(setNumberField);

        //hbox to hold the button
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER);
        
        //close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        // Use a filter to allow only digits to be entered
        setNumberField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            // If the typed character is not a digit, consume the event
            if (!Character.isDigit(event.getCharacter().charAt(0))) {
                event.consume();
            }
        });
        Button addSetButton = new Button("Add Set"); //button to add the set 
        addSetButton.setOnAction(e -> {
            String setNumber = setNumberField.getText(); //gets the text from the text field
            if (setNumber.isEmpty()) { //checks if the text field is empty  
                // Set the hint text (prompt text)
                setNumberField.setPromptText("Enter a set number");
                // Apply custom CSS to change the color of the hint text (prompt text)
                setNumberField.setStyle("-fx-prompt-text-fill: red;");
            } else {
                if (checkForSet(Integer.parseInt(setNumberField.getText())) == false) {
                    addSet(Integer.parseInt(setNumberField.getText())); //calls the add set method
                    updateUIAfterSetCreation(currentSetIndex - 1); //updates the UI after the set is created
                    setNumberField.clear(); //clears the text field
                    // Set the hint text (prompt text)
                    setNumberField.setPromptText("Set number added");
                    setNumberField.setStyle("-fx-prompt-text-fill: green;");
                    //call the save method to save the sets
                    saveLegoSets(legoSets);
                } else {
                    setNumberField.clear(); //clears the text field
                    // Set the hint text (prompt text)
                    setNumberField.setPromptText("Set number already exists");
                    setNumberField.setStyle("-fx-prompt-text-fill: red;");
                }
            }
            
        });
        buttonBox.getChildren().addAll(addSetButton, closeButton);
        newSetCreationBox.getChildren().addAll(title,setNumbertext, textInputBox,buttonBox); //adds all the elements to the new set creation vbox
        return newSetCreationBox; //returns the new set creation vbox
    }

    //checks if the set number is already in the list
    public boolean checkForSet(int setNumber) {
        for (LegoSet legoSet : legoSets) { //loops through every set in the list
            if (legoSet != null && legoSet.getSetNumber() == setNumber) { //checks if the set number is already in the list
                return true; //returns true if found
            }
        }
        return false; //returns false if not found
    }

    //adds a new set to the list
    public void addSet(int setNumber) {
        if (checkForSet(setNumber) == false) { //checks if the set number is already in the list
            LegoSet set = new LegoSet(setNumber); //creates a new lego set
            listSizeCheck(currentSetIndex); //checks the list size and expands it if needed
            legoSets[currentSetIndex] = set; //adds the set to the list
            currentSetIndex++; //increases the current index
        }
    }

    //checks the list size and expands it if needed
    public void listSizeCheck(int currentSetIndex) {
        if (currentSetIndex == legoSets.length) { //checks if the current index is equal to the length of the list
            int newSize = legoSets.length * 2; //expands the current size by x2
            LegoSet[] temp = new LegoSet[newSize]; //temp set array to hold all old entries
            for (int i = 0; i < legoSets.length; i++) { //loops through every set in the list
                temp[i] = legoSets[i]; //moves all entries over to the temp
            }
            legoSets = temp; //changes the pointer for lego sets to temp
        }
    }

    //adds all sets to the left Vbox as buttons
    public void addSetsToUI(){
        //loads the lego sets from the .dat file in the home directory
        LegoSet[] myLegoSets = loadLegoSets(); //loads lego sets from the file
        if (myLegoSets != null && myLegoSets.length > 0 ) {
            currentSetIndex = 0; //sets the current set index to 0
            legoSets = myLegoSets; //makes the legosets pointer to the loaders
            for (int i = 0; i < legoSets.length; i++) { //loops through every set in the list
                if (legoSets[i] != null) { //checks if the set is not null
                    updateUIAfterSetCreation(i); //updates the UI after the set is loaded
                    currentSetIndex++; //increases the current index
                } else {
                    break; //breaks the loop because there are no more sets
                }
            }
        } else {
            legoSets = new LegoSet[5]; //creates a new lego set array
            currentSetIndex = 0;
        }
    }

    //updates the UI after a set is created
    public void updateUIAfterSetCreation(int currentSetIndex) {
        Button button = new Button("#" + legoSets[currentSetIndex].getSetNumber()); //creates a button for the set
        button.setMaxWidth(Double.MAX_VALUE);// Fill full width
        button.setAlignment(Pos.CENTER);  // Ensure text is centered
        VBox.setMargin(button, new Insets(2, 0, 2, 0)); // top, right, bottom, left
        button.setUserData(legoSets[currentSetIndex]);
        button.setOnAction(e -> {
            mainCenterBox.getChildren().clear(); //clears the center vbox
            createSetGUI((LegoSet)button.getUserData()); //casts the user data to a lego set 
            updateScrollPane(); //updates the scroll pane

        });
        
        setsVbox.getChildren().add(button); //sets the content of the set scroll pane
    }

    //create the center vbox for the set
    public void createSetGUI(LegoSet legoSet) {
        activeLegoSet = legoSet;
        
        Text setTittletext = new Text("Set #" + activeLegoSet.getSetNumber()); //text for the set number
        setTittletext.setId("page-title");
        HBox titleBox = new HBox(); //creates a new hbox for the title
        Button deleteSetButton = new Button("Delete Set"); //button to delete the set
        deleteSetButton.setOnAction(e -> {
            for (int i = 0; i < legoSets.length; i++) { //loops through every set in the list
                if (legoSets[i] != null && legoSets[i].getSetNumber() == activeLegoSet.getSetNumber()) { //checks if the set number is already in the list
                    legoSets[i] = null; //sets the set to null
                    LegoSet[] temp = new LegoSet[legoSets.length]; //creates a new set array
                    int tempIndex = 0; //temp index for the new array
                    for (int x = 0; x < legoSets.length; x++) {
                        if (legoSets[x] != null) { //checks if the set is not null
                            temp[tempIndex] = legoSets[x]; //adds the set to the new array
                            tempIndex++; //increases the temp index
                        }
                    }
                    legoSets = temp; //sets the lego sets pointer to the new array
                    break; //breaks the loop
                }
            }   
            currentSetIndex = 0; 
            saveLegoSets(legoSets);
            setsVbox.getChildren().clear(); //clears the vbox of all sets buttons inside the scroll box
            setsHBox.getChildren().clear(); //clears the hbox of the sets header
            createLeftSideOfUI();  // Adds dashboard button and header
            addSetsToUI(); //adds all sets to the left vbox as buttons
            createMainDashBoard(); //calls the create main dashboard method
        });
        Region spacerR = new Region(); //creates a new region for the spacer
        HBox.setHgrow(spacerR, Priority.ALWAYS);
        Region spacerL = new Region(); //creates a new region for the spacer
        HBox.setHgrow(spacerL, Priority.ALWAYS);
        titleBox.setStyle("-fx-padding: 10px;"); // Set padding and alignment
        titleBox.setAlignment(Pos.CENTER); //sets the alignment of the hbox
        
        titleBox.getChildren().addAll(spacerR,setTittletext,spacerL,deleteSetButton); //adds the text to the title hbox
        mainCenterBox.getChildren().add(titleBox); //adds the text to the center vbox
        root.setCenter(mainCenterBox); //sets the center of the border pane to the set center vbox
        createSetGUIHelper1(); //calls the create set GUI helper method
    }

    //creates the add new part GUI
    public void createSetGUIHelper1() {
        VBox addNewPartBoxHolder = new VBox(); //creates a new vbox for the add new part
        HBox addNewPartBox = new HBox(5); //creates a new hbox for the add new part
        HBox addNewPartBoxDisplayText = new HBox(); //creates a new hbox for the add new part
        Button addNewPartButton = new Button("Add New Part"); //button to add a new part
        Text partNumberText = new Text("Part Number"); //text for the part number
        TextField partNumberField = new TextField(); //text field for the part number  
        Text ammountMissingText = new Text("Ammount Missing"); //text for the ammount missing
        TextField ammountMissingField = new TextField(); //text field for the ammount missing   
        // Use a filter to allow only digits to be entered
        ammountMissingField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            // If the typed character is not a digit, consume the event
            if (!Character.isDigit(event.getCharacter().charAt(0))) {
                event.consume();
            }
        });
        // Use a filter to allow only digits and letters to be entered
        partNumberField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            char typedChar = event.getCharacter().charAt(0);
            if (!Character.isLetterOrDigit(typedChar)) {
                event.consume();
            }
        });
            
        ComboBox<LegoColor> colorComboBox = new ComboBox<>();
        ObservableList<LegoColor> originalItems = FXCollections.observableArrayList(LegoColorData.getAllColors()); // your original list
        FilteredList<LegoColor> filteredItems = new FilteredList<>(originalItems, p -> true);
        colorComboBox.setItems(filteredItems);

        // Flag to prevent re-entry loop
        BooleanProperty programmaticChange = new SimpleBooleanProperty(false);

        // Set how the items are shown
        colorComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(LegoColor object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public LegoColor fromString(String string) {
                return null; // Not needed
            }
        });

        // Custom editor for filtering
        colorComboBox.setEditable(true);
        TextField editor = colorComboBox.getEditor();

        // Listen for changes in text to apply filtering
        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (programmaticChange.get()) return; // Skip if change was programmatic

            filteredItems.setPredicate(item -> {
                if (newText == null || newText.isEmpty()) return true;
                return item.getName().toLowerCase().contains(newText.toLowerCase());
            });

            if (!filteredItems.isEmpty()) {
                colorComboBox.show();
            }
        });

        // Prevent value setting from triggering filter again
        colorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !programmaticChange.get()) {
                programmaticChange.set(true);
                editor.setText(newVal.getName()); // Will not trigger filtering due to flag
                selectedColor = LegoColorData.getColorByName(editor.getText());
                programmaticChange.set(false);
            }
        });


        //style for the set, adding new part boxes
        VBox numberBox = new VBox(); //vbox for the part number fields
        partNumberText.setId("scroll-entry");
        numberBox.getChildren().addAll(partNumberText,partNumberField);
        numberBox.setAlignment(Pos.CENTER);
        VBox partMissingBox = new VBox();
        ammountMissingText.setId("scroll-entry");
        partMissingBox.getChildren().addAll(ammountMissingText,ammountMissingField);
        partMissingBox.setAlignment(Pos.CENTER);
        VBox colorBox = new VBox();
        Text colorSelectText = new Text("Select Color");
        colorSelectText.setId("scroll-entry");
        colorBox.getChildren().addAll(colorSelectText,colorComboBox);
        colorBox.setAlignment(Pos.CENTER);

        //creates spacers to center the input fields when adding a new item
        Region spacerR = new Region(); //creates a new region for the spacer
        HBox.setHgrow(spacerR, Priority.ALWAYS);
        Region spacerL = new Region(); //creates a new region for the spacer
        HBox.setHgrow(spacerL, Priority.ALWAYS);

        //adds elements to the new part box
        addNewPartBox.setAlignment(Pos.CENTER);
        addNewPartBox.getChildren().addAll(spacerL,numberBox,partMissingBox,colorBox,spacerR,addNewPartButton); //adds all the elements to the add new part hbox
        
        //creates a wrapper to add the yellow underline
        addNewPartBox.setPadding(new Insets(0, 0, 5, 0)); // 10 pixels bottom padding
        VBox newPartBoxWrapper = new VBox();
        HBox yellowUnderlineBox = new HBox();
        yellowUnderlineBox.setId("yellow-underline");
        newPartBoxWrapper.getChildren().addAll(addNewPartBox, yellowUnderlineBox);

        //creates the error text when a part information is miss inputed
        Text addNewPartInfomationText = new Text(""); //text for the add new part
        HBox errorBox = new HBox();
        errorBox.setPadding(new Insets(2,0,2,0));
        errorBox.setAlignment(Pos.CENTER_LEFT);
        errorBox.getChildren().add(addNewPartBoxDisplayText);
        addNewPartBoxDisplayText.setId("error-font");
    
        addNewPartBoxDisplayText.getChildren().add(addNewPartInfomationText); //adds the text to the add new part hbox
        addNewPartBoxHolder.getChildren().addAll(newPartBoxWrapper, errorBox); //adds the add new part hbox to the add new part vbox
        mainCenterBox.getChildren().addAll(addNewPartBoxHolder, centerScrollPane); //adds the add new part vbox to the center vbox
        addNewPartButton.setOnAction(e -> {
            String partNumber = partNumberField.getText();
            String amountMissing = ammountMissingField.getText();

            // Validate part number
            if (partNumber == null || partNumber.isEmpty()) {
                addNewPartInfomationText.setFill(Color.RED);
                addNewPartInfomationText.setText("Please enter a valid part number!");
                return;
            }

            // Check if part already exists
            if (activeLegoSet.checkForPart(partNumber, selectedColor.getName())) {
                addNewPartInfomationText.setFill(Color.RED);
                addNewPartInfomationText.setText("Part already exists, please use the edit button!");
                return;
            }

            // Validate missing amount
            if (amountMissing == null || amountMissing.isEmpty()) {
                addNewPartInfomationText.setFill(Color.RED);
                addNewPartInfomationText.setText("Please enter a valid missing amount!");
                return;
            }

            // Validate selected color
            if (selectedColor == null) {
                addNewPartInfomationText.setFill(Color.RED);
                addNewPartInfomationText.setText("Please select a valid color");
                return;
            }

            // Everything valid, proceed to add
            try {
                int missingAmount = Integer.parseInt(amountMissing);
                activeLegoSet.addPart(partNumber, missingAmount, selectedColor.getCode(), selectedColor.getName());
                updateScrollPane();
                saveLegoSets(legoSets);

                addNewPartInfomationText.setFill(Color.GREEN);
                addNewPartInfomationText.setText("Part successfully added!");
            } catch (NumberFormatException ex) {
                addNewPartInfomationText.setFill(Color.RED);
                addNewPartInfomationText.setText("Missing amount must be a number!");
            }
        });

    }

    //updates the scroll pane for the missing parts
    public void updateScrollPane() {

        centerScrollPane.setContent(null); //clears the scroll pane
        
        VBox scrollBox = new VBox(); //creates a new vbox for the scroll pane

        int x = 0; //used to set the backgrounds of each entry
        for (LegoPart legoPart : activeLegoSet.getMissingParts()) { //loops through every part in the list
            if (legoPart != null) { //checks if the part is not null
                HBox scrollBoxEntry = new HBox(5); //creates a new hbox for the scroll pane
                scrollBoxEntry.setPadding(new Insets(10,10,5,10)); //sets the padding around the part list entrys
                //set the background colors of entrys based on x value
                if (x % 2 == 0){
                    scrollBoxEntry.setId("even-row");
                } else {
                    scrollBoxEntry.setId("odd-row");
                }


                scrollBoxEntry.setAlignment(Pos.CENTER_LEFT); //sets the alignment of the hbox
                ImageView partImage = new ImageView(legoPart.getPartImage(legoPart.getPartNumber(),legoPart.getColorName())); //creates a new image view for the part image
                partImage.setFitWidth(50); //sets the width of the image
                partImage.setPreserveRatio(true); //preserves the ratio of the image
                partImage.setOnMouseClicked(e -> {
                    // Add your action here when the image is clicked
                    Stage imageStage = new Stage(); //creates a new stage for the image
                    imageStage.setTitle("Part: " + legoPart.getPartNumber() + " Image."); //sets the title of the stage
                    imageStage.initModality(Modality.APPLICATION_MODAL); //sets the modality of the stage
                    imageStage.initStyle(StageStyle.UTILITY); //sets the style of the stage

                    ImageView imageView = new ImageView(legoPart.getPartImage(legoPart.getPartNumber(),legoPart.getColorName())); //creates a new image view for the part image
                    imageView.setFitWidth(300); //sets the width of the image
                    imageView.setPreserveRatio(true); //preserves the ratio of the image

                    StackPane popupImage = new StackPane(imageView); //creates a new stack pane for the image
                    imageStage.setScene(new Scene(popupImage, 300, 300)); //sets the scene of the stage
                    imageStage.show(); //shows the stage
                });
                Region spacer = new Region(); //creates a new region for the spacer
                HBox.setHgrow(spacer, Priority.ALWAYS); //sets the hgrow for the spacer
                Text partText = new Text("Part #" + legoPart.getPartNumber() + " " + legoPart.getColorName() ); //creates a text for the part
                partText.setId("scroll-entry");

                Text partAmountText = new Text("Missing: " + legoPart.getAmmountMissing());
                partAmountText.setId("scroll-entry");

                Button editButton = new Button("Edit"); //creates an edit button
                editButton.setOnAction(e -> {
                    editPart(legoPart); //calls the edit part method
                });

                //delete part button
                Button deletePartButton = new Button("Delete");
                deletePartButton.setOnAction(e -> {
                    activeLegoSet.deletePart(legoPart.getPartNumber(), legoPart.getColorName());
                    updateScrollPane();
                });


                scrollBoxEntry.getChildren().addAll(partImage,partText,spacer,partAmountText,deletePartButton,editButton); //adds the text to the hbox
                scrollBox.getChildren().add(scrollBoxEntry); //adds the hbox to the vbox

                x++; //increases the entry counter
            }
        }

        centerScrollPane.setContent(scrollBox); //sets the content of the scroll pane to the vbox
        centerScrollPane.setFitToHeight(true); //sets the scroll pane to fit to height
        centerScrollPane.setFitToWidth(true); //sets the scroll pane to fit to width    
    }

    public void editPart(LegoPart legoPart) {
        // Create a new stage for editing the part
        Stage editStage = new Stage();
        editStage.setTitle("Edit Part #" + legoPart.getPartNumber());
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.initStyle(StageStyle.UTILITY);

        VBox editBox = new VBox(); // Create a VBox for the edit form
        

        // Create a title for the edit form
        HBox titleBox = new HBox(); // Create a HBox for the title
        titleBox.setAlignment(Pos.CENTER);
        Text titleText = new Text("Edit Part #" + legoPart.getPartNumber());
        titleText.setId("scroll-title");
        titleBox.getChildren().add(titleText); // Add the title text to the HBox
        editBox.getChildren().add(titleBox); // Add the title HBox to the VBox

        // Create a TextField for the part number and amount missing before edit
        HBox partInformationBeforeEditBox = new HBox(); // Create a HBox for the part information
        partInformationBeforeEditBox.setAlignment(Pos.CENTER);
        Text ammountMissingText = new Text("Amount Missing Before Edit: " + String.valueOf(legoPart.getAmmountMissing())); //text for the ammount missing
        ammountMissingText.setId("scroll-entry");
        partInformationBeforeEditBox.getChildren().add(ammountMissingText); // Add the part information to the HBox
        editBox.getChildren().add(partInformationBeforeEditBox); // Add the part information HBox to the VBox

        //Update part missing amount
        HBox editAmountMissingBox = new HBox(10); // Create a HBox for the edit amount missing
        editAmountMissingBox.setAlignment(Pos.CENTER);
        Text ammountMissingLabel = new Text("New Amount Missing:"); //text for the ammount missing
        ammountMissingLabel.setId("scroll-entry");
        TextField ammountMissingField = new TextField(String.valueOf(legoPart.getAmmountMissing()));
        ammountMissingField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!Character.isDigit(event.getCharacter().charAt(0))) {
                event.consume();
            }
        });
        editAmountMissingBox.getChildren().addAll(ammountMissingLabel, ammountMissingField); // Add the edit amount missing to the HBox
        editBox.getChildren().add(editAmountMissingBox); // Add the edit amount missing HBox to the VBox

        //creates a save and cancel button
        // Create a Button to save changes to the part list
        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            if (ammountMissingField.getText().isEmpty()) { //checks if the text field is empty  
                // Set the hint text (prompt text)
                ammountMissingField.setPromptText("Enter a valid missing amount");
                // makes the text red 
                ammountMissingField.setStyle("-fx-prompt-text-fill: red;");
            } else {
                int newAmmountMissing = Integer.parseInt(ammountMissingField.getText());
                legoPart.setAmmountMissing(newAmmountMissing);
                    updateScrollPane(); // Update the scroll pane after editing
            editStage.close(); // Close the edit stage
            }  
        });
        // Create a Button to cancel the edit
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            editStage.close(); // Close the edit stage
        });
        HBox buttonBox = new HBox(); // Create a HBox for the buttons
        buttonBox.setStyle("-fx-alignment: center; -fx-padding: 10px;"); // Set padding and alignment   
        buttonBox.setSpacing(10); // Set spacing between elements
        buttonBox.getChildren().addAll(saveButton, cancelButton); // Add the buttons to the HBox
        editBox.getChildren().add(buttonBox); // Add the button HBox to the VBox

        // Create a Scene and set it to the Stage
        Scene scene = new Scene(editBox, 350, 175);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        editStage.setScene(scene);
        editStage.show();
    }
    
    // Create the main dashboard layout
    public void createMainDashBoard() {
        //clears the center vbox and gets it ready for the main dashboard
        mainCenterBox.getChildren().clear(); // Clear the center box

        //create a section for the total number of parts and which sets contain them
        Text titleText2 = new Text("Complete Part Overview"); //text for the total number of parts
        titleText2.setId("page-title");
        HBox titleBox2 = new HBox(); //creates a new hbox for the title
        titleBox2.setAlignment(Pos.CENTER_LEFT);
        titleBox2.getChildren().add(titleText2); //adds the text to the title hbox
        titleBox2.setPadding(new Insets(0,0,4,0));
        mainCenterBox.getChildren().add(titleBox2); //adds the title hbox to the center vbox

        //creating a scrollpane for the complete part overview
        completePartOverviewScrollPane.setContent(null); //clears the scroll pane
        
        updateMainDashboardPartList();
        loadMainDashboardPartList();

        root.setCenter(mainCenterBox);
        root.setRight(rightUISpacer()); //sets the right side of the UI to a spacer
        root.setBottom(bottomUISpacer()); //sets the bottom of the UI to a spacer
    }

    //little spacer method for the right side of the UI
    public Region rightUISpacer(){
        Region spacer = new Region();
        spacer.setMinWidth(30); //sets the minimum width of the spacer
        return spacer; //returns the spacer
    }
    
    //little spacer method for the bottom  of the UI
    public Region bottomUISpacer(){
        Region spacer = new Region();
        spacer.setMinHeight(30); //sets the minimum width of the spacer
        return spacer; //returns the spacer
    }

    //checks if the set number is already in the list
    public boolean checkForCompletePart(String partNumber, String partColorName) {
        for (LegoCompletePart completePart : completeParts) { //loops through every set in the list
            if (completePart != null && completePart.getPartNumber().equals(partNumber) && completePart.getColorName().equals(partColorName)) { //checks if the set number is already in the list
                return true; //returns true if found
            }
        }
        return false; //returns false if not found
    }
    
    //adds a new complete part to the list
    public void addCompletePart(String partNumber, String partColorName, int partColorCode) {
        listCompleteSizeCheck(currentCompletePartIndex); //checks the list size and expands it if needed
        if (checkForCompletePart(partNumber, partColorName) == false ) { //checks if the set number is already in the list
            LegoCompletePart completePart = new LegoCompletePart(partNumber, partColorName, partColorCode); //creates a new complete part
            completeParts[currentCompletePartIndex] = completePart; //adds the set to the list
        } 
    }

    //checks the list size and expands it if needed for the complete part list
    public void listCompleteSizeCheck(int currentCompletePartIndex) {
        if (currentCompletePartIndex == completeParts.length) { //checks if the current index is equal to the length of the list
            int newSize = completeParts.length * 2; //expands the current size by x2
            LegoCompletePart[] temp = new LegoCompletePart[newSize]; //temp set array to hold all old entries
            for (int i = 0; i < completeParts.length; i++) { //loops through every set in the list
                temp[i] = completeParts[i]; //moves all entries over to the temp
            }
            completeParts = temp; //changes the pointer for lego sets to temp
        }
    }
    
    //loads the complete part overview into the scroll pane   //topText.setId("page-title");
    public void loadMainDashboardPartList(){
        VBox scrollContent = new VBox();

        for (int i = 0; i < currentCompletePartIndex; i++) {
            LegoCompletePart part = completeParts[i];

            // Header: part number and total missing
            HBox headerBox = new HBox();
            headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            headerBox.setSpacing(10);
            headerBox.setPadding(new Insets(10,0,0,10));
            Text partHeader = new Text("Part #" + part.getPartNumber() + " "+ part.getColorName()); //text for part number and color
            partHeader.setId("scroll-title"); //styling for text above
            
            int amount = part.getTotalAmmountMissing();
            String partMissingAmount = String.format("%-4s", amount);
            Text partTotalHeader = new Text("Missing: " + partMissingAmount); //text for the number of parts missing

            partTotalHeader.setId("scroll-title");
            Region spacerM = new Region(); //creates a new region for the spacer
            HBox.setHgrow(spacerM, Priority.ALWAYS);
            ImageView partImage = new ImageView(part.getPartImage()); //creates a new image view for the part image
                partImage.setFitWidth(50); //sets the width of the image
                partImage.setPreserveRatio(true); //preserves the ratio of the image
                partImage.setOnMouseClicked(e -> {
                    // Add your action here when the image is clicked
                    Stage imageStage = new Stage(); //creates a new stage for the image
                    imageStage.setTitle("Part: " + part.getPartNumber() + " Image."); //sets the title of the stage
                    imageStage.initModality(Modality.APPLICATION_MODAL); //sets the modality of the stage
                    imageStage.initStyle(StageStyle.UTILITY); //sets the style of the stage

                    ImageView imageView = new ImageView(part.getPartImage()); //creates a new image view for the part image
                    imageView.setFitWidth(300); //sets the width of the image
                    imageView.setPreserveRatio(true); //preserves the ratio of the image

                    StackPane popupImage = new StackPane(imageView); //creates a new stack pane for the image
                    imageStage.setScene(new Scene(popupImage, 300, 300)); //sets the scene of the stage
                    imageStage.show(); //shows the stage
                });
            headerBox.getChildren().addAll(partImage, partHeader, spacerM, partTotalHeader);
            scrollContent.getChildren().add(headerBox);

            // Add entries for each set that needs this part
            int[] setNumbers = part.getSetNumbers();
            int[] amounts = part.getMissingParts();

            for (int j = 0; j < setNumbers.length; j++) {
                if (setNumbers[j] != 0 && amounts[j] != 0) { // skip uninitialized entries
                    Text entry = new Text("- Set #" + setNumbers[j] + " needs " + amounts[j]);
                    entry.setId("scroll-entry");
                    HBox entryBox = new HBox();
                    Region spacer = new Region();
                    spacer.setMinWidth(75);
                    entryBox.getChildren().addAll(spacer, entry);

                    if (i % 2 == 0) {
                        headerBox.setId("even-row"); // Set ID for even rows
                        entryBox.setId("even-row"); // Set ID for even rows
                    } else {
                        headerBox.setId("odd-row"); // Set ID for odd rows
                        entryBox.setId("odd-row"); // Set ID for odd rows
                    }

                    scrollContent.getChildren().add(entryBox);
                }
            }
        }

        completePartOverviewScrollPane.setContent(scrollContent); // Set the content of the scroll pane
        completePartOverviewScrollPane.setFitToWidth(true); // Set the scroll pane to fit to width

        //add the scroll pane to the main center box
         mainCenterBox.getChildren().add(completePartOverviewScrollPane);
    }

    public void updateMainDashboardPartList() {
    currentCompletePartIndex = 0; // Reset index for rebuilding the complete parts list

    // Clear all previous complete part data
    for (int i = 0; i < completeParts.length; i++) {
        if (completeParts[i] != null) {
            completeParts[i] = null; // completely remove old entry to reset
        }
    }

    for (LegoSet legoSet : legoSets) {
        if (legoSet != null) {
            for (LegoPart legoPart : legoSet.getMissingParts()) {
                if (legoPart != null) {
                    String partNumber = legoPart.getPartNumber();
                    int amountMissing = legoPart.getAmmountMissing();
                    int setNumber = legoSet.getSetNumber();
                    Image partImage = legoPart.getPartImage(legoPart.getPartNumber(),legoPart.getColorName());
                    String partColorName = legoPart.getColorName();
                    int partColorCode = legoPart.getColorCode();

                    // Check if this part already exists in the completeParts list
                    LegoCompletePart existingPart = null;
                    for (int i = 0; i < currentCompletePartIndex; i++) {
                        if (completeParts[i] != null && completeParts[i].getPartNumber().equals(partNumber) && completeParts[i].getColorName().equalsIgnoreCase(partColorName)) {
                            existingPart = completeParts[i];
                            break;
                        }
                    }

                    if (existingPart != null) {
                        // Part already exists, just update its data
                        existingPart.addSet(setNumber, amountMissing);
                        existingPart.updateTotalAmmountMissing(amountMissing);
                        existingPart.setPartImage(partImage); // Update the part image
                    } else {
                        // Part not found, create a new LegoCompletePart
                        addCompletePart(partNumber, partColorName, partColorCode); // make sure this adds to completeParts[currentCompletePartIndex]
                        completeParts[currentCompletePartIndex].addSet(setNumber, amountMissing);
                        completeParts[currentCompletePartIndex].updateTotalAmmountMissing(amountMissing);
                        completeParts[currentCompletePartIndex].setPartImage(partImage); // Set the part image
                        completeParts[currentCompletePartIndex].setColorName(partColorName);
                        currentCompletePartIndex++;
                    }
                }
            }
        }
    }
}

public void generateXML(){
   DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        
        // Root element is INVENTORY
        Element rootElement = doc.createElement("INVENTORY");
        doc.appendChild(rootElement);

        // Loop through all LegoParts to generate ITEM elements
        for (LegoCompletePart legoCompletePart : completeParts) { 
            if (legoCompletePart != null) {
        
                // Create ITEM element
                Element itemElement = doc.createElement("ITEM");
                rootElement.appendChild(itemElement);

                // Add ITEMTYPE for "part"
                Element itemTypeElement = doc.createElement("ITEMTYPE");
                itemTypeElement.appendChild(doc.createTextNode("P"));  // "P" stands for Part
                itemElement.appendChild(itemTypeElement);

                // Add ITEMID (part number)
                Element itemIdElement = doc.createElement("ITEMID");
                itemIdElement.appendChild(doc.createTextNode(String.valueOf(legoCompletePart.getPartNumber())));
                itemElement.appendChild(itemIdElement);

                // Add COLOR (color code)
                Element itemColorElement = doc.createElement("COLOR");
                itemColorElement.appendChild(doc.createTextNode(String.valueOf(legoCompletePart.getColorCode())));
                itemElement.appendChild(itemColorElement);

                // Add QTYFILLED (quantity)
                Element qtyElement = doc.createElement("MINQTY");
                qtyElement.appendChild(doc.createTextNode(String.valueOf(legoCompletePart.getTotalAmmountMissing())));  // Assuming you use "ammountMissing" for quantity
                itemElement.appendChild(qtyElement);
            }
        }
        
        // Get the user's home directory
        String userHome = System.getProperty("user.home");
        
        // Define the path to the Downloads folder on Windows (C:\Users\<username>\Downloads)
        String downloadsFolder = userHome + File.separator + "Downloads" + File.separator + "BrickLink_WantedList.xml";

        // Set up the transformer and output the XML to the Downloads folder
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(downloadsFolder));
        transformer.transform(source, result);

        System.out.println("XML file has been generated at: " + downloadsFolder);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    //loads the lego sets from a .dat file in home directory
     public static LegoSet[] loadLegoSets() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            return (LegoSet[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No saved sets found or error occurred.");
            return new LegoSet[0]; // Return empty array on failure
        }
    }

    //saves the lego sets to a .dat file in home directory
     public static void saveLegoSets(LegoSet[] sets) {
        try (FileOutputStream fos = new FileOutputStream(FILE_PATH);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(sets);
            System.out.println("Lego sets saved to " + FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}