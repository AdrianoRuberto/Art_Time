package Model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Adriano on 16.08.2016.
 */
public class Projet implements Serializable {
	private String name;
	private Date begin = new Date();

	public Projet(String name) {
		this.name = name;
	}

}
