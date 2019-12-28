package ch.fhnw.cpib.platform.parser.abstracttree;

import ch.fhnw.cpib.platform.javavm.ICodeArray;
import ch.fhnw.cpib.platform.parser.util.Node;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public abstract class AbstractNode extends Node {

    public AbstractNode(int idendation) {
        super(idendation);
    }

    public int generateCode(int loc, boolean isProcedure) throws ICodeArray.CodeTooSmallError {
        throw new RuntimeException("Code generation not implemented yet!");
    }

}
