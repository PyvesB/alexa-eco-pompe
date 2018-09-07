package io.github.pyvesb.alexaecopompe.speech;

public enum Messages {;

	public static final String NAME = "Éco Pompe";
	public static final String CARD_HELP = "Exemples:\n\"le sans plomb 98 à Lyon\"\n\"le gazole dans la Creuse\"\n\"l'E10 à moins de 10 kilomètres\"\nCarburants: gazole, sans plomb 95, sans plomb 98, E10, E85 ou GPL.";
	public static final String LAUNCH = "Bienvenue ! Je peux trouver le carburant le moins cher par ville, par département ou près de chez vous. Dîtes \"aide\" pour obtenir les instructions.";
	public static final String HELP = "Spécifiez un carburant suivi d'une ville, d'un département ou d'une distance. Par exemple : \"le sans plomb 98 à Lyon\", \"le gazole dans la Creuse\", ou \"l'E10 à moins de 10 kilomètres\".";
	public static final String CANCEL_STOP = "D'accord. À bientôt !";
	public static final String UNSUPPORTED = "Je n'ai pas compris. Réessayez, ou bien dîtes \"aide\" pour obtenir les instructions.";
	public static final String STATION_FOUND = "$NAME vend $TYPE pour $PRICE. $SUBJECT est située $ADDRESS à $TOWN, et a actualisé ses tarifs $DATE.";
	public static final String STATION_FOUND_E10 = "Je n'ai pas trouvé de pompe vendant du sans plomb 95. Cependant, $NAME vend de l'E10 pour $PRICE. $SUBJECT est située $ADDRESS à $TOWN, et a actualisé ses tarifs $DATE.";
	public static final String NO_STATION_TOWN = "Je n'ai pas trouvé de pompe dans $LOCATION. Éssayez une ville différente.";
	public static final String NO_STATION_RADIUS = "Je n'ai pas trouvé de pompe à moins de $RADIUS kilomètres. Réessayez en spécifiant une distance plus grande.";
	public static final String NO_STATION_FOR_TYPE_TOWN = "Je n'ai pas trouvé de pompe vendant $TYPE dans $LOCATION. Éssayez un autre carburant ou une ville différente.";
	public static final String NO_STATION_FOR_TYPE_RADIUS = "Je n'ai pas trouvé de pompe vendant $TYPE à moins de $RADIUS kilomètres. Réessayez en spécifiant un autre carburant ou une distance plus grande.";
	public static final String UNSUPPORTED_GAS_TYPE = "Je n'ai pas compris le carburant demandé. Veuillez réessayer en utilisant gazole, sans plomb 95, sans plomb 98, E10, E85 ou GPL.";
	public static final String UNSUPPORTED_LOCATION = "Je n'ai pas trouvé d'informations pour cette localisation géographique. Veuillez réessayer en énonçant clairement un nom de ville ou de département.";
	public static final String INCORRECT_RADIUS = "Veuillez fournir une distance comprise entre 1 et 50 kilomètres. Par exemple : \"le sans plomb 95 à moins de 10 kilomètres\".";
	public static final String LOCATION_BAD_REQUEST = "Veuillez réessayer en fournissant un carburant suivi d'une ville ou d'un département. Par exemple : \"le sans plomb 98 à Paris\" ou \"le gazole dans la Creuse\".";
	public static final String RADIUS_BAD_REQUEST = "Veuillez réessayer en fournissant un carburant suivi d'une distance. Par exemple : \"le sans plomb 95 à moins de 10 kilomètres\".";
	public static final String MISSING_PERMS = "J'ai besoin de votre adresse pour trouver les pompes à proximité. Veuillez autoriser l'accès dans l'application Alexa, ou bien précisez un nom de ville ou de département.";
	public static final String ADDRESS_ERROR = "Alexa a retourné une erreur. Réessayez plus tard, ou bien précisez un nom de ville ou de département.";
	public static final String POSITION_UNKNOWN = "Je n'ai pas réussi à déterminer votre position géographique avec l'adresse renseignée dans votre Amazon Echo. Réessayez plus tard, ou bien précisez un nom de ville ou de département.";

}
