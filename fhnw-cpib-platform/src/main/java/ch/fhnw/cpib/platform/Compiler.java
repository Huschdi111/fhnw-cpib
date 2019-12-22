package ch.fhnw.cpib.platform;

import ch.fhnw.cpib.platform.checker.Checker;
import ch.fhnw.cpib.platform.checker.CheckerException;
import ch.fhnw.cpib.platform.generator.Generator;
import ch.fhnw.cpib.platform.generator.GeneratorException;
import ch.fhnw.cpib.platform.parser.Parser;
import ch.fhnw.cpib.platform.parser.abstracttree.AbstractTree;
import ch.fhnw.cpib.platform.parser.concretetree.ConcreteTree;
import ch.fhnw.cpib.platform.parser.exception.ParserException;
import ch.fhnw.cpib.platform.scanner.Scanner;
import ch.fhnw.cpib.platform.scanner.exception.ScannerException;
import ch.fhnw.cpib.platform.scanner.tokens.TokenList;

import java.io.File;

public class Compiler {

    private final Scanner scanner;

    private final Parser parser;

    private final Generator generator;

    public Compiler() {
        this.scanner = new Scanner();
        this.parser = new Parser();
        this.generator = new Generator();
    }

    public void compileString(String content) {
        try {
            // Show the content
            System.out.println("===== Scanning content =====");
            System.out.println(content);
            System.out.println();

            // Scan the source code and show the token list
            System.out.println("===== Scanned token list =====");
            TokenList tokenlist = scanner.scanString(content);
            System.out.println(tokenlist.toString());
            System.out.println();

            // Parse the concrete tree and show it
            System.out.println("===== Concrete parsing tree =====");
            ConcreteTree.Program concreteprogram = parser.parseTokenList(tokenlist);
            System.out.println(concreteprogram);
            System.out.println();

            // Parse the abstract tree and show it
            System.out.println("===== Abstract parsing tree =====");
            AbstractTree.Program abstractprogram = concreteprogram.toAbstract();
            System.out.println(abstractprogram);
            System.out.println();

            // Check the abstract tree
            System.out.println("===== Check abstract tree =====");
            abstractprogram.checkCode(new Checker());
            System.out.println("Done");
            System.out.println();

            // Generate the Java code
            System.out.println("===== Generate Java code =====");
            String javacode = generator.generateJavaCode(abstractprogram);
            System.out.println(javacode);
            System.out.println();

            /*// Generate the Java JAR file
            System.out.println("===== Generate Java JAR file =====");
            File jarfile = generator.generateJarFile(javacode, abstractprogram);
            System.out.println("Done: " + jarfile.getAbsolutePath());
            System.out.println();

            // Execute the Java JAR file
            /*System.out.println("===== Execute Java JAR file =====");
            Pair<String, String> output = generator.executeJarFile(jarfile);
            System.out.println("Regular Output:");
            System.out.println(output.getValue0());
            System.out.println();
            System.out.println("Error Output:");
            System.out.println(output.getValue1());
            System.out.println();*/
        } catch (ScannerException exception) {
            System.out.println("During the scanning process, an error occurred: " + exception.getMessage());
            System.exit(1);
        } catch (ParserException exception) {
            System.out.println("During the parsing process, an error occurred: " + exception.getMessage());
            System.exit(1);
        } catch (CheckerException exception) {
            System.out.println("During the checking process, an error occurred: " + exception.getMessage());
            System.exit(1);
        } catch (GeneratorException exception) {
            System.out.println("During the generation process, an error occurred: " + exception.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {

        /*String content = "program Assoc()\n"
            + "global b:bool; x:int32\n"
            + " do \n"
            + " b init := 2;"
            + " x init := 1;\n"
            + " if b then \n "
            + "     x := x + 1\n"
            + " else x := x + 2\n"
            + " endif \n"
            + "endprogram \n";*/
        String content = "program Assoc()\n"
            + "global b:bool; x:int32\n"
            + " do \n"
            + " b init := 2;"
            + " x init := 1;\n"
            + " guardif\n "
            + "    | x == 2 => x := x + 1\n"
            + "    | x + 1 => x := 2\n"
            + " endguardif \n"
            + "endprogram \n";

        new Compiler().compileString(content);
    }
}
