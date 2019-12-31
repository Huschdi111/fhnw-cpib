package ch.fhnw.cpib.platform.checker;

import ch.fhnw.cpib.platform.scanner.tokens.Tokens;

public class ExpressionInfo {

    private String name;

    private Tokens.TypeToken.Type type;

    public Tokens.MechModeToken.MechMode getMechMode() {
        return mechMode;
    }

    public void setMechMode(Tokens.MechModeToken.MechMode mechMode) {
        this.mechMode = mechMode;
    }

    private Tokens.MechModeToken.MechMode mechMode;

    public ExpressionInfo(String name, Tokens.TypeToken.Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Tokens.TypeToken.Type getType() {
        return type;
    }
}
