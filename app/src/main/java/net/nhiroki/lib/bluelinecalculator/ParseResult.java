package net.nhiroki.lib.bluelinecalculator;

public class ParseResult {
    private final FormulaPart formulaPart;
    private final int consumedChars;

    ParseResult(final FormulaPart formulaPart, final int consumedChars) {
        this.formulaPart = formulaPart;
        this.consumedChars = consumedChars;
    }

    public FormulaPart getFormulaPart() {
        return this.formulaPart;
    }

    public int getConsumedChars() {
        return this.consumedChars;
    }
}
