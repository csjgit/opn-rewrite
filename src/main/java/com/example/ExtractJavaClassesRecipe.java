package com.example;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaParser;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExtractJavaClassesRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Extract Java Classes from XML";
    }

    @Override
    public String getDescription() {
        return "Extracts Java classes from XML files and creates separate Java source files.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlToJavaVisitor();
    }

    private static class XmlToJavaVisitor extends TreeVisitor<Xml.Document, ExecutionContext> {
        private final XPathMatcher classMatcher = new XPathMatcher("//class");
        private final JavaParser javaParser = JavaParser.fromJavaVersion().build();


        public Xml.Document visitDocument(Xml.Document document, ExecutionContext ctx) {
            try {
                // Parse the XML document
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(
                        new ByteArrayInputStream(document.printAll().getBytes(StandardCharsets.UTF_8))
                );

                // Create XPath for finding Java classes
                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expr = xpath.compile("//class");
                NodeList classes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                // Process each Java class found
                for (int i = 0; i < classes.getLength(); i++) {
                    String className = classes.item(i).getAttributes()
                            .getNamedItem("name")
                            .getNodeValue();

                    String javaCode = classes.item(i)
                            .getTextContent()
                            .trim();

                    // Remove CDATA wrapper if present
                    javaCode = javaCode.replace("<![CDATA[", "")
                            .replace("]]>", "")
                            .trim();

                    // Create Java source file
                    Path sourcePath = Paths.get("src/main/java/" + className + ".java");
                    ctx.putMessage(
                            "java.source." + className,
                            new JavaSourceFile(sourcePath, javaCode)
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return document;
        }
    }

    private static class JavaSourceFile {
        private final Path path;
        private final String content;

        public JavaSourceFile(Path path, String content) {
            this.path = path;
            this.content = content;
        }

        public Path getPath() {
            return path;
        }

        public String getContent() {
            return content;
        }
    }
}