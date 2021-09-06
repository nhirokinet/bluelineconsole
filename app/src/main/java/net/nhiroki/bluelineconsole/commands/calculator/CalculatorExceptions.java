package net.nhiroki.bluelineconsole.commands.calculator;

import net.nhiroki.bluelineconsole.commands.calculator.units.CombinedUnit;

public class CalculatorExceptions {
    public static class PrecisionNotAchievableException extends Exception {}
    public static class IllegalFormulaException extends Exception {}
    public static class CalculationException extends Exception {}
    public static class DivisionByZeroException extends CalculationException {}
    public static class UnitConversionException extends CalculationException {
        private final CombinedUnit from;
        private final CombinedUnit to;

        public UnitConversionException(CombinedUnit from, CombinedUnit to) {
            this.from = (from == null) ? new CombinedUnit() : from;
            this.to = (to == null) ? new CombinedUnit() : to;
        }

        public CombinedUnit getFrom() {
            return from;
        }

        public CombinedUnit getTo() {
            return to;
        }
    }
}
