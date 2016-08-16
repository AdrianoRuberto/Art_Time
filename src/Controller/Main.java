package Controller;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Application implements Initializable {

	public ListView<String> leftListView = new ListView<>();

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/View/main.fxml"));
		primaryStage.setTitle("Art Time");

		primaryStage.setScene(new Scene(root, 600, 300));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		ObservableList<String> names = FXCollections.observableArrayList(
				"Julia", "Ian", "Sue", "Matthew", "Valentine", "Stephan", "Denise");

		leftListView.setItems(names);
	}
}
