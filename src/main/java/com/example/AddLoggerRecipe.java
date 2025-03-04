// AddLoggerRecipe.java
package com.example;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.ClassDeclaration;

public class AddLoggerRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add SLF4J Logger";
    }

    @Override
    public String getDescription() {
        return "Adds a private static final SLF4J Logger field to classes.";
    }



}

