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

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Main extends Application implements Initializable, Observer {

	public ListView<Project> currentListView = new ListView<>();
	public ListView<Project> endedListView = new ListView<>();

	public Label timePassed = new Label("Select a project");

	public TabPane tabPane;
	public Tab tabMore;
	public Tab tabTimer;

	public Button cmdSessionStart;
	public Button cmdEnd;
	public Button cmdContinue;

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
			Project.getProjects(p -> true).forEach(Project::stopSession);
			Project.saveAllProjects();
		});
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void noProjectSelected() {
		tabMore.setDisable(true);
		tabTimer.setDisable(false);
		cmdSessionStart.setVisible(false);
		tabPane.getSelectionModel().select(tabTimer);
		timePassed.setText("Select a project");
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

		noProjectSelected();

		endedListView.getSelectionModel().selectedItemProperty().addListener((p, oldProj, newProj) -> {
			if (current != null && sessionInUse) {
				session(null);
			}

			if (newProj == null) {
				noProjectSelected();
			} else {
				currentListView.getSelectionModel().clearSelection();

				tabPane.getSelectionModel().select(tabMore);

				current = newProj;

				// Timer tab
				tabTimer.setDisable(true);

				// More Tab
				tabMore.setDisable(false);
				cmdEnd.setVisible(false);
				cmdContinue.setVisible(true);
				FieldBegin.setText(newProj.getBegin().format(DateTimeFormatter.ISO_LOCAL_DATE));
				FieldEnd.setText(newProj.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE));
				FieldTimePassed.setText(getTotalTimePassed(newProj.getTimeOn()));
				FieldRatioHourDay.setText(getHourDayRatio(newProj));
			}
		});

		currentListView.getSelectionModel().selectedItemProperty().addListener((p, oldProj, newProj) -> {
			if (current != null && sessionInUse) {
				session(null);
			}

			if (newProj == null) {
				noProjectSelected();
			} else {
				endedListView.getSelectionModel().clearSelection();
				current = newProj;

				// Timer tab
				tabTimer.setDisable(false);
				cmdSessionStart.setVisible(true);
				timePassed.setText(durationToChrono(newProj.getTimeOn()));

				// More Tab
				tabMore.setDisable(false);
				cmdEnd.setVisible(true);
				cmdContinue.setVisible(false);
				FieldBegin.setText(newProj.getBegin().format(DateTimeFormatter.ISO_LOCAL_DATE));
				FieldEnd.setText("Not ended yet");
				FieldTimePassed.setText(getTotalTimePassed(newProj.getTimeOn()));
				FieldRatioHourDay.setText(getHourDayRatio(newProj));
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
		Util.Popup.input("Project Creation", "Enter the name of the new project :", "piou !").ifPresent(name -> {
			Project p = Project.addProject(name);
			currentListView.getItems().add(p);
			p.addObserver(this);
		});
	}

	public void deleteProject(ActionEvent event) {
		if (current != null) {
			ButtonType yes = ButtonType.YES;
			ButtonType cancel = ButtonType.CANCEL;
			Util.Popup.warning("Delete project", "Are you sure to delete " + current.getName() + " ?\nThis action is irreversible", yes, cancel).ifPresent(b -> {
				if (sessionInUse)
					current.stopSession();
				current.delete();
				currentListView.getItems().remove(current);
				endedListView.getItems().remove(current);
			});
		}
	}

	public void continueProject(ActionEvent event) {
		ButtonType yes = ButtonType.YES;
		ButtonType cancel = ButtonType.CANCEL;
		Util.Popup.warning("Continue project", "Are you sure to continue  " + current.getName() + " ?", yes, cancel).ifPresent(b -> {
			if (b.equals(yes)) {
				current.continueIt();
				currentListView.getItems().add(current);
				endedListView.getItems().remove(current);
			}
		});
	}

	public void endProject(ActionEvent event) {
		if (sessionInUse)
			current.stopSession();
		current.endIt();
		endedListView.getItems().add(current);
		currentListView.getItems().remove(current);
	}

	private String durationToChrono(Duration time) {
		return String.format("%02d:%02d:%02d", time.toHours(), time.toMinutes() % 60, time.getSeconds() % 60);
	}

	private String getTotalTimePassed(Duration time) {
		return String.format("%d s or %d m or %d h or %d d", time.getSeconds(), time.toMinutes(), time.toHours(), time.toDays());
	}

	private String getHourDayRatio(Project p) {
		LocalDateTime end = p.isEnded() ? p.getEnd() : LocalDateTime.now();
		return String.format("%d", p.getTimeOn().toHours() / (ChronoUnit.DAYS.between(p.getBegin(), end) + 1));
	}

	@Override
	public void update(Observable o, Object arg) {
		Project p = ((Project) o);
		timePassed.setText(durationToChrono(p.getTimeOn()));
		FieldTimePassed.setText(getTotalTimePassed(p.getTimeOn()));
	}
}
