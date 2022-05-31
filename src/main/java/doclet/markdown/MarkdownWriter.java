package doclet.markdown;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MarkdownWriter {

	List<String> lines = new ArrayList<>();

	public void cover(String title, String author, String date) {

		lines.add(0, "% " + markdown(date));
		lines.add(0, "% " + markdown(author));
		lines.add(0, "% " + markdown(title));
		breakElement();
	}

	public void heading1(String str) {
		lines.add("# " + markdown(str));
		breakElement();
	}

	public void heading2(String str) {
		lines.add("## " + markdown(str));
		breakElement();
	}

	public void heading3(String str) {
		lines.add("### " + markdown(str));
		breakElement();
	}

	public void heading4(String str) {
		lines.add("#### " + markdown(str));
		breakElement();
	}

	public void heading5(String str) {
		lines.add("##### " + markdown(str));
		breakElement();
	}

	public void unorderedList(String str) {
		lines.add("* " + markdown(str));
	}

	public void orderedList(String str) {
		lines.add("1. " + markdown(str));
	}

	public void definition(String item, String term) {
		lines.add(markdown(item));
		if (!term.isEmpty()) {
			lines.add(":   " + markdown(term));
		} else {
			lines.add(":   " + "(undef)");
		}
		breakElement();
	}

	public void line(String str) {
		lines.add(markdown(str) + "  ");
	}

	public void columns(String... cols) {
		StringBuilder sb = new StringBuilder();
		sb.append('|');
		for (String col : cols) {
			sb.append(markdown(col));
			sb.append('|');
		}
		lines.add(sb.toString());
	}

	public void breakElement() {
		lines.add("");
	}

	private String markdown(String str) {

		str = Pattern.compile("<code>(.*?)</code>").matcher(str).replaceAll("`$1`");
		str = Pattern.compile("<i>(.*?)</i>").matcher(str).replaceAll("_$1_");
		str = Pattern.compile("<em>(.*?)</em>").matcher(str).replaceAll("_$1_");
		str = Pattern.compile("<b>(.*?)</b>").matcher(str).replaceAll("__$1__");
		str = Pattern.compile("<strong>(.*?)</strong>").matcher(str).replaceAll("__$1__");
		str = Pattern.compile("<a href=\"(https?://.+?)\">(.+?)</a>").matcher(str).replaceAll("[$2]($1)");
		str = Pattern.compile("\\{@link +(.+?) +(.+?)}").matcher(str).replaceAll("[$2]($1)");
		str = Pattern.compile("\\{@link +(.+?)}").matcher(str).replaceAll("[$1]($1)");
		str = Pattern.compile("\\{@code +(.+?)}").matcher(str).replaceAll("`$1`");

		str = str.replaceAll("\\\\", "\\\\\\\\");
		str = Pattern.compile("<(.*?)>").matcher(str).replaceAll("\\\\<$1\\\\>");

		str = str.replaceAll("&lt;", "<");
		str = str.replaceAll("&gt;", ">");
		str = str.replaceAll("&quot;", "\"");
		str = str.replaceAll("&apos;", "'");
		str = str.replaceAll("&nbsp;", " ");
		str = str.replaceAll("&amp;", "&");

		return str;
	}

	public void save(File file) throws IOException {
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			for (String line : lines) {
				writer.write(line);
				writer.write(System.lineSeparator());
			}
		}
	}
}