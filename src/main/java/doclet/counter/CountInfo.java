package doclet.counter;

public class CountInfo {

	private final int lines;

	private final int steps;

	private final int branks;

	private final String mdLocation;

	private final boolean href;

	private final int methodCount;

	private final int docCount;

	public CountInfo(int lines, int steps, int branks, String mdLocation, boolean href, int methodCount, int docCount) {
		this.docCount= docCount;
		this.methodCount = methodCount;
		this.href = href;
		this.lines = lines;
		this.steps = steps;
		this.branks = branks;
		this.mdLocation = mdLocation;
	}

	public int methodCount() { return methodCount; }

	public int docCount() { return docCount; }

	public boolean getHref(){ return href; }

	public int getLines() {
		return lines;
	}

	public int getSteps() {
		return steps;
	}

	public int getBranks() {
		return branks;
	}

	public String getMdLocation() { return this.mdLocation; }

	public static String getJavadocCoverage(int methodsI, int docsI) {
		if (methodsI == 0) return "N/A";
		if (docsI == 0) return "0%";
		double percentage = (double) docsI / (double) methodsI;
		percentage = percentage * 100;
		if (percentage == 100) return "100%";
		if (percentage < 10) return Double.toString(percentage).charAt(0) + "%";
		return Double.toString(percentage).charAt(0) + Double.toString(percentage).charAt(1) + "%";
	}
}