package ca.thepopbox;

import javafx.scene.image.Image;

public class LegoCompletePart {
    private int[] setNumbers; //array of part numbers
    private int[] ammountMissing; //array of ammounts missing
    final private String partNumber; //array of part numbers
    private int totalAmmountMissing = 0; //total ammount of missing parts
    private int currentListIndex = 0; //pointer to the current open slot in the list
    private int partListSize = 5; //the max size of the part list
    private Image partImage; //image of the part
    private String partColor = "";
    private int partColorCode = 99999999;

    //constructor
    public LegoCompletePart(String partNumber, String partColorname, int partColorCode) {
        this.partNumber = partNumber;
        this.partColor = partColorname; //sets the part color name
        this.partColorCode = partColorCode; //sets the part color code
        this.ammountMissing = new int[partListSize];
        this.setNumbers = new int[partListSize];
    }

    //set colorcode
    public void setColorCode(int colorCode) {
        this.partColorCode = colorCode; //sets the part color code
    }

    //get color code
    public int getColorCode() {
        return partColorCode; //returns the part color code
    }

    //set color name
    public void setColorName(String colorName) {
        this.partColor = colorName; //sets the part color name
    }

    //get color name 
    public String getColorName() {
        return partColor; //returns the part color name
    }

    //get part image
    public Image getPartImage(){
        return partImage; //returns the part image
    }

    //set part image
    public void setPartImage(Image partImage){
        this.partImage = partImage; //sets the part image

    }

    //check for part in list
    public boolean checkForSet(int setNumber) {
        for (int i = 0; i < currentListIndex; i++) {
            if (setNumbers[i] == setNumber) { //checks if the set is already in the list
                return true; //part is already in the list
            }
        }
        return false; //part is not in the list
    }

    //get the total ammount of missing parts
    public int getTotalAmmountMissing() {
        return totalAmmountMissing; //returns the total ammount of missing parts
    }

    //set the total ammount of missing parts
    public void updateTotalAmmountMissing(int ammountMissing) {
        this.totalAmmountMissing += ammountMissing; //adds the ammount of missing parts to the total
    }

    //checks the list size and expands it, if needed
    private void listSizeCheck(int currentListIndex) {
        if (currentListIndex == partListSize) {
            partListSize = partListSize * 2; //expand the current size by x2
            int[] tempSetNumbers = new int[partListSize]; //temp part array to hold all old entries
            int[] tempAmmountMissing = new int[partListSize]; //temp part array to hold all old entries
            for (int i = 0; i < setNumbers.length; i++) {
                tempSetNumbers[i] = setNumbers[i]; //move all entries over to the temp
                tempAmmountMissing[i] = ammountMissing[i]; //move all entries over to the temp
            }
            setNumbers = tempSetNumbers; //change the pointer for missing parts to temp
            ammountMissing = tempAmmountMissing; //change the pointer for missing parts to temp
        }
    }

    //add new part into the missing parts list
    public void addSet(int setNumber, int ammountMissing) {
        if (checkForSet(setNumber) == false) { //calls checkforpart method to check if the set is already in the list of missing parts
            listSizeCheck(currentListIndex); //checks list to make sure there is room to add a new part
            setNumbers[currentListIndex] = setNumber; // adds the part to the list
            this.ammountMissing[currentListIndex] = ammountMissing; //adds the ammount of missing parts to the list
            currentListIndex++; //increases the current index
        }
    }

    //get the part number
    public String getPartNumber() {
        return partNumber;
    }

    //returns the list of missing parts
    public int[] getMissingParts() {
        return ammountMissing;
    }

    //returns the list of set numbers
    public int[] getSetNumbers() {
        return setNumbers;
    }

}
