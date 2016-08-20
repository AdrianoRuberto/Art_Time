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
	private Timer timer;

	public Project(String name) {
		this(name, new Date(), 0, new Date(-1));
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

	public static void saveAllProjects() {
		try {
			Files.write(Paths.get(PROJECTS_PATH), projects.stream()
			                                              .map(p -> p.name + ";" + p.begin.getTime() + ";" + p.timeOn + ";" + p.end.getTime())
			                                              .collect(Collectors.toList()));
		} catch (IOException e) {
			System.out.println("Impossible to write into the file " + PROJECTS_PATH);
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
		return String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(timeOn),
				TimeUnit.SECONDS.toMinutes(timeOn) % TimeUnit.HOURS.toMinutes(1),
				TimeUnit.SECONDS.toSeconds(timeOn) % TimeUnit.MINUTES.toSeconds(1));
	}

	public void startSession() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				Platform.runLater(() -> {
					++timeOn;
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
