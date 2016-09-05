package Controller;

import Model.Project;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.swing.*;
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
	public Button cmdSessionStart = new Button();

	public TextField FieldBegin;
	public TextField FieldEnd;
	public TextField FieldTimePassed;
	public TextField FieldRatioHourDay;

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
			if (sessionInUse) {
				session(null);
			}
			current = newValue;
			if (newValue != null) {
				timePassed.setText(newValue.getCurrentTimeOn());

				// More Tab
				FieldBegin.setText(newValue.getBegin());
				FieldEnd.setText(newValue.getEnd());
				FieldTimePassed.setText(newValue.getTotalTimePassed());
				FieldRatioHourDay.setText(newValue.getHourDayRatio());
			} else {
				timePassed.setText("Select a project");
				FieldBegin.setText("");
				FieldEnd.setText("");
				FieldTimePassed.setText("");
				FieldRatioHourDay.setText("");
				cmdSessionStart.setVisible(false);
			}
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

	public void newProject(ActionEvent event) {
		Project p = Project.addProject(JOptionPane.showInputDialog("Enter the name of the project"));
		currentListView.getItems().add(p);
		p.addObserver(this);
	}

	public void deleteProject(ActionEvent event) {
		if (current != null) {
			if (sessionInUse)
				current.stopSession();
			current.delete();
			currentListView.getItems().remove(current);
			endedListView.getItems().remove(current);

		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Project p = ((Project) o);
		timePassed.setText(p.getCurrentTimeOn());
		FieldTimePassed.setText(p.getTotalTimePassed());
	}
}
