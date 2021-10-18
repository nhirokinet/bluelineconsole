package net.nhiroki.bluelineconsole.commands.calculator;

public interface Operator extends FormulaPart {
    int getPriority(); // strictly greater than 0

    interface InfixOperator extends Operator {
        CalculatorNumber.BigDecimalNumber operate(CalculatorNumber.BigDecimalNumber o1, CalculatorNumber.BigDecimalNumber o2)
                throws CalculatorExceptions.CalculationException, CalculatorExceptions.IllegalFormulaException;
    }


    // Canonicalize
    class AddOperator implements InfixOperator {
        public CalculatorNumber.BigDecimalNumber operate(CalculatorNumber.BigDecimalNumber o1, CalculatorNumber.BigDecimalNumber o2) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.UnitConversionException {
            return o1.add(o2);
        }

        public int getPriority() {
            return 10;
        }
    }

    class SubtractOperator implements InfixOperator {
        public CalculatorNumber.BigDecimalNumber operate(CalculatorNumber.BigDecimalNumber o1, CalculatorNumber.BigDecimalNumber o2) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.UnitConversionException {
            return o1.subtract(o2);
        }

        public int getPriority() {
            return 10;
        }
    }

    class MultiplyOperator implements InfixOperator {
        public CalculatorNumber.BigDecimalNumber operate(CalculatorNumber.BigDecimalNumber o1, CalculatorNumber.BigDecimalNumber o2) throws CalculatorExceptions.UnitConversionException, CalculatorExceptions.IllegalFormulaException {
            return o1.multiply(o2);
        }

        public int getPriority() {
            return 20;
        }
    }

    class DivideOperator implements InfixOperator {
        public CalculatorNumber.BigDecimalNumber operate(CalculatorNumber.BigDecimalNumber o1, CalculatorNumber.BigDecimalNumber o2) throws CalculatorExceptions.DivisionByZeroException, CalculatorExceptions.UnitConversionException, CalculatorExceptions.IllegalFormulaException {
            return o1.divide(o2);
        }

        public int getPriority() {
            return 20;
        }
    }
}
