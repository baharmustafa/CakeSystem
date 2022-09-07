package uni.fmi.masters;

import java.util.ArrayList;

public class Cake {
	private String id;
	private String name;
	private ArrayList<String> ingridients = new ArrayList<>();
	
	public Cake() { }
	
	public Cake(String id, String name, ArrayList<String> ingridients) {
		super();
		this.id = id;
		this.name = name;
		this.ingridients = ingridients;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getIngridients() {
		return ingridients;
	}

	public void setIngridients(ArrayList<String> ingridients) {
		this.ingridients = ingridients;
	}
}
