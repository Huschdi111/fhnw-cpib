package ch.fhnw.cpib.platform.generator;

import ch.fhnw.cpib.platform.TestFiles;
import ch.fhnw.cpib.platform.checker.Checker;
import ch.fhnw.cpib.platform.parser.Parser;
import ch.fhnw.cpib.platform.parser.abstracttree.AbstractTree;
import ch.fhnw.cpib.platform.parser.concretetree.ConcreteTree;
import ch.fhnw.cpib.platform.scanner.Scanner;
import ch.fhnw.cpib.platform.scanner.tokens.TokenList;
import ch.fhnw.cpib.platform.utils.ReaderUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class TestGenerator {

    @Test
    public void testGenerator() throws Exception {
        // Create the scanner, parser and generator
        Scanner scanner = new Scanner();
        Parser parser = new Parser();
        Generator generator = new Generator();

        // Parse the files
        for (String filename : TestFiles.generatorfilenames) {
            // Load the program
            String content = ReaderUtils.getContentFromInputStream(getClass().getResourceAsStream(filename), StandardCharsets.UTF_8);
            Assert.assertFalse(content.isEmpty());

            // Scan the program
            TokenList tokenlist = scanner.scanString(content);
            Assert.assertTrue(tokenlist.getSize() > 0);

            // Parse the token list
            ConcreteTree.Program concreteprogram = parser.parseTokenList(tokenlist);
            Assert.assertTrue(concreteprogram.toString().length() > 0);

            // Make the parse tree abstract
            AbstractTree.Program abstractprogram = concreteprogram.toAbstract();
            //System.out.println(abstractprogram);

            // Check the abstract tree
            abstractprogram.checkCode(new Checker());

            // Generate the Java code
            String javacode = generator.generateJavaCode(abstractprogram);
            Assert.assertTrue(javacode.length() > 0);
            //System.out.println(javacode);

            // Generate the Java JAR file
            File jarfile = generator.generateJarFile(javacode, abstractprogram);
            Assert.assertNotNull(jarfile);

            // Execute the Java JAR file
            /*System.out.println("===== Execute Java JAR file =====");
            Pair<String, String> output = generator.executeJarFile(jarfile);
            System.out.println("Regular Output:");
            System.out.println(output.getValue0());
            System.out.println();
            System.out.println("Error Output:");
            System.out.println(output.getValue1());
            System.out.println();*/
        }
    }
}
