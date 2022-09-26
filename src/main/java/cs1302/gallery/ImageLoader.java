package cs1302.gallery;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;

/**
 * An ImageLoader will load and display an image given a proper URL.
 */
public class ImageLoader extends VBox {
    /** A default image which loads when the application starts. */
    private static final String DEFAULT_IMG =
        "http://cobweb.cs.uga.edu/~mec/cs1302/gui/default.png";

    /** Default height and width for Images. */
    private static final int DEF_HEIGHT = 100;
    private static final int DEF_WIDTH = 100;

    /** The container for the loaded image. */
    ImageView imgView;

    /**
     * Constructs an ImageLoader object.
     */
    public ImageLoader() {
        super();

        // Load the default image with the default dimensions
        Image img = new Image(DEFAULT_IMG, DEF_HEIGHT, DEF_WIDTH, false, false);

        // Add the image to its container and preserve the aspect ratio if resized
        imgView = new ImageView(img);
        imgView.setPreserveRatio(true);

        // Add the hbox and imageview to the containing vbox and set the vbox
        // to be the root of the scene
        this.getChildren().add(imgView);
    } // ImageLoader

    /**
     * Will load the image based on the given URL.
     *
     * @param url the url of the image to be loaded
     */

    public void loadImage(String url) {

        try {
            Image newImg = new Image(url, DEF_HEIGHT, DEF_WIDTH, false, false);
            imgView.setImage(newImg);
        } catch (IllegalArgumentException iae) {
            System.out.println("The supplied URL is invalid");
        } // try

    } // loadImage

} // ImageLoader
