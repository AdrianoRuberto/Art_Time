package Model;

import javafx.application.Platform;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Adriano on 16.08.2016.
 */
public class Project extends Observable implements Serializable {
	private static final String PROJECTS_PATH = "projects.txt";
	private static List<Project> projects = getProjectsFromFile();

	private String name;
	private Date begin;
	private long timeOn;
	private Date end;
	Timer timer = new java.util.Timer();

	public Project(String name) {
		this(name, new Date(), -1, new Date(-1));
	}

	private Project(String name, Date begin, long timer, Date end) {
		this.name = name;
		this.begin = begin;
		this.timeOn = timer;
		this.end = end;
	}

	public boolean isEnded() {
		return end.after(begin);
	}

	private static List<Project> getProjectsFromFile() {
		LinkedList<Project> pro = new LinkedList<>();
		try {
			Files.lines(Paths.get(PROJECTS_PATH))
			     .forEach(line -> {
				     String[] dec = line.split(";");
				     pro.add(new Project(dec[0], new Date(Long.parseLong(dec[1])), Long.parseLong(dec[2]), new Date(Long.parseLong(dec[3]))));
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

	public static List<Project> getProjects(Predicate<Project> p) {
		return projects.stream().filter(p).collect(Collectors.toList());
	}

	public String getName() {
		return name;
	}

	public Date getBegin() {
		return begin;
	}

	public String getTimeOn() {
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeOn),
				TimeUnit.MILLISECONDS.toMinutes(timeOn) % TimeUnit.HOURS.toMinutes(1),
				TimeUnit.MILLISECONDS.toSeconds(timeOn) % TimeUnit.MINUTES.toSeconds(1));
	}

	public void startSession() {
		timer.schedule(new TimerTask() {
			public void run() {
				Platform.runLater(() -> {
					timeOn += 1000;
					System.out.println(timeOn);
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
}
