package net.nhiroki.bluelineconsole.commands.calculator;

public class CalculatorExceptions {
    public static class PrecisionNotAchievableException extends Exception {}
    public static class IllegalFormulaException extends Exception {}
    public static class CalculationException extends Exception {}
    public static class DivisionByZeroException extends CalculationException {}
}
