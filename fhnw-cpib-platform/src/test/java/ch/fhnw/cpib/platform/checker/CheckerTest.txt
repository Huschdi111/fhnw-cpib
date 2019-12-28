package ch.fhnw.cpib.platform.checker;

import ch.fhnw.cpib.platform.TestFiles;
import ch.fhnw.cpib.platform.parser.Parser;
import ch.fhnw.cpib.platform.parser.abstracttree.AbstractTree;
import ch.fhnw.cpib.platform.parser.concretetree.ConcreteTree;
import ch.fhnw.cpib.platform.scanner.Scanner;
import ch.fhnw.cpib.platform.scanner.tokens.TokenList;
import ch.fhnw.cpib.platform.utils.ReaderUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class CheckerTest {

    @Test
    public void testAbstractParser() throws Exception {
        // Create the scanner and parser
        Scanner scanner = new Scanner();
        Parser parser = new Parser();

        // Parse the files
        for (String filename : TestFiles.checkerfilenames) {
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
            Assert.assertTrue(abstractprogram.toString().length() > 0);

            // Check the tree
            //try {
                abstractprogram.checkCode(new Checker());
            //} catch (Exception e) {
            //    System.out.println(filename);
            //}
        }
    }
}
