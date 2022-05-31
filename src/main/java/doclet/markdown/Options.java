package doclet.markdown;

public class Options {

	public static String[][] options;

	public static String getOption(String name) {
		return getOption(name, "");
	}

	public static String getOption(String name, String defaultValue) {
		for (String[] opt : options) {
			if (opt[0].equals("-" + name)) {
				return opt[1];
			}
		}
		return defaultValue;
	}

	public static boolean isSupportedOption(String option) {
		switch (option) {
		case "-file":
		case "-title":
		case "-subtitle":
		case "-version":
		case "-company":
			return true;
		}
		return false;
	}
}