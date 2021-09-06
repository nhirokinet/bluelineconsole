package net.nhiroki.bluelineconsole.commands.calculator;

import android.util.Log;
import android.view.GestureDetector;

import androidx.annotation.NonNull;
import net.nhiroki.bluelineconsole.commands.calculator.units.CombinedUnit;

import java.math.BigDecimal;

public class CalculatorNumber implements FormulaPart {
    public int getPrecision() {
        return Precision.PRECISION_NULL;
    }

    //public BigDecimal getBigDecimal() {
    //    throw new RuntimeException("This is not a number");
    //}

    @NonNull
    public BigDecimalNumber convertUnit(CombinedUnit combinedUnit) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.UnitConversionException {
        throw new RuntimeException("This is not a number");
    }

    // Result is guaranteed to return precision as displayed number, not internal number.
    @NonNull
    public BigDecimalNumber generateFinalDecimalValue() {
        throw new RuntimeException("This is not a number");
    }

    @NonNull
    public String generateFinalString() {
        return "...";
    }

    public static class BigDecimalNumber extends CalculatorNumber {
        public static final BigDecimalNumber ONE = new CalculatorNumber.BigDecimalNumber(BigDecimal.ONE, CalculatorNumber.Precision.PRECISION_NO_ERROR, null);

        private final BigDecimal val;
        private final BigDecimal denominator;
        private final int precision;
        private final CombinedUnit combinedUnit;

        public BigDecimalNumber(final String val) {
            this.val = new BigDecimal(val);
            this.denominator = BigDecimal.ONE;
            this.precision = Precision.PRECISION_NO_ERROR;
            this.combinedUnit = null;
        }

        private BigDecimalNumber(final BigDecimal val, final int precision) {
            this.val = val;
            this.denominator = BigDecimal.ONE;
            this.precision = precision;
            this.combinedUnit = null;
        }

        public BigDecimalNumber(final BigDecimal val, final int precision, final CombinedUnit combinedUnit) {
            this.val = val;
            this.denominator = BigDecimal.ONE;
            this.precision = precision;
            this.combinedUnit = combinedUnit;
        }

        private BigDecimalNumber(final BigDecimal val, final BigDecimal denominator, final int precision, final CombinedUnit combinedUnit) {
            this.val = val;
            this.denominator = denominator;
            this.precision = precision;
            this.combinedUnit = combinedUnit;
        }

        @Override
        public int getPrecision() {
            return precision;
        }

        private static BigDecimal normalizeBigDecimal(final BigDecimal in, final int precision) {
            if (in.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            BigDecimal ret = in.stripTrailingZeros();
            if (precision == CalculatorNumber.Precision.PRECISION_NO_ERROR && ret.scale() < 0) {
                //noinspection BigDecimalMethodWithoutRoundingCalled
                ret = ret.setScale(0);
            }

            return ret;
        }

        @NonNull
        public BigDecimalNumber generateFinalDecimalValue() {
            BigDecimalNumber displayedResult = this;
            if (this.combinedUnit != null) {
                try {
                    displayedResult = displayedResult.multiply(displayedResult.combinedUnit.calculateRatioToUnifyUnitInEachDimension());
                } catch (CalculatorExceptions.IllegalFormulaException e) {
                    throw new RuntimeException("Unit unifying failure");
                }
            }
            try {
                //noinspection BigDecimalMethodWithoutRoundingCalled
                return new CalculatorNumber.BigDecimalNumber(displayedResult.val.divide(displayedResult.denominator), displayedResult.precision, displayedResult.combinedUnit);
            } catch (ArithmeticException e) {
                // continue to precisioned error
            }

            return new CalculatorNumber.BigDecimalNumber(displayedResult.val.divide(displayedResult.denominator, 20, BigDecimal.ROUND_HALF_UP), Precision.calculateLowerPrecision(displayedResult.precision, Precision.PRECISION_SCALE_20), displayedResult.combinedUnit);
        }

        @Override
        @NonNull
        public String toString() {
            return this.val + "/" + this.denominator + " " + ((this.combinedUnit == null) ? "No Unit" : this.combinedUnit.toString()) + " with Precision " + this.precision;
        }

        @Override
        @NonNull
        public String generateFinalString() {
            String suffix = "";
            if (this.denominator.compareTo(BigDecimal.ONE) != 0) {
                suffix = "/" + this.denominator.toString();
            }
            if (this.combinedUnit == null) {
                return normalizeBigDecimal(this.val, this.precision).toString() + suffix;
            } else {
                final String unitName = this.combinedUnit.calculateDisplayName();
                return normalizeBigDecimal(this.val, this.precision).toString() + suffix + (unitName.isEmpty() ? "" : " ") + unitName;
            }
        }

        @NonNull
        public BigDecimalNumber convertUnit(CombinedUnit combinedUnit) throws CalculatorExceptions.UnitConversionException {
            if (this.combinedUnit == null) {
                if (combinedUnit == null || combinedUnit.dimensionEquals(null)) {
                    try {
                        return this.divide(combinedUnit.calculateRatioAgainst(null));
                    } catch (CalculatorExceptions.DivisionByZeroException e) {
                        throw new RuntimeException("Zero division in convertUnit");
                    }
                }
                throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, combinedUnit);
            }
            if (! this.combinedUnit.dimensionEquals(combinedUnit)) {
                throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, combinedUnit);
            }
            return this.multiply(this.combinedUnit.calculateRatioAgainst(combinedUnit));
        }

