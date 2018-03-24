package net.nhiroki.bluelineconsole.commands.calculator;

import java.math.BigDecimal;

public class CalculatorNumber implements FormulaPart {
    public int getPrecision() {
        return Precision.PRECISION_NULL;
    }

    public BigDecimal getBigDecimal() {
        throw new RuntimeException("This is not a number");
    }

    public String toString() {
        return "...";
    }

    public static class BigDecimalNumber extends CalculatorNumber {
        private final BigDecimal val;
        private final int precision;

        BigDecimalNumber(final BigDecimal val, final int precision) {
            this.val = val;
            this.precision = precision;
        }

        @Override
        public int getPrecision() {
            return precision;
        }

        @Override
        public BigDecimal getBigDecimal() {
            return val;
        }

        @Override
        public String toString() {
            return val.toString();
        }
    }

    public class Precision {
        public static final int PRECISION_NULL = 999999;

        public static final int PRECISION_NO_ERROR = 1;
        public static final int PRECISION_SCALE_20 = 3;
    }
}
