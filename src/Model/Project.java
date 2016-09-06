package Model;

import javafx.application.Platform;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Adriano on 16.08.2016.
 */
public class Project extends Observable implements Serializable {
	private static final String PROJECTS_PATH = "projects.txt";
	private static List<Project> projects = getProjectsFromFile();
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	private String name;
	private LocalDateTime begin;
	private Duration timeOn;
	private LocalDateTime end;
	private Timer timer;

	private Project(String name) {
		this(name, LocalDateTime.now(), Duration.ZERO, LocalDateTime.MIN);
	}

	private Project(String name, LocalDateTime begin, Duration timer, LocalDateTime end) {
		this.name = name;
		this.begin = begin;
		this.timeOn = timer;
		this.end = end;
	}

	public boolean isEnded() {
		return end.isAfter(begin);
	}

	private static List<Project> getProjectsFromFile() {
		LinkedList<Project> pro = new LinkedList<>();
		try {
			Files.lines(Paths.get(PROJECTS_PATH))
			     .forEach(line -> {
				     String[] dec = line.split(";");
				     pro.add(new Project(dec[0], LocalDateTime.parse(dec[1]), Duration.parse(dec[2]), LocalDateTime.parse(dec[3])));
			     });
			return pro;
		} catch (IOException e) {
			System.out.println("No " + PROJECTS_PATH + " founded");
			System.out.println("Creating one ...");
			try {
				Files.write(Paths.get(PROJECTS_PATH), new LinkedList<String>());
			} catch (IOException e1) {
				System.out.println("Impossible to create the file");
				e1.printStackTrace();
			}
			System.out.println("File " + PROJECTS_PATH + " created");

			return new LinkedList<>();
		}
	}

	public static void saveAllProjects() {
		try {
			Files.write(Paths.get(PROJECTS_PATH), projects.stream()
			                                              .map(p -> p.name + ";" + p.begin.format(DATE_FORMATTER) + ";" + p.timeOn + ";" + p.end.format(DATE_FORMATTER))
			                                              .collect(Collectors.toList()));
		} catch (IOException e) {
			System.out.println("Impossible to write into the file " + PROJECTS_PATH);
		}
	}

	public static List<Project> getProjects(Predicate<Project> p) {
		return projects.stream().filter(p).collect(Collectors.toList());
	}

	public void delete() {
		projects.remove(this);
	}

	public void endIt() {
		end = LocalDateTime.now();
	}

	public void continueIt() {
		end = LocalDateTime.MIN;
	}

	public static Project addProject(String name) {
		Project p = new Project(name);
		projects.add(p);
		return p;
	}

	public String getName() {
		return name;
	}

	public LocalDateTime getBegin() {
		return begin;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public Duration getTimeOn() {
		return timeOn;
	}

	public void startSession() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				Platform.runLater(() -> {
					timeOn = timeOn.plusSeconds(1);
					setChanged();
					notifyObservers();
				});
			}
		}, 0, 1000);
	}

	public void stopSession() {
		timer.cancel();
		timer.purge();
	}

	@Override
	public String toString() {
		return name;
	}
}
