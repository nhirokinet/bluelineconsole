package net.nhiroki.bluelineconsole.commands.calculator;

import net.nhiroki.bluelineconsole.commands.calculator.units.CombinedUnit;

public class CalculatorExceptions {
    public static class IllegalFormulaException extends Exception {}
    public static class CalculationException extends Exception {
        String error_message = "";
        public CalculationException() {}
        public CalculationException(String error_message) {
            this.error_message = error_message;
        }

        public String toString() {
            return this.getClass().getSimpleName() + ": " + this.error_message;
        }
    }
    public static class DivisionByZeroException extends CalculationException {}
    public static class UnitConversionException extends CalculationException {
        private final CombinedUnit from;
        private final CombinedUnit to;

        public UnitConversionException(CombinedUnit from, CombinedUnit to) {
            this.from = (from == null) ? new CombinedUnit() : from;
            this.to = (to == null) ? new CombinedUnit() : to;
        }

        @Override
        public String toString() {
            return "Failed to convert unit from " + this.from.toString() + " to " + this.to.toString() + ")";
        }

        public CombinedUnit getFrom() {
            return from;
        }

        public CombinedUnit getTo() {
            return to;
        }
    }
}
