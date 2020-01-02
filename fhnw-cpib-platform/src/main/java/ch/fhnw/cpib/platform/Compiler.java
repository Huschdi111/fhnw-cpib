package ch.fhnw.cpib.platform;
import ch.fhnw.cpib.platform.checker.Checker;
import ch.fhnw.cpib.platform.checker.CheckerException;
import ch.fhnw.cpib.platform.javavm.CodeArray;
import ch.fhnw.cpib.platform.javavm.ICodeArray;
import ch.fhnw.cpib.platform.javavm.IVirtualMachine;
import ch.fhnw.cpib.platform.javavm.VirtualMachine;
import ch.fhnw.cpib.platform.parser.Parser;
import ch.fhnw.cpib.platform.parser.abstracttree.AbstractTree;
import ch.fhnw.cpib.platform.parser.concretetree.ConcreteTree;
import ch.fhnw.cpib.platform.parser.exception.ParserException;
import ch.fhnw.cpib.platform.scanner.Scanner;
import ch.fhnw.cpib.platform.scanner.exception.ScannerException;
import ch.fhnw.cpib.platform.scanner.tokens.TokenList;

public class Compiler {

    private final Scanner scanner;
    private final Parser parser;

    public Compiler() {
        this.scanner = new Scanner();
        this.parser = new Parser();
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
            abstractprogram.checkCode();
            System.out.println("Done\n\n");

            //Generate Code for javaVM
            System.out.println("===== Generate Code for IML VM =====");
            abstractprogram.generateCode(0);
            System.out.println("Done\n\n");

            //Show Mashine Code
            System.out.println("===== Generated VM Maschine Code =====");
            System.out.println(Checker.getcodeArray());
            System.out.println("Done\n\n");

            //Execute the code array
            System.out.println("===== Execute VM =====");
            ICodeArray codeArray = Checker.getcodeArray();
            System.out.println(codeArray);
            codeArray.resize();
            new VirtualMachine(codeArray, codeArray.getSize());


        } catch (ScannerException exception) {
            System.out.println("During the scanning process, an error occurred: " + exception.getMessage());
            System.exit(1);
        } catch (ParserException exception) {
            System.out.println("During the parsing process, an error occurred: " + exception.getMessage());
            System.exit(1);
        } catch (CheckerException exception) {
            System.out.println("During the checking process, an error occurred: " + exception.getMessage());
            System.exit(1);
        } catch (ICodeArray.CodeTooSmallError exception) {
            System.out.println("During the generation process, an error occurred: " + exception.getMessage());
            System.exit(1);
        } catch (IVirtualMachine.ExecutionError exception) {
            System.out.println("During the execution of the vm an error occurred: " + exception.getMessage());
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
        String content = "program Assoc(in const m:int32)\n"
            + "global \n"
            + "var x:int32 \n"
            + "do \n"
            + "x init := 2;\n"
            + "! x\n"
            + "endprogram \n";

        new Compiler().compileString(content);
    }
}
