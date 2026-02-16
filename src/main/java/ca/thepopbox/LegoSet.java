package ca.thepopbox;

import java.io.Serializable;

public class LegoSet implements Serializable{

    final private int setNumber; //number of the lego set
    private int partListSize = 5; //the max size of the part list
    private int currentListIndex = 0; //pointer to the current open slot in the list
    private LegoPart[] missingParts = new LegoPart[partListSize]; //the list where all the missing parts are stored

    //constructor
    public LegoSet(int setNumber){
        this.setNumber = setNumber;
    }

    //returns the list of missing parts
    public LegoPart[] getMissingParts(){
        return missingParts;
    }
    
    //return the set number
    public int getSetNumber(){
        return setNumber;
    }

    public void updatePartPhoto() {
    for (LegoPart part : missingParts) { // 'parts' is your list of parts in this set
        if (part != null) {
                part.updatePhoto();
            }
        }
    }

    //delete part method
    public void deletePart(String partNumber, String colorName) {
    for (int i = 0; i < currentListIndex; i++) {
        LegoPart part = missingParts[i];
        if (part != null && part.getPartNumber().equals(partNumber) && part.getColorName().equals(colorName)) {
            // Shift all elements left to fill the gap
            for (int j = i; j < currentListIndex - 1; j++) {
                missingParts[j] = missingParts[j + 1];
            }
            // Clear the now-unused slot
            missingParts[currentListIndex - 1] = null;
            currentListIndex--;
        }
    }
}

    //retuns a list of all the parts and their missing piece ammounts 
    public String toString(){
        String output = ""; //string base
        //loops through every lego part in the list
        for (LegoPart legoPart : missingParts) {
            if (legoPart != null){
                output += "Part #:" + legoPart.getPartNumber() + " "; //adds the part number to the output string
               output += "Amount: " + legoPart.getAmmountMissing() + "\n"; //adds the ammount of missing parts to the output string
            } else {
                break; //breaks the loop because there are no more parts
            }   
        }
        return output; //returns the output string
    }

    //adds a new part into the missing parts list
    public void addPart(String partNumber, int ammountMissing, int colorCode, String colorName){
        if (checkForPart(partNumber, colorName) == false){ //calls checkforpart method to check if the part is already in the list of missing parts
            LegoPart part = new LegoPart(partNumber, ammountMissing, colorCode, colorName); //create a new lego part
            listSizeCheck(currentListIndex); //checks list to make sure there is room to add a new part
            missingParts[currentListIndex] = part; // adds the part to the list
            currentListIndex++; //increases the current index
        }
    }

    //checks the list size and expands it, if needed
    private void listSizeCheck(int currentListIndex){
        if (currentListIndex == partListSize){
            partListSize = partListSize * 2; //expand the current size by x2
            LegoPart[] temp = new LegoPart[partListSize]; //temp part array to hold all old entries
            for (int i = 0; i < missingParts.length; i++) {
                temp[i]=missingParts[i]; //move all entries over to the temp
            }
            missingParts = temp; //change the pointer for missing parts to temp
        }
    }

    //Checks every part in the missing part list and returns true if found
    public boolean checkForPart(String partNumber, String colorName){
        for (LegoPart legoPart : missingParts) {
            if (legoPart != null && legoPart.getPartNumber().equals(partNumber) && legoPart.getColorName().equals(colorName)){
                return true;
            }
        }
        return false;
    }

}