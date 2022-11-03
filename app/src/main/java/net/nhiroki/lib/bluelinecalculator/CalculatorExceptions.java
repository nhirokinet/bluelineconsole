package net.nhiroki.lib.bluelinecalculator;

import androidx.annotation.NonNull;

import net.nhiroki.lib.bluelinecalculator.units.CombinedUnit;
import net.nhiroki.lib.bluelinecalculator.units.UnitDirectory;

public class CalculatorExceptions {
    public static class IllegalFormulaException extends Exception {}
    public static class CalculationException extends Exception {
        public CalculationException() {}

        @NonNull
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }
    public static class DivisionByZeroException extends CalculationException {}
    public static class UnitConversionException extends CalculationException {
        private final CombinedUnit from;
        private final CombinedUnit to;

        public UnitConversionException(CombinedUnit from, CombinedUnit to, UnitDirectory unitDirectory) {
            this.from = (from == null) ? new CombinedUnit(unitDirectory) : from;
            this.to = (to == null) ? new CombinedUnit(unitDirectory) : to;
        }

        @NonNull
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
