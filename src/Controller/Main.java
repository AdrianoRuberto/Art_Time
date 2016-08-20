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

	public ListView<String> leftListView = new ListView<>();

	public List<Project> projects = Project.getProjects(p -> !p.isEnded());
	public Label timePassed = new Label("Select a project");
	public Tab tabMore;
	public Tab tabTimer;
	public GridPane gridTimeSpent;
	public Button cmdSessionStart = new Button();
	private int selectedProject = -1;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/View/main.fxml"));
		primaryStage.setTitle("Art Time");

		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		leftListView.setItems(FXCollections.observableArrayList(projects.stream()
		                                                                .map(Project::getName)
		                                                                .collect(Collectors.toList())));

		projects.forEach(p -> p.addObserver(this));

		leftListView.setOnMouseClicked(e -> select(leftListView.getSelectionModel().getSelectedIndex()));

		cmdSessionStart.setVisible(false);
	}

	private void select(int index) {
		if (index >= 0 && index < projects.size()) {
			selectedProject = index;
			cmdSessionStart.setVisible(true);
			timePassed.setText(projects.get(index).getTimeOn());
		} else {
			timePassed.setText("Select a project");
		}
	}

	public void session(ActionEvent event) {
		if(cmdSessionStart.isVisible()) {
			projects.get(selectedProject).startSession();
			cmdSessionStart.setVisible(false);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		timePassed.setText(((Project)o).getTimeOn());
	}
}