        @NonNull
        public BigDecimalNumber add(BigDecimalNumber o) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.UnitConversionException {
            if (this.combinedUnit == null && o.combinedUnit == null) {
                return new BigDecimalNumber(this.val.multiply(o.denominator).add(o.val.multiply(this.denominator)), this.denominator.multiply(o.denominator), precision, null);
            }
            if (this.combinedUnit == null || o.combinedUnit == null) {
                if (this.combinedUnit == null && ! o.combinedUnit.dimensionEquals(null)) {
                    throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, o.combinedUnit);
                }
                if (! this.combinedUnit.dimensionEquals(null)) {
                    throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, o.combinedUnit);
                }
            }
            o = o.convertUnit(this.combinedUnit);
            return new BigDecimalNumber(this.val.multiply(o.denominator).add(o.val.multiply(this.denominator)), this.denominator.multiply(o.denominator), Precision.calculateLowerPrecision(this.precision, o.precision), this.combinedUnit);
        }

        @NonNull
        public BigDecimalNumber subtract(BigDecimalNumber o) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.UnitConversionException {
            if (this.combinedUnit == null && o.combinedUnit == null) {
                return new BigDecimalNumber(this.val.multiply(o.denominator).subtract(o.val.multiply(this.denominator)), this.denominator.multiply(o.denominator), precision, null);
            }
            if (this.combinedUnit == null || o.combinedUnit == null) {
                if (this.combinedUnit == null && ! o.combinedUnit.equals(null)) {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
                if (! this.combinedUnit.equals(null)) {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
            } else if (! this.combinedUnit.equals(o.combinedUnit)) {
                o = o.convertUnit(this.combinedUnit);
            }
            return new BigDecimalNumber(this.val.multiply(o.denominator).subtract(o.val.multiply(this.denominator)), this.denominator.multiply(o.denominator), Precision.calculateLowerPrecision(this.precision, o.precision), this.combinedUnit);
        }

        @NonNull
        public BigDecimalNumber multiply(final BigDecimalNumber o) {
            CombinedUnit resultUnit = null;

            if (this.combinedUnit != null || o.combinedUnit != null) {
                if (this.combinedUnit == null) {
                    resultUnit = new CombinedUnit();
                } else {
                    resultUnit = this.combinedUnit;
                }
                resultUnit = resultUnit.multiply(o.combinedUnit);
            }

            return new CalculatorNumber.BigDecimalNumber(this.val.multiply(o.val), this.denominator.multiply(o.denominator), Precision.calculateLowerPrecision(this.precision, o.precision), resultUnit);
        }

        @NonNull
        public BigDecimalNumber divide(final BigDecimalNumber o) throws CalculatorExceptions.DivisionByZeroException {
            CombinedUnit resultUnit = null;

            if (this.combinedUnit != null || o.combinedUnit != null) {
                if (this.combinedUnit == null) {
                    resultUnit = new CombinedUnit();
                } else {
                    resultUnit = this.combinedUnit;
                }
                resultUnit = resultUnit.divide(o.combinedUnit);
            }

            if (o.val.compareTo(BigDecimal.ZERO) == 0) {
                throw new CalculatorExceptions.DivisionByZeroException();
            }

            return new CalculatorNumber.BigDecimalNumber(this.val.multiply(o.denominator), this.denominator.multiply(o.val), Precision.calculateLowerPrecision(this.precision, o.precision), resultUnit);
        }
    }

    public static class Precision {
        public static final int PRECISION_NULL = 999999;

        public static final int PRECISION_NO_ERROR = 1;
        public static final int PRECISION_SCALE_20 = 3;

        public static int calculateLowerPrecision(int precision1, int precision2) {
            return Math.max(precision1, precision2);
        }
        public static int calculateLowerPrecision(int precision1, int precision2, int precision3) {
            return Math.max(precision1, Math.max(precision2, precision3));
        }
    }
}
