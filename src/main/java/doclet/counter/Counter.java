package doclet.counter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Counter {

	public static CountInfo count(File file, String mdLocation, boolean href, int methodCount, int docCount) {

		BufferedReader br = null;
		try {

			int lines = 0;
			int steps = 0;
			int branks = 0;

			String line;

			boolean comment = false;

			br = new BufferedReader(new FileReader(file));

			while ((line = br.readLine()) != null) {

				lines++;

				line = line.trim();

				if (line.length() == 0) {
					branks++;
				} else {
					if (line.startsWith("/*") && line.endsWith("*/")) {
						comment = false;
					} else if (line.startsWith("/*")) {
						comment = true;
					} else if (line.endsWith("*/")) {
						comment = false;
					} else {
						if (!comment && !line.startsWith("//")) {
							steps++;
						}
					}
				}
			}

			return new CountInfo(lines, steps, branks, mdLocation, href, methodCount, docCount);

		} catch (IOException ignored) {
		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException ignored) {
				}
			}
		}

		return null;
	}
}