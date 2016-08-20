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

	public Label timePassed = new Label("Select a project");
	public Tab tabMore;
	public Tab tabTimer;
	public GridPane gridTimeSpent;
	public Button cmdSessionStart = new Button();

	private int selectedProject = -1;
	private boolean sessionInUse = false;
	private List<Project> projects = Project.getProjects(p -> !p.isEnded());

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/View/main.fxml"));
		primaryStage.setTitle("Art Time");

		primaryStage.setScene(new Scene(root));
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			if (sessionInUse) {
				projects.get(selectedProject).stopSession();
			}
			Project.saveAllProjects();
		});
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

		cmdSessionStart.setVisible(false);

		leftListView.setOnMouseClicked(e -> select(leftListView.getSelectionModel().getSelectedIndex()));
	}

	private void select(int index) {
		if (index < 0 || index >= projects.size()) {
			timePassed.setText("Select a project");
			cmdSessionStart.setVisible(false);
		} else if (index != selectedProject) {
			cmdSessionStart.setVisible(true);
			if (sessionInUse) {
				session(null);
			}
			timePassed.setText(projects.get(index).getTimeOn());

			selectedProject = index;
		}
	}

	public void session(ActionEvent event) {
		if (!sessionInUse) {
			projects.get(selectedProject).startSession();
			cmdSessionStart.setText("Stop session");
		} else {
			projects.get(selectedProject).stopSession();
			cmdSessionStart.setText("Start session");
		}

		sessionInUse = !sessionInUse;
	}

	@Override
	public void update(Observable o, Object arg) {
		timePassed.setText(((Project) o).getTimeOn());
	}
}
