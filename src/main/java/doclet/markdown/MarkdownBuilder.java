package doclet.markdown;

import com.sun.javadoc.*;
import doclet.counter.CountInfo;
import doclet.counter.Counter;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MarkdownBuilder {

	private RootDoc root;

	private List<PackageDoc> packages;

	private Map<File, CountInfo> counts;

	private static final String NO_COMMENT = "No explanation";

	public void makeDirs() {
		for (Types types : Types.values()) {
			if (!types.equals(Types.OTHER)) {
				if (!new File( System.getProperty("user.dir") + "/"+ types.name()).mkdir()) {
					System.out.println(types.name() + " directory already exists");
				}
			}
		}
	}

	public void create(RootDoc rootDoc) throws IOException {

		makeDirs();

		root = rootDoc;

		packages = new ArrayList<>();

		counts = new HashMap<>();

		makeClassPages();

		makeCoverPage();
	}

	private void makeCoverPage() throws IOException {

		String title = Options.getOption("title");
		if (!Options.getOption("subtitle").isEmpty()) {
			if (!title.isEmpty()) {
				title += " ";
			}
			title += Options.getOption("subtitle");
		}
		if (!Options.getOption("version").isEmpty()) {
			if (!title.isEmpty()) {
				title += " ";
			}
			title += Options.getOption("version");
		}

		Locale locale = new Locale("ENGLISH", "US", "WIN");
		Calendar cal = Calendar.getInstance(locale);
		DateFormat jformat = new SimpleDateFormat("dd/MM/yyyy", locale);
		String stamp = jformat.format(cal.getTime());
		MarkdownWriter md = new MarkdownWriter();
		String COMPANY = "VODACOM";
		md.cover(title, COMPANY, stamp);
		makeCountPage(md);
		md.save(new File("Info.md"));
	}

	private String getParamSignature(Parameter[] parameters) {
		StringBuilder sb = new StringBuilder();
		for (Parameter parameter : parameters) {
			if (0 < sb.length()) {
				sb.append(", ");
			}
			sb.append(parameter.name());
		}
		return sb.toString();
	}

	private String getParamComment(ParamTag[] tags, String name) {
		for (ParamTag tag : tags) {
			if (tag.parameterName().equals(name)) {
				return tag.parameterComment();
			}
		}
		return "";
	}

	private String getThrowsComment(ThrowsTag[] tags, String name) {
		for (ThrowsTag tag : tags) {
			if (tag.exceptionName().equals(name)) {
				return tag.exceptionComment();
			}
		}
		return "";
	}

	private Types getAnnotationValue(AnnotationDesc[] annotationDescArr) {
		for (AnnotationDesc annotationDesc : annotationDescArr) {
			String annotationString = annotationDesc.toString().toLowerCase();
			if (annotationString.contains("repository")) return Types.Z_REPOSITORIES;
			else if (annotationString.contains("controller")) return Types.Z_CONTROLLERS;
			else if (annotationString.contains("service")) return Types.Z_SERVICES;
			else if (annotationString.contains("component")) return Types.Z_COMPONENTS;
			else if (annotationString.contains("entity")) return Types.Z_ENTITIES;
		}
		return Types.OTHER;
	}

	private Types getType(ClassDoc classDoc) {

		Types types = getAnnotationValue(classDoc.annotations());

		if (types.equals(Types.OTHER)) {
			if (classDoc.isInterface()) return Types.Z_INTERFACES;
			else if (classDoc.isAbstract()) return Types.Z_ABSTRACT_CLASSES;
			else if (classDoc.isEnum()) return Types.Z_ENUMS;
		}
		return types;
	}

	private void makeClassPages() throws IOException {

		File saveTo;
		Types types;

		for (ClassDoc classDoc : root.classes()) {

			String docS;
			int methodCount = 1;
			int docCount = 0;

			types = getType(classDoc);
			File source = classDoc.position().file();

			if (types.equals(Types.OTHER)) {
				Counter.count(source, "", false, 0, 0);
				continue;
			}

			PackageDoc packageDoc = classDoc.containingPackage();
			MarkdownWriter md = new MarkdownWriter();

			md.line("");

			if (!packages.contains(packageDoc)) {

				md.heading1(packageDoc.name() + " package");

				docS = getText(packageDoc.commentText());
				if (!docS.equals(NO_COMMENT)) docCount += 1;
				print(docS, md);

				packages.add(packageDoc);
			}

			String classType;
			if (classDoc.isInterface()) {
				classType = "Interface";
			} else {
				if (classDoc.isAbstract()) {
					classType = "Abstract Class";
				} else {
					classType = "Class";
				}
			}

			md.heading2(classDoc.modifiers() + " " + classDoc.name() + " " + classType);

			print(getText(classDoc.commentText()), md);

			md.heading3("Package");
			md.unorderedList(classDoc.containingPackage().name());
			md.breakElement();

			List<ClassDoc> classDocs = new ArrayList<>();
			classDocs.add(classDoc);
			ClassDoc d = classDoc.superclass();
			while (d != null && !d.qualifiedName().equals("java.lang.Object")) {
				classDocs.add(d);
				d = d.superclass();
			}
			if (2 <= classDocs.size()) {
				md.heading3("Inherited Class Hierarchy");
				Collections.reverse(classDocs);
				for (ClassDoc doc : classDocs) {
					md.orderedList(doc.qualifiedName());
				}
				md.breakElement();
			}

			if (0 < classDoc.interfaces().length) {
				md.heading3("All Implemented Interfaces");
				for (int i = 0; i < classDoc.interfaces().length; i++) {
					md.unorderedList(classDoc.interfaces()[i].qualifiedName());
				}
				md.breakElement();
			}

			Tag[] versionTags = classDoc.tags("version");
			if (0 < versionTags.length) {
				md.heading3("version");
				for (Tag versionTag : versionTags) {
					md.unorderedList(versionTag.text());
				}
				md.breakElement();
			}

			Tag[] authorTags = classDoc.tags("author");
			if (0 < authorTags.length) {
				md.heading3("author");
				for (Tag authorTag : authorTags) {
					md.unorderedList(authorTag.text());
				}
				md.breakElement();
			}

			if (0 < classDoc.enumConstants().length) {
				md.heading4("Constants");
				for (int i = 0; i < classDoc.enumConstants().length; i++) {
					writeFieldDoc(classDoc.enumConstants()[i], md);
				}
			}

			if (0 < classDoc.fields().length) {
				md.heading4("Fields");
				for (int i = 0; i < classDoc.fields().length; i++) {
					writeFieldDoc(classDoc.fields()[i], md);
				}
			}

			for (int i = 0; i < classDoc.constructors().length; i++) {
				methodCount += 1;
				if (writeMemberDoc(classDoc.constructors()[i], md)) docCount += 1;
			}

			for (int i = 0; i < classDoc.methods().length; i++) {
				methodCount += 1;
				if (writeMemberDoc(classDoc.methods()[i], md)) docCount += 1;
			}

			saveTo = new File( System.getProperty("user.dir") + "/"+ types.name() + "/" + classDoc.name() + ".md");
			String[] splitParentPath = saveTo.getParent().split("[\\\\\\/]");
			CountInfo ci = Counter.count(source,
					splitParentPath[splitParentPath.length - 1] + "/" + saveTo.getName().replaceAll("\\.md", ""),
					true,
					methodCount,
					docCount);

			if (ci != null) {
				md.heading3("File");
				md.unorderedList(String.format("%s - %,d Step %,d OK", source.getName(), ci.getSteps(), ci.getLines()));
				md.breakElement();
			}
			counts.put(source, ci);

			md.line("[File Overview](Code Coverage Info)");
			md.save(saveTo);
		}
	}

	private void writeFieldDoc(MemberDoc doc, MarkdownWriter md) {
		String fieldType;
		if (doc.isEnumConstant()) {
			fieldType = "Enum Constant";
		} else if (doc.isEnum()) {
			fieldType = "Enum";
		} else {
			fieldType = "Field";
		}

		md.heading5(doc.modifiers() + " " + getShortName(((FieldDoc) doc).type()) + " " + doc.name() + " " + fieldType);
		print(getText(doc.commentText()), md);
	}

	private void addAnnotations(ExecutableMemberDoc doc, MarkdownWriter md) {

		String[] splitVal;

		for (AnnotationDesc annotationDesc : doc.annotations()) {
			splitVal = annotationDesc.toString().split("\\.");
			md.line("@" + splitVal[splitVal.length - 1]);
		}
	}

	private boolean writeMemberDoc(ExecutableMemberDoc doc, MarkdownWriter md) {

		String memberType;
		boolean hasDoc = false;

		if (doc.isConstructor()) {
			memberType = "Constructor";
		} else if (doc.isMethod()) {
			memberType = "Method";
		} else {
			memberType = "Member";
		}

		String str = doc.modifiers();
		if (doc instanceof MethodDoc) {
			str += " " + getShortName(((MethodDoc) doc).returnType());
		}
		str += " " + doc.name() + " (" + getParamSignature(doc.parameters()) + ") " + memberType;
		addAnnotations(doc, md);
		md.heading4(str);
		String docS = getText(doc.commentText());
		if (!docS.equals(NO_COMMENT)) hasDoc = true;
		print(docS, md);

		Parameter[] parameters = doc.parameters();
		if (0 < parameters.length) {
			for (Parameter parameter : parameters) {
				md.heading5(getShortName(parameter.type()) + " " + parameter.name() + " Parameters");
				print(getText(getParamComment(doc.paramTags(), parameter.name())), md);
			}
		}

		if (doc instanceof MethodDoc) {
			MethodDoc method = (MethodDoc) doc;
			if (0 < method.tags("return").length) {
				md.heading5("Return Value");
				print(method.tags("return")[0].text(), md);
			}
		}

		Type[] exceptions = doc.thrownExceptionTypes();
		if (0 < exceptions.length) {
			md.heading5("Exception");
			for (Type exception : exceptions) {
				md.definition(getShortName(exception), getText(getThrowsComment(doc.throwsTags(), exception.typeName())));
			}
		}
		return hasDoc;
	}

	private void print(String str, MarkdownWriter md) {

		String[] paragraphs = str.split("\\s*<([pP])>\\s*");
		for (int i = 0; i < paragraphs.length; i++) {

			paragraphs[i] = paragraphs[i].replaceAll("\\s*[\\r\\n]+\\s*", " ");

			paragraphs[i] = paragraphs[i].replaceAll("\\.\\s+", ".\n");
			paragraphs[i] = paragraphs[i].replaceAll("。\\s*", "。\n");

			String[] lines = paragraphs[i].split("\n");
			for (String line : lines) {
				md.line(line);
			}

			md.breakElement();
		}
	}

	private String getText(String str) {
		if (str == null || str.isEmpty()) {
			return MarkdownBuilder.NO_COMMENT;
		}
		return str;
	}

	private String getShortName(Type type) {
		String name = type.toString();
		name = name.replaceAll("[a-zA-Z0-9\\-_]+\\.", "");
		return name;
	}

	private void makeCountPage(MarkdownWriter md) {

		int count = 0;
		long sumSize = 0;
		int sumSteps = 0;
		int sumBranks = 0;
		int sumLines = 0;
		int sumMethodCount = 0;
		int sumDocCount = 0;

		md.heading1("File List");

		md.columns("File", "bytes", "Step", "Blank Lines", "Number of Lines", "Methods", "Javadocs", "Javadoc Coverage");
		md.columns(":-----", "-----:", "-----:", "-----:", "-----:", "-----:", "-----:", "-----:");

		if (0 < counts.size()) {

			List<File> files = new ArrayList<>(counts.keySet());
			files.sort(Comparator.comparing(File::getName));

			for (File file : files) {

				long size = file.length();
				int steps = counts.get(file).getSteps();
				int branks = counts.get(file).getBranks();
				int lines = counts.get(file).getLines();
				int methodCount = counts.get(file).methodCount();
				sumMethodCount += methodCount;
				int docCount = counts.get(file).docCount();
				sumDocCount += docCount;
				String fileNameCol = file.getName();

				if (counts.get(file).getHref()) {
					fileNameCol = "[" + file.getName() + "]" + "(" + counts.get(file).getMdLocation() + ")";
				}

				count++;
				sumSize += size;
				sumSteps += steps;
				sumBranks += branks;
				sumLines += lines;

				md.columns(fileNameCol,
						String.format("%,d", size),
						String.format("%,d", steps),
						String.format("%,d", branks),
						String.format("%,d", lines),
						String.format("%,d", methodCount - 1),
						String.format("%,d", docCount),
						CountInfo.getJavadocCoverage(methodCount, docCount)
				);
			}
		}

		md.columns(String.format("Total files: %,d", count),
				String.format("%,d", sumSize),
				String.format("%,d", sumSteps),
				String.format("%,d", sumBranks),
				String.format("%,d", sumLines),
				String.format("%,d", sumMethodCount),
				String.format("%,d", sumDocCount),
				CountInfo.getJavadocCoverage(sumMethodCount - counts.size() + 1, sumDocCount)
		);
		md.breakElement();
	}
}