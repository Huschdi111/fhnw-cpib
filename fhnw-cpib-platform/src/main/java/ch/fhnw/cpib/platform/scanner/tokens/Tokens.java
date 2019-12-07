package ch.fhnw.cpib.platform.scanner.tokens;

public class Tokens {

    public static class Token implements Cloneable {

        private final Terminal terminal;

        private int row;

        private int column;

        public Token(Terminal terminal) {
            this.terminal = terminal;
        }

        public final Terminal getTerminal() {
            return terminal;
        }

        public String toString() {
            return terminal.toString();
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public static class IdentifierToken extends Token {

        private final String name;

        public IdentifierToken(String name, Terminal terminal) {
            super(terminal);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "(" + super.toString() + "," + name + ")";
        }
    }

    public static class LiteralToken extends Token {

        private final String value;

        private final TypeToken.Type type;

        public LiteralToken(String value, Terminal terminal) {
            super(terminal);
            try {
                long v = Long.parseLong(value);
                if (v > Integer.MAX_VALUE && v < Long.MAX_VALUE) {
                    type = TypeToken.Type.INT64;
                } else if (v < Integer.MAX_VALUE && v > Integer.MIN_VALUE) {
                    type = TypeToken.Type.INT;
                } else if ("true".equals(value) || "false".equals(value)) {
                    type = TypeToken.Type.BOOL;
                } else {
                    type = null;
                }
            } catch (RuntimeException e) {
                throw new RuntimeException("Invalid number " + value);
            }
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public Tokens.TypeToken.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return "(" + super.toString() + "," + value + ")";
        }
    }

    public static class ChangeModeToken extends Token {

        public enum ChangeMode {
            VAR,
            CONST
        }

        private final ChangeMode changemode;

        public ChangeModeToken(Terminal terminal, ChangeMode changemode) {
            super(terminal);
            this.changemode = changemode;
        }

        public ChangeMode getChangeMode() {
            return changemode;
        }

        @Override
        public String toString() {
            return "(" + getTerminal() + "," + changemode + ")";
        }
    }

    public static class FlowModeToken extends Token {

        public enum FlowMode {
            IN,
            INOUT,
            OUT
        }

        private final FlowMode flowmode;

        public FlowModeToken(Terminal terminal, FlowMode flowmode) {
            super(terminal);
            this.flowmode = flowmode;
        }

        public FlowMode getFlowMode() {
            return flowmode;
        }

        @Override
        public String toString() {
            return "(" + getTerminal() + "," + flowmode + ")";
        }
    }

    public static class MechModeToken extends Token {

        public enum MechMode {
            COPY,
            REF
        }

        private final MechMode mechmode;

        public MechModeToken(Terminal terminal, MechMode mechmode) {
            super(terminal);
            this.mechmode = mechmode;
        }

        public MechMode getMechMode() {
            return mechmode;
        }

        @Override
        public String toString() {
            return "(" + getTerminal() + "," + mechmode + ")";
        }
    }

    public static abstract class OperationToken extends Token {

        public enum Operation {
            PLUS,
            MINUS,
            AND,
            OR,
            CAND,
            COR,
            TIMES,
            DIVE,
            MODE,
            EQ,
            NE,
            LT,
            GT,
            LE,
            GE
        }

        private final Operation operation;

        public OperationToken(Terminal terminal, Operation operation) {
            super(terminal);
            this.operation = operation;
        }

        public Operation getOperation() {
            return operation;
        }

        @Override
        public String toString() {
            return "(" + super.toString() + "," + getOperation() + ")";
        }
    }

    public static class AddOprToken extends OperationToken {

        public enum AddOpr {
            PLUS,
            MINUS
        }

        public AddOprToken(Terminal terminal, AddOpr addopr) {
            super(terminal, convertOperation(addopr));
        }

        private static OperationToken.Operation convertOperation(AddOpr addopr) {
            if (addopr == AddOpr.PLUS) {
                return OperationToken.Operation.PLUS;
            } else {
                return OperationToken.Operation.MINUS;
            }
        }
    }

    public static class BoolOprToken extends OperationToken {

        public enum BoolOpr {
            AND,
            OR,
            CAND,
            COR
        }


        public BoolOprToken(Terminal terminal, BoolOpr boolopr) {
            super(terminal, convertOperation(boolopr));
        }

        private static OperationToken.Operation convertOperation(BoolOpr boolopr) {
            if (boolopr == BoolOpr.AND) {
                return Operation.AND;
            } else if (boolopr == BoolOpr.OR) {
                return Operation.OR;
            } else if (boolopr == BoolOpr.CAND) {
                return Operation.CAND;
            } else {
                return Operation.COR;
            }
        }
    }

    public static class MultOprToken extends OperationToken {

        public enum MultOpr {
            TIMES,
            DIVE,
            MODE
        }

        public MultOprToken(Terminal terminal, MultOpr multopr) {
            super(terminal, convertOperation(multopr));
        }

        private static OperationToken.Operation convertOperation(MultOpr multopr) {
            if (multopr == MultOpr.TIMES) {
                return Operation.TIMES;
            } else if (multopr == MultOpr.DIVE) {
                return Operation.DIVE;
            } else {
                return Operation.MODE;
            }
        }
    }

    public static class RelOprToken extends OperationToken {

        public enum RelOpr {
            EQ,
            NE,
            LT,
            GT,
            LE,
            GE
        }

        public RelOprToken(Terminal terminal, RelOpr relopr) {
            super(terminal, convertOperation(relopr));
        }

        private static OperationToken.Operation convertOperation(RelOpr relopr) {
            if (relopr == RelOpr.EQ) {
                return Operation.EQ;
            } else if (relopr == RelOpr.NE) {
                return Operation.NE;
            } else if (relopr == RelOpr.LT) {
                return Operation.LT;
            } else if (relopr == RelOpr.GT) {
                return Operation.GT;
            } else if (relopr == RelOpr.LE) {
                return Operation.LE;
            } else {
                return Operation.GE;
            }
        }
    }

    public static class TypeToken extends Token {

        public enum Type {
            BOOL,
            INT,
            INT64
        }

        private final Type type;

        public TypeToken(Terminal terminal, Type type) {
            super(terminal);
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        public String toString() {
            return "(" + getTerminal() + "," + type + ")";
        }
    }

    public static class SentinelToken extends Token {
        public SentinelToken(Terminal terminal) {
            super(terminal);
        }
    }
}
