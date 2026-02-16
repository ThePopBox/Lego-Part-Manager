package ca.thepopbox;

import java.io.File;
import java.io.Serializable;
import java.net.URL; 

import javafx.scene.image.Image;

public class LegoPart implements Serializable {

    private String partNumber;
    private int ammountMissing;
    private transient Image partImage; // Make it transient to avoid serialization
    private String imagePath;  // Save the image path for later reloading
    private String colorName; // Color name of the part
    private int colorCode; // Color code of the part

    public LegoPart(String numberOfPart, int missingAmount, int colorCode, String colorName) {
        partNumber = numberOfPart;
        ammountMissing = missingAmount;
        this.colorCode = colorCode;
        this.colorName = colorName;
        setPartImage(partNumber, colorName); // Set the part image based on the part number
    }

    //sets the color data for the part from a lego color object
    public void setColorData(LegoColor color) {
        this.colorName = color.getName();
        this.colorCode = color.getCode();
    }

    //returns the color name of the part
    public String getColorName() {
        return colorName;
    }
    //returns the color code of the part
    public int getColorCode() {
        return colorCode;
    }

    private void setPartImage(String partNumber, String partColor) {
        // Define the directory where images are stored
        String userHome = System.getProperty("user.home");
        File imageDir = new File(userHome, "partmanagerimages");

        // Construct the image file name based on partNumber and partColor
        String imageName = partNumber + "-" + partColor.toLowerCase() + ".png";
        File imageFile = new File(imageDir, imageName); // Check if the file exists

        if (imageFile.exists()) {
            // If the image exists, create an Image from the file
            this.partImage = new Image(imageFile.toURI().toString());
        } else {
            // If image not found, use a fallback/default image
            String fallbackPath = "/images/unknownpart.png"; // Fallback image in resources
            URL fallbackUrl = getClass().getResource(fallbackPath);

            if (fallbackUrl != null) {
                this.partImage = new Image(fallbackUrl.toString());
            } else {
                this.partImage = null; // If fallback is not found, set it to null
            }
        }
    }

    //updates the photo
    public void updatePhoto() {
    // Refresh the part image by resetting it using the current part number and color
    setPartImage(this.partNumber, this.colorName);
    }

    public Image getPartImage(String partNumber, String partColor) {
        // If partImage is not loaded, reload it from the image file
        if (partImage == null) {
            setPartImage(partNumber, partColor); // Reload image using the correct partNumber and partColor
        }
        return partImage;
    }

    
    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public int getAmmountMissing() {
        return ammountMissing;
    }

    public void setAmmountMissing(int ammountMissing) {
        this.ammountMissing = ammountMissing;
    }
}
