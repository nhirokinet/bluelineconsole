package net.nhiroki.bluelineconsole.commands.calculator;

import java.math.BigDecimal;

public interface Operator extends FormulaPart {
    public int getPriority(); // strictly greater than 0

    public interface InfixOperator extends Operator {
        public CalculatorNumber.BigDecimalNumber operate(BigDecimal o1, BigDecimal o2, int targetPrecision)
                throws CalculatorExceptions.PrecisionNotAchievableException, CalculatorExceptions.CalculationException;
    }


    public class AddOperator implements InfixOperator {
        public CalculatorNumber.BigDecimalNumber operate(BigDecimal o1, BigDecimal o2, int targetPrecision) {
            return new CalculatorNumber.BigDecimalNumber(o1.add(o2), targetPrecision);
        }

        public int getPriority() {
            return 10;
        }
    }

    public class SubtractOperator implements InfixOperator {
        public CalculatorNumber.BigDecimalNumber operate(BigDecimal o1, BigDecimal o2, int targetPrecision) {
            return new CalculatorNumber.BigDecimalNumber(o1.subtract(o2),  targetPrecision);
        }

        public int getPriority() {
            return 10;
        }
    }

    public class MultiplyOperator implements InfixOperator {
        public CalculatorNumber.BigDecimalNumber operate(BigDecimal o1, BigDecimal o2, int targetPrecision) {
            return new CalculatorNumber.BigDecimalNumber(o1.multiply(o2), targetPrecision);
        }

        public int getPriority() {
            return 20;
        }
    }

    public class DivideOperator implements InfixOperator {
        public CalculatorNumber.BigDecimalNumber operate(BigDecimal o1, BigDecimal o2, int targetPrecision)
                throws CalculatorExceptions.PrecisionNotAchievableException, CalculatorExceptions.DivisionByZeroException {

            if (o2.compareTo(BigDecimal.ZERO) == 0) {
                throw new CalculatorExceptions.DivisionByZeroException();
            }

            switch (targetPrecision) {
                case CalculatorNumber.Precision.PRECISION_NO_ERROR:
                    try {
                        return new CalculatorNumber.BigDecimalNumber(o1.divide(o2), targetPrecision);
                    } catch (ArithmeticException e) {
                        throw new CalculatorExceptions.PrecisionNotAchievableException();
                    }

                case CalculatorNumber.Precision.PRECISION_SCALE_20:
                    return new CalculatorNumber.BigDecimalNumber(o1.divide(o2, 20, BigDecimal.ROUND_HALF_UP), targetPrecision);


                default:
                    throw new RuntimeException("Unknown precision");
            }
        }

        public int getPriority() {
            return 20;
        }
    }
}
