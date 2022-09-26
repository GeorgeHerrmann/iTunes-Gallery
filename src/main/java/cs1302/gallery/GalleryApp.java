package cs1302.gallery;

import javafx.scene.control.ProgressBar;
import java.util.Random;
import javafx.stage.Modality;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import java.time.LocalTime;
import javafx.util.Duration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.text.Text;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import java.net.URL;
import java.net.URLEncoder;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.io.IOException;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;


/**
 * A GalleryApp pulls iTunes images for music based on the searched query.
 */
public class GalleryApp extends Application {

    String userInput = "rap"; //"Rap" will be the default query
    ImageLoader[] loaders = new ImageLoader[20]; //The 20 images are represented by these "loaders"
    String encodedString = null;
    ProgressBar progress = new ProgressBar();
    GridPane pane = new GridPane();
    BorderPane border = new BorderPane();
    Timeline timeline = new Timeline();
    JsonArray results = null; //Holds the results from the iTunes query
    JsonArray resultsDisplayed = new JsonArray(); //Holds the displayed results
    MenuBar file = new MenuBar();
    Menu menu1 = new Menu("File");
    Menu menu2 = new Menu("Help");
    MenuItem aboutMe = new MenuItem("About");
    TextField searchField = new TextField(userInput);

    /**
     * Starts the JavaFX program.
     *
     * <p>
     *  {@inheritdoc}
     */
    @Override
    public void start(Stage stage) {
        int col = 0, row = 0;
        for (int i = 0; i < loaders.length; i++) { //Adds the loaders (which hold the images
            loaders[i] = new ImageLoader();
            pane.add(loaders[i], row, col); //Each loader is added to the GridPane
            row++;
            if (row == 5) {
                row = 0;
                col++;
            }
        }
        Scene scene = new Scene(border);
        VBox vbox = new VBox(); //The VBox is the top portion of the GUI
        HBox hbox = new HBox(); //The HBox is the bottom portion of the GUI
        Button imageUpdate = new Button("Update Images");
        Button pause = new Button("Pause");
        Label query = new Label("Search Query:");
        Text courtesy = new Text ("Images provided courtesy of iTunes");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> Platform.exit()); //Sets the action of the exit button
        menu1.getItems().add(exit);
        menu2.getItems().add(aboutMe);
        aboutMe();
        file.getMenus().addAll(menu1, menu2);
        ToolBar toolbar = new ToolBar(); //The Toolbar holds the buttons, search text and TextField
        toolbar.getItems().addAll(pause, query, searchField, imageUpdate);
        vbox.getChildren().addAll(file, toolbar);
        hbox.getChildren().addAll(progress, courtesy);
        border.setTop(vbox);
        border.setCenter(pane);
        border.setBottom(hbox);
        border.setBottom(hbox);
        stage.setMinWidth(500);
        stage.setMinHeight(515);
        stage.setMaxWidth(500);
        stage.setMaxHeight(515);
        stage.setTitle("Gallery!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        imageUpdate.setOnAction (new EventHandler<ActionEvent>() { //Sets the action of imageUpdate
                public void handle (ActionEvent event) {
                    userInput = searchField.getText();
                    runNow(() -> grabImages()); //Will grab then update the images on button press
                }
            });
        pause.setOnAction(new EventHandler<ActionEvent>() { //Sets the action of the pause button
            public void handle(ActionEvent event) {
                if (pause.getText().equals("Play")) { //Will pause timeline if unpaused
                    pause.setText("Pause");
                    timeline.play();
                } else if (pause.getText().equals("Pause")) { //Will unpause timeline if paused
                    pause.setText("Play");
                    timeline.pause();
                }
            }
        });
        runNow(() -> timelineBegin()); //Begins the timeline
        runNow(() -> grabImages()); //Begins the first query
    } // start

    /**
     * A private method that creates the AboutMe window.
     */
    private void aboutMe() {
        aboutMe.setOnAction(e -> {
            FlowPane flowPane3 = new FlowPane(); //This FlowPane holds the about me information
            Text info = new Text("George Herrmann\n gvh12771@uga.edu\n Version: 1.02 Alpha \n");
            Image me = new Image("https://bit.ly/3dwkX2D");
            ImageView meView = new ImageView(me);

            meView.setFitHeight(300.0);
            meView.setFitWidth(150.0);

            flowPane3.getChildren().add(info);
            flowPane3.getChildren().add(meView);

            Stage stage3 = new Stage();
            Scene scene3 = new Scene(flowPane3);

            stage3.setScene(scene3);
            stage3.initModality(Modality.APPLICATION_MODAL);
            stage3.setTitle("About George Herrmann");

            stage3.sizeToScene();
            stage3.showAndWait();
        }); //Creates the about window using a FlowPane
    } // aboutMe

    /**
     * Pulls the results of the query from iTunes and stores it as a jsonArray.
     */
    public void grabImages() {
        URL url = null;
        InputStreamReader reader = null;
        try {
            encodedString = URLEncoder.encode(userInput, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println(e.toString());
        } // try
        if (encodedString != null) {
            try {
                String baseUrl = "https://itunes.apple.com/search?term=";
                url = new URL(baseUrl + "" + encodedString + "&limit=50&media=music");
            } catch (java.net.MalformedURLException e) {
                System.err.println(e.toString());
            } // try
        } // if
        try {
            reader = new InputStreamReader(url.openStream());
        } catch (IOException e) {
            System.out.println(e.toString());
        } //try
        try {
            JsonElement je = JsonParser.parseReader(reader);
            JsonObject root = je.getAsJsonObject();
            JsonArray tempResults = root.getAsJsonArray("results"); // "results" array

            updateImages(userInput, tempResults); //updates the images based on the grabbed query
        } catch (NullPointerException npe) { //Occasionally the API can throw null when it shouldn't
            grabImages(); //This makes sure, if it does, the program will retry
        }
    }

    /**
     * Begins the timeline, allowing the program to swap images every 2 seconds.
     */
    private void timelineBegin() {
        EventHandler<ActionEvent> handler = event -> swapImages();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /**
     * Will update the images to the screen based on the
     * results from {@code grabImages()}.
     *
     * @param input the user's query
     * @param tempResults the JsonArray from {@code grabImages()}
     */
    private void updateImages(String input, JsonArray tempResults) {
        if (tempResults.size() > 20) { //Ensures the query size was over twenty
            results = tempResults; //Global variable "results" now reflects the grabbed results
            resultsDisplayed = new JsonArray(); //Resets the JsonArray of the displayed images
            for (int i = 0; i < 21; i++) {
                resultsDisplayed.add(results.get(i)); //Adds the displayed images to the array
            } //for

            setProgress(0);
            for (int i = 0; i < 20; i++) { //Updates the GridPane and ProgressBar
                JsonObject result = results.get(i).getAsJsonObject();
                JsonElement artworkUrl100 = result.get("artworkUrl100");
                if (artworkUrl100 != null) {
                    String imageUrl = artworkUrl100.getAsString();
                    loaders[i].loadImage(imageUrl);
                } // if
                setProgress(1.0 * i / 20);
            } // for
            setProgress(1);
        } else { //If the query had less than 20 results
            Platform.runLater(() -> {
                timeline.pause();
                FlowPane flowPane2 = new FlowPane(); //This flowpane holds the error text window
                Text info  = new Text("Search had < 20 results,\nexit this window and try again.");
                flowPane2.getChildren().add(info);

                Stage stage2 = new Stage();
                Scene scene2 = new Scene(flowPane2, 300, 100);
                stage2.setScene(scene2);
                stage2.initModality(Modality.APPLICATION_MODAL);
                stage2.setTitle("Search Error");
                stage2.sizeToScene();
                stage2.showAndWait();
                timeline.play();
            });
        } // else
    }

    /**
     * Swaps the images every two seconds based on the JsonArray.
     */
    public void swapImages() {
        if (results != null) {
            Random random = new Random();

            int imageInArray = random.nextInt(19); //Picks a random num for the displayed images

            int imageSpot = 21 + random.nextInt((results.size() - 22) + 1);

            /*
             * Checks if the displayed (20 images) results contain the
             * result at imageSpot in the full results array (50 images)
             */
            while (resultsDisplayed.contains(results.get(imageSpot))) {
                imageSpot = 21 + random.nextInt((results.size() - 22) + 1);
            } //while

            /*
             * Replaces the element in the displayed results array with the picked
             * element from the full results array
             */
            for (int i = 0; i < resultsDisplayed.size(); i++) {
                if (resultsDisplayed.get(i).equals(results.get(imageSpot))) {
                    resultsDisplayed.set(i, results.get(imageSpot));
                }
            }

            JsonObject result = results.get(imageSpot).getAsJsonObject();

            JsonElement artworkUrl100 = result.get("artworkUrl100");

            if (artworkUrl100 != null) {
                String artUrl = artworkUrl100.getAsString();
                loaders[imageInArray].loadImage(artUrl); //Then the image is updated
            }
        }
    }

    /**
     * Sets the progress of the ProgressBar.
     *
     * @param progressAmount how much progress has been made in loading images
     */
    private void setProgress(final double progressAmount) {
        Platform.runLater(() -> progress.setProgress(progressAmount));
    } // setProgress

    /**
     * Creates and immediately starts a new daemon thread that executes
     * {@code target.run()}. This method, which may be called from any thread,
     * will return immediately its the caller.
     * @param target the object whose {@code run} method is invoked when this
     *               thread is started
     */
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    } // runNow

} // GalleryApp
