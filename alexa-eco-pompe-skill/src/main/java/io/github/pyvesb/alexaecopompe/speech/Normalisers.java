package io.github.pyvesb.alexaecopompe.speech;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

public class Normalisers {

	private static final Pattern TOWN_CEDEX = Pattern.compile(" Cedex.*");
	private static final Pattern ADDRESS_COMPLEMENT = Pattern.compile("\\/[0-9]+");
	private static final String[] TOWN_REPLACEES = new String[] { "Mt ", "S/", "/" };
	private static final String[] TOWN_REPLACEMENTS = new String[] { "Mont ", "Sur ", "," };
	private static final String[] ADDRESS_REPLACEES = new String[] { "Bld.", "Bld ", "Bd ", "Av.", "Av ", "Ave ", "Rn ", "Rd ", "Za ", "Zac ", ",", "S/ ", "/" };
	private static final String[] ADDRESS_REPLACEMENTS = new String[] { "Boulevard ", "Boulevard ", "Boulevard ", "Avenue ", "Avenue ","Avenue ", "RN ", "RD ", "Z.A. ", "Z.A.C. ", "", "Sur ", "," };
	private static final String[] NAME_REPLACEES = new String[] { "Sas ", "Sarl", "E.leclerc", "rmarche", "Geant " };
	private static final String[] NAME_REPLACEMENTS = new String[] { "S.A.S. ", "S.A.R.L.", "Leclerc", "rmarché", "Géant " };

	public static String normaliseTown(String town) {
		String capitalized = WordUtils.capitalizeFully(town, ' ', '-', '\'', '/');
		String capitalizedWithoutCedex = TOWN_CEDEX.matcher(capitalized).replaceAll("");
		return StringUtils.replaceEach(capitalizedWithoutCedex, TOWN_REPLACEES, TOWN_REPLACEMENTS);
	}

	public static String normaliseAddress(String address) {
		String capitalized = WordUtils.capitalizeFully(address, ' ', '-', '\'');
		String capitalizedWithoutComplement = ADDRESS_COMPLEMENT.matcher(capitalized).replaceAll("");
		return StringUtils.replaceEach(capitalizedWithoutComplement, ADDRESS_REPLACEES, ADDRESS_REPLACEMENTS);
	}

	public static String normaliseGasStationName(String gasStationName) {
		String capitalized = WordUtils.capitalizeFully(gasStationName, ' ', '-');
		return StringUtils.replaceEach(capitalized, NAME_REPLACEES, NAME_REPLACEMENTS);
	}
	
	private Normalisers() {
		// Not called.
	}

}
