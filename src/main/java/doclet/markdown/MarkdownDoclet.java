package doclet.markdown;

import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

public class MarkdownDoclet extends Doclet {

	public static boolean start(RootDoc rootDoc) {
		MarkdownBuilder creator = new MarkdownBuilder();
		try {
			Options.options = rootDoc.options();
			creator.create(rootDoc);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static int optionLength(String option) {
		if (Options.isSupportedOption(option)) {
			return 2;
		}
		return 0;
	}

	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}
}