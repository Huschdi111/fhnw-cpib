package ch.fhnw.cpib.platform.checker;

import ch.fhnw.cpib.platform.javavm.ICodeArray;
import ch.fhnw.cpib.platform.javavm.IInstructions.*;
import ch.fhnw.cpib.platform.scanner.tokens.Tokens;

public class Store extends Symbol {

    private boolean writeable;

    private boolean initialized;

    private boolean isConst;

    private boolean relative = false;

    private int address;

    private boolean reference = true;

    public Store(
        final String identifier,
        final Tokens.TypeToken.Type type,
        final boolean isConst) {
        super(identifier, type);
        this.writeable = true;
        this.initialized = false;
        this.isConst = isConst;
    }

    public boolean isConst() {
        return isConst;
    }

    public boolean isWriteable() {
        return writeable;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize() {
        initialized = true;
        if (isConst) {
            writeable = false;
        }
    }

    public void setRelative(final boolean relative) {
        this.relative = relative;
    }

    public void setReference(final boolean reference) {
        this.reference = reference;
    }

    public int codeLoad(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {  //TODO aufl�sen und in codeRef handlen?
        int loc1 = codeRef(loc, true, false, routine);
        //Compiler.getVM().Deref(loc1++);
        Checker.getcodeArray().put(loc1++, new Deref());
        return loc1;
    }

    public int codeRef(final int loc, boolean rel, boolean ref, boolean routine) throws ICodeArray.CodeTooSmallError {
        int loc1 = loc;

        this.setRelative(rel);
        this.setReference(ref);

        if (relative && routine) {
            //Compiler.getVM().LoadRel(loc1++, address);
            if(Checker.getprocIdentTable().get(getIdentifier())[1].equals("COPY")){
                Checker.getcodeArray().put(loc1++, new LoadAddrRel(Integer.parseInt(Checker.getprocIdentTable().get(getIdentifier())[0])));
            }else{
                Checker.getcodeArray().put(loc1++, new LoadAddrRel(Integer.parseInt(Checker.getprocIdentTable().get(getIdentifier())[0])));
                Checker.getcodeArray().put(loc1++, new Deref());
            }


        }else if(relative){
            Checker.getcodeArray().put(loc1++, new LoadAddrRel(Checker.getIdentTable().get(getIdentifier())));
        } else {
            //Compiler.getVM().IntLoad(loc1++, address);
            Checker.getcodeArray().put(loc1++, new LoadImInt(address));
        }

        if (reference) {
            //Compiler.getVM().Deref(loc1++);
            Checker.getcodeArray().put(loc1++, new Deref());
        }

        return loc1;
    }

}
