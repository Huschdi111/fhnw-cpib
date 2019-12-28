package ch.fhnw.cpib.platform.parser.abstracttree;

import ch.fhnw.cpib.platform.generator.GeneratorException;
import ch.fhnw.cpib.platform.parser.util.Node;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public abstract class AbstractNode extends Node {

    public AbstractNode(int idendation) {
        super(idendation);
    }

    /*public void generateCode(MethodSpec.Builder methodscpecbuilder) {
        throw new RuntimeException("Code generation not implemented yet!");
    }*/

    public int generateCode(int loc) throws GeneratorException {
        throw new RuntimeException("Code generation not implemented yet!");
    }
}
