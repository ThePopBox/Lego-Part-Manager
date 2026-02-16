package ca.thepopbox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ColorManager {
    // Create an ObservableList to hold color names
    private ObservableList<String> colorList;

    public ColorManager() {
        colorList = FXCollections.observableArrayList();
        loadColors();
    }

    // Method to get the list of colors
    public ObservableList<String> getColorList() {
        return colorList;
    }

    // Method to load colors into the list
    private void loadColors() {
        // Loop through all LegoColor objects and add their names to the list
        for (LegoColor color : LegoColorData.getAllColors()) {
            colorList.add(color.getName());
        }
    }
}
