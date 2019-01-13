package io.github.pyvesb.alexaecopompe.domain;

public enum GasType {

	UNKNOWN("Inconnu", "du carburant inconnu"),
	GAZOLE("Gazole", "du gazole"),
	SP95("Sans Plomb 95", "du sans plomb 95"),
	E85("E85", "de l'E85"),
	GPL("GPL", "du GPL"),
	E10("E10", "de l'E10"),
	SP98("Sans Plomb 98", "du sans plomb 98");

	private final String dispayName;
	private final String speechText;

	private GasType(String dispayName, String speechText) {
		this.dispayName = dispayName;
		this.speechText = speechText;
	}
	
	public static GasType fromId(String id) {
		return GasType.values()[Integer.parseInt(id)];
	}

	public String getDisplayName() {
		return dispayName;
	}

	public String getSpeechText() {
		return speechText;
	}

	public int getId() {
		return ordinal();
	}

	public String getIdString() {
		return Integer.toString(ordinal());
	}

}
