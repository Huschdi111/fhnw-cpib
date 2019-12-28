package ch.fhnw.cpib.platform.parser.util;

public abstract class Node {

    private final int idendation;

    public Node(int idendation) {
        this.idendation = idendation;
    }

    public int getIdendation() {
        return idendation;
    }

    protected String getHead(String content) {
        return buildString(content, idendation);
    }

    protected String getBody(String content) {
        return buildString(content, idendation + 1);
    }

    private String buildString(String content, int idendation) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < idendation; i++) {
            builder.append('\t');
        }
        builder.append(content);
        builder.append('\n');
        return builder.toString();
    }



    public abstract String toString();
}
