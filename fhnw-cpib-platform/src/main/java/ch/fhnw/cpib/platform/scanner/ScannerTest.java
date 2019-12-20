package ch.fhnw.cpib.platform.scanner;

import ch.fhnw.cpib.platform.scanner.exception.ScannerException;

public class ScannerTest {
    public static void main(String[] args) throws ScannerException {
        Scanner scanner = new Scanner();
        String s = "program Assoc()\n" +
            "  global x:int32\n" +
            " do \n" +
            "guardif \n" +
            "| x==1 => x := 2 endguardif" +
            "//debugout x divE (2 divE 2) ;\n" +
            "  //debugout (x divE 2) divE 2 ;\n" +
            "  //debugout x divE 2 divE 2\n" +
            "endprogram\n";
        System.out.println(s);
        System.out.println(scanner.scanString(s));

    }
}
