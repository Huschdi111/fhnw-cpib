package ch.fhnw.cpib.platform.scanner;

import ch.fhnw.cpib.platform.scanner.exception.ScannerException;

public class ScannerTest {
    public static void main(String[] args) throws ScannerException {
        Scanner scanner = new Scanner();
        String s = "This file should lex successfully:\n" +
            "Liebe Grossmutter:\n" +
            "\n" +
            "Zu Deinen 67-ten Geburtstag wuensche ich Dir alles Gute,\n" +
            "\n" +
            "Dein Beat\n" +
            "\n" +
            "&?   |?   program++-*endprogram\n" +
            "\n" +
            "whilex==17 do//\n" +
            "  xwhile:=5\n" +
            "\n" +
            "x_'_'17   1000''100'10\n" +
            "\n" +
            "// A Comment\n" +
            "no comment";
        System.out.println(s);
        System.out.println(scanner.scanString(s));

    }
}
