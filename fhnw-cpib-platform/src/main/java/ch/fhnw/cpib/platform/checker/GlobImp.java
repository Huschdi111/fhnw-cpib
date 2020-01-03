package ch.fhnw.cpib.platform.checker;

import ch.fhnw.cpib.platform.scanner.tokens.Tokens;

public final class GlobImp {
    private final Tokens.ChangeModeToken.ChangeMode changeMode;
    private final String ident;

    public String getIdent() {
        return ident;
    }

    public GlobImp(final Tokens.ChangeModeToken.ChangeMode changeMode, final String ident) {
        this.changeMode = changeMode;
        this.ident = ident;
    }
}
