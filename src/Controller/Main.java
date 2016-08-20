package Controller;

import Model.Project;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Main extends Application implements Initializable, Observer {

	public ListView<Project> currentListView = new ListView<>();
	public ListView<Project> endedListView = new ListView<>();

	public Label timePassed = new Label("Select a project");
	public Tab tabMore;
	public Tab tabTimer;
	public GridPane gridTimeSpent;
	public Button cmdSessionStart = new Button();

	private Project current;
	private boolean sessionInUse = false;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/View/main.fxml"));
		primaryStage.setTitle("Art Time");

		primaryStage.setScene(new Scene(root));
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			if (sessionInUse) {
				currentListView.getItems().forEach(Project::stopSession);
			}
			Project.saveAllProjects();
		});
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		List<Project> projects = Project.getProjects(p -> true);

		projects.forEach(p -> p.addObserver(this));
		currentListView.setItems(FXCollections.observableArrayList(projects.stream()
		                                                                   .filter(p -> !p.isEnded())
		                                                                   .collect(Collectors.toList())));

		endedListView.setItems(FXCollections.observableArrayList(projects.stream()
		                                                                 .filter(Project::isEnded)
		                                                                 .collect(Collectors.toList())));

		cmdSessionStart.setVisible(false);

		currentListView.getSelectionModel().selectedItemProperty().addListener((p, old, newValue) -> {
			cmdSessionStart.setVisible(true);
			if(sessionInUse) {
				session(null);
			}
			current = newValue;

			timePassed.setText(newValue.getTimeOn());
		});
	}


	public void session(ActionEvent event) {
		if (!sessionInUse) {
			current.startSession();
			cmdSessionStart.setText("Stop session");
		} else {
			current.stopSession();
			cmdSessionStart.setText("Start session");
		}

		sessionInUse = !sessionInUse;
	}

	@Override
	public void update(Observable o, Object arg) {
		timePassed.setText(((Project) o).getTimeOn());
	}
}
