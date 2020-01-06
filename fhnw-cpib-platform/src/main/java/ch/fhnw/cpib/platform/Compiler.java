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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

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
            codeArray.resize();
            new VirtualMachine(codeArray, codeArray.getSize()); //TODO find the right size for stack


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
            + "global b:bool; x:int32; y:int32\n"
            + " do \n"
            + " y init := 9;"
            + " b init := 1;"
            + " x init := 1;\n"
            + " if b > 0 then \n "
            + "     x := b\n"
            + " else x := x + 2\n"
            + " endif; \n"
            + " ! x\n"
            + "endprogram \n";*/
        /*String content = "program Assoc()\n"
            + "global \n"
            + "var x:int32; \n"
            + "const y:int32 \n"
            + "do \n"
            + "y init := 3;\n"
            + "x init := 2;\n"
            + "while y > 0 do\n"
            + "  y := y - 1;\n"
            + "  ! y\n"
            + "endwhile;\n"
            + "if x == 2 then\n"
            + "? x \n"
            + "endif;\n"
            + "x := (x + y) * 2;"
            + "! x\n"
            + "endprogram \n";*/
        /*String content = "program Assoc()\n"
            + "global \n"
            + "var x:int32; \n"
            + "const y:int32 \n"
            + "do \n"
            + "y init := 2;\n"
            + "x init := 1;\n"
            + "guardif \n"
            + "  | y > 2 => x := x + 4\n"
            + "  | x == 5 => x := x + 2\n"
            + " default \n"
            + "   x := x + 1 \n"
            + "endguardif;\n"
            + "! x\n"
            + "endprogram \n";*/

        try {
            //InputStreamReader source = new InputStreamReader(new FileInputStream("res/code.iml"));
            InputStreamReader source = new InputStreamReader(new FileInputStream("ressources/progParams.iml"));
            BufferedReader reader = new BufferedReader(source);
            String currentLine = "";
            StringBuilder program = new StringBuilder();

            while ((currentLine = reader.readLine()) != null) {
                program.append(currentLine + "\n");
            }

            new Compiler().compileString(program.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
