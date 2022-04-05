package net.nhiroki.lib.bluelinecalculator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.nhiroki.lib.bluelinecalculator.units.CombinedUnit;
import net.nhiroki.lib.bluelinecalculator.units.UnitDirectory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculatorNumber implements FormulaPart {
    public int getPrecision() {
        return Precision.PRECISION_NULL;
    }

    @NonNull
    public BigDecimalNumber convertUnit(CombinedUnit combinedUnit) throws CalculatorExceptions.UnitConversionException {
        throw new RuntimeException("This is not a number");
    }

    // Result outputs final output String in decimal..
    @NonNull
    public BigDecimalNumber generateFinalDecimalValue() {
        throw new RuntimeException("This is not a number");
    }

    @Nullable
    public BigDecimalNumber generatePossiblyPreferredOutputValue() {
        return null;
    }

    @NonNull
    public String generateFinalString() {
        return "...";
    }

    public static class

    BigDecimalNumber extends CalculatorNumber {
        public static BigDecimalNumber one(UnitDirectory unitDirectory) {
            return new CalculatorNumber.BigDecimalNumber(BigDecimal.ONE, CalculatorNumber.Precision.PRECISION_NO_ERROR, null, unitDirectory);
        }

        private final BigDecimal val;
        private final BigDecimal denominator;
        private final int precision;
        private CombinedUnit combinedUnit; // this changes output, be careful to modify
        private boolean specialOutputForSpecialTime = false;  // this changes output, be careful to modify
        private UnitDirectory unitDirectory;

        public BigDecimalNumber(final BigDecimalNumber copiedFrom, UnitDirectory unitDirectory) {
            this.val = copiedFrom.val;
            this.denominator = copiedFrom.denominator;
            this.precision = copiedFrom.precision;
            this.combinedUnit = copiedFrom.combinedUnit;
            this.specialOutputForSpecialTime = copiedFrom.specialOutputForSpecialTime;
            this.unitDirectory = unitDirectory;
        }

        public BigDecimalNumber(final String val, UnitDirectory unitDirectory) {
            this.val = new BigDecimal(val);
            this.denominator = BigDecimal.ONE;
            this.precision = Precision.PRECISION_NO_ERROR;
            this.combinedUnit = null;
            this.unitDirectory = unitDirectory;
        }

        public BigDecimalNumber(final BigDecimal val, final int precision, final CombinedUnit combinedUnit, UnitDirectory unitDirectory) {
            this.val = val;
            this.denominator = BigDecimal.ONE;
            this.precision = precision;
            this.combinedUnit = combinedUnit;
            this.unitDirectory = unitDirectory;
        }

        private BigDecimalNumber(final BigDecimal val, final BigDecimal denominator, final int precision, final CombinedUnit combinedUnit, UnitDirectory unitDirectory) {
            this.val = val;
            this.denominator = denominator;
            this.precision = precision;
            this.combinedUnit = combinedUnit;
            this.unitDirectory = unitDirectory;
        }

        @Override
        public int getPrecision() {
            return precision;
        }

        public CombinedUnit getCombinedUnit() {
            return this.combinedUnit;
        }

        @NonNull
        public BigDecimalNumber removeCombinedUnit() {
            return new BigDecimalNumber(this.val, this.denominator, this.precision, null, this.unitDirectory);
        }

        @NonNull
        public BigDecimalNumber applyCombinedUnit(CombinedUnit combinedUnit) throws CalculatorExceptions.IllegalFormulaException {
            if (this.combinedUnit != null && !this.combinedUnit.equals(null)) {
                throw new CalculatorExceptions.IllegalFormulaException();
            }
            return new BigDecimalNumber(this.val, this.denominator, this.precision, combinedUnit, this.unitDirectory);
        }

        @NonNull
        public BigDecimalNumber applyMinusJustToNumber() {
            return new BigDecimalNumber(this.val.multiply(new BigDecimal("-1")), this.denominator, this.precision, this.combinedUnit, this.unitDirectory);
        }

        @NonNull
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

        private CombinedUnit finalUnit() {
            BigDecimalNumber displayedResult = this;

            if (this.combinedUnit != null && this.combinedUnit.isCalculatable()) {
                try {
                    displayedResult = displayedResult.multiply(displayedResult.combinedUnit.calculateRatioToUnifyUnitInEachDimension());
                    CombinedUnit shouldConvertTo = this.unitDirectory.getShouldConvertFrom(displayedResult.combinedUnit);
                    if (shouldConvertTo != null) {
                        displayedResult = displayedResult.convertUnit(shouldConvertTo);
                    }
                } catch (CalculatorExceptions.IllegalFormulaException | CalculatorExceptions.UnitConversionException e) {
                    throw new RuntimeException("Unit unifying failure");
                }
            }

            return displayedResult.combinedUnit;
        }

        @NonNull
        public BigDecimalNumber generateFinalDecimalValue() {
            BigDecimalNumber displayedResult = this;
            if (this.combinedUnit != null && this.combinedUnit.isCalculatable()) {
                try {
                    displayedResult = displayedResult.convertUnit(this.finalUnit());
                } catch (CalculatorExceptions.UnitConversionException e) {
                    throw new RuntimeException("Unit unifying failure");
                }
            }
            try {
                //noinspection BigDecimalMethodWithoutRoundingCalled
                return new CalculatorNumber.BigDecimalNumber(displayedResult.val.divide(displayedResult.denominator), displayedResult.precision, displayedResult.combinedUnit, this.unitDirectory);
            } catch (ArithmeticException e) {
                // continue to precision error
            }

            return new CalculatorNumber.BigDecimalNumber(displayedResult.val.divide(displayedResult.denominator, 20, BigDecimal.ROUND_HALF_UP), Precision.calculateLowerPrecision(displayedResult.precision, Precision.PRECISION_SCALE_20), displayedResult.combinedUnit, this.unitDirectory);
        }

        @Nullable
        public BigDecimalNumber generatePossiblyPreferredOutputValue() {
            if (this.combinedUnit == null) {
                return null;
            }

            try {
                BigDecimalNumber ret = this.convertUnit(new CombinedUnit(this.unitDirectory.getSecond(), this.unitDirectory)).generateFinalDecimalValue();

                if (ret.val.abs().compareTo(BigDecimal.ONE) < 0) {
                    return null;
                }
                ret.specialOutputForSpecialTime = true;
                return ret;

            } catch (CalculatorExceptions.UnitConversionException e) {
                // Continue, just this was not time
            }

            List<CombinedUnit> candidates = this.unitDirectory.getPreferredCombinedUnits(this.combinedUnit);
            if (! candidates.isEmpty()) {
                for (CombinedUnit c: candidates) {
                    if (c.equals(this.finalUnit())) {
                        return null;
                    }
                }
                BigDecimalNumber previousOutput = null;

                for (CombinedUnit c: candidates) {
                    try {
                        BigDecimalNumber output = this.convertUnit(c);
                        if (output.removeCombinedUnit().compareTo(BigDecimalNumber.one(this.unitDirectory)) == -1) {
                            return previousOutput != null ? previousOutput.generateFinalDecimalValue() : output.generateFinalDecimalValue();
                        }
                        previousOutput = output;
                    } catch (CalculatorExceptions.UnitConversionException | CalculatorExceptions.IllegalFormulaException e) {
                        continue;
                    }
                }

                return previousOutput.generateFinalDecimalValue();
            }

            return null;
        }

        @Override
        @NonNull
        public String toString() {
            return this.val + "/" + this.denominator + " " + ((this.combinedUnit == null) ? "No Unit" : this.combinedUnit.toString()) + " with Precision " + this.precision;
        }

        public int compareTo(BigDecimalNumber o) throws CalculatorExceptions.UnitConversionException, CalculatorExceptions.IllegalFormulaException {
            BigDecimalNumber diff = this.subtract(o);
            return diff.val.signum() * diff.denominator.signum();
        }

        @Override
        @NonNull
        public String generateFinalString() {
            String suffix = "";
            if (this.denominator.compareTo(BigDecimal.ONE) != 0) {
                suffix = "/" + this.denominator.toString();
            }

            if (this.specialOutputForSpecialTime) {
                if (! this.combinedUnit.equals(new CombinedUnit(this.unitDirectory.getSecond(), this.unitDirectory))) {
                    throw new RuntimeException("specialOutputForTime enabled, but this is not second");
                }

                BigDecimal sixty = new BigDecimal(60);

                BigDecimal positiveVal = this.val;
                String prefix = "";

                if (positiveVal.compareTo(BigDecimal.ZERO) < 0) {
                    positiveVal = this.val.multiply(new BigDecimal("-1"));
                    prefix = "-";
                }

                BigDecimal second = BigDecimalNumber.normalizeBigDecimal(positiveVal.remainder(BigDecimal.TEN), this.precision);
                int tenSecond = this.val.remainder(sixty).divide(BigDecimal.TEN, 0, BigDecimal.ROUND_FLOOR).intValueExact();
                BigDecimal minuteTotal = positiveVal.divide(sixty, 0, BigDecimal.ROUND_FLOOR);
                BigDecimal hour = minuteTotal.divide(sixty, 0, BigDecimal.ROUND_FLOOR);
                int minute = minuteTotal.remainder(sixty).intValueExact();

                if (hour.compareTo(new BigDecimal(24)) < 0) {
                    return prefix + hour.toPlainString() + ":" + (minute < 10 ? "0" : "") + minute + ":" + tenSecond + second.toPlainString() + suffix;
                } else {
                    int hourRemainder = hour.remainder(new BigDecimal(24)).intValueExact();
                    BigDecimal days = hour.divide(new BigDecimal(24), 0, RoundingMode.FLOOR);
                    return prefix + days + "d " + (hourRemainder < 10 ? "0" : "") + hourRemainder + ":" + (minute < 10 ? "0" : "") + minute + ":" + tenSecond + second.toPlainString() + suffix;
                }
            }

            if (this.combinedUnit == null) {
                return normalizeBigDecimal(this.val, this.precision).toString() + suffix;
            } else {
                final String unitName = this.combinedUnit.calculateDisplayName();
                return normalizeBigDecimal(this.val, this.precision).toString() + suffix + (unitName.isEmpty() ? "" : " ") + unitName;
            }
        }

        @NonNull BigDecimalNumber makeUnitsExplicit() {
            BigDecimalNumber ret = new BigDecimalNumber(this, this.unitDirectory);
            ret.combinedUnit = ret.combinedUnit.explicitCombinedUnit();
            return ret;
        }

        @NonNull
        public BigDecimalNumber convertUnit(CombinedUnit combinedUnit) throws CalculatorExceptions.UnitConversionException {
            if (this.combinedUnit == null) {
                if (combinedUnit == null || combinedUnit.dimensionEquals(null)) {
                    try {
                        return this.divide(combinedUnit.calculateRatioAgainst(null));
                    } catch (CalculatorExceptions.DivisionByZeroException e) {
                        throw new RuntimeException("Zero division in convertUnit");
                    } catch (CalculatorExceptions.IllegalFormulaException e) {
                        throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, combinedUnit, unitDirectory);
                    }
                }
                throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, combinedUnit, unitDirectory);
            }
            if (! this.combinedUnit.isCalculatable()) {
                return this.combinedUnit.makeCalculatableFromThisUnit(this).convertUnit(combinedUnit).makeUnitsExplicit();
            }
            if (combinedUnit.isCalculatable()) {
                if (! this.combinedUnit.dimensionEquals(combinedUnit)) {
                    throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, combinedUnit, unitDirectory);
                }
                try {
                    return this.multiply(this.combinedUnit.calculateRatioAgainst(combinedUnit)).makeUnitsExplicit();
                } catch (CalculatorExceptions.IllegalFormulaException e) {
                    throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, combinedUnit, unitDirectory);
                }
            }

            return combinedUnit.makeThisUnitFromCalculatable(this);
        }

        @NonNull
        public BigDecimalNumber add(BigDecimalNumber o) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.UnitConversionException {
            if (this.combinedUnit == null && o.combinedUnit == null) {
                return new BigDecimalNumber(this.val.multiply(o.denominator).add(o.val.multiply(this.denominator)), this.denominator.multiply(o.denominator), precision, null, this.unitDirectory);
            }
            if (this.combinedUnit == null || o.combinedUnit == null) {
                if (this.combinedUnit == null && ! o.combinedUnit.dimensionEquals(null)) {
                    throw new CalculatorExceptions.UnitConversionException(null, o.combinedUnit, unitDirectory);
                }
                if (! this.combinedUnit.dimensionEquals(null)) {
                    throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, o.combinedUnit, unitDirectory);
                }
            }
            if (! this.combinedUnit.isCalculatable()) {
                return this.combinedUnit.makeCalculatableFromThisUnit(this).add(o);
            }
            if (o.combinedUnit != null && ! o.combinedUnit.isCalculatable()) {
                o = o.combinedUnit.makeCalculatableFromThisUnit(o);
            }
            o = o.convertUnit(this.combinedUnit);
            return new BigDecimalNumber(this.val.multiply(o.denominator).add(o.val.multiply(this.denominator)), this.denominator.multiply(o.denominator), Precision.calculateLowerPrecision(this.precision, o.precision), this.combinedUnit, this.unitDirectory);
        }

        @NonNull
        public BigDecimalNumber subtract(BigDecimalNumber o) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.UnitConversionException {
            if (this.combinedUnit == null && o.combinedUnit == null) {
                return new BigDecimalNumber(this.val.multiply(o.denominator).subtract(o.val.multiply(this.denominator)), this.denominator.multiply(o.denominator), precision, null, this.unitDirectory);
            }
            if (this.combinedUnit == null || o.combinedUnit == null) {
                if (this.combinedUnit == null && ! o.combinedUnit.dimensionEquals(null)) {
                    throw new CalculatorExceptions.UnitConversionException(null, o.combinedUnit, unitDirectory);
                }
                if (! this.combinedUnit.dimensionEquals(null)) {
                    throw new CalculatorExceptions.UnitConversionException(this.combinedUnit, o.combinedUnit, unitDirectory);
                }
            }
            if (! this.combinedUnit.isCalculatable()) {
                return this.combinedUnit.makeCalculatableFromThisUnit(this).subtract(o);
            }
            if (o.combinedUnit != null && ! o.combinedUnit.isCalculatable()) {
                o = o.combinedUnit.makeCalculatableFromThisUnit(o);
            }
            o = o.convertUnit(this.combinedUnit);

            return new BigDecimalNumber(this.val.multiply(o.denominator).subtract(o.val.multiply(this.denominator)), this.denominator.multiply(o.denominator), Precision.calculateLowerPrecision(this.precision, o.precision), this.combinedUnit, this.unitDirectory);
        }

        @NonNull
        public BigDecimalNumber multiply(BigDecimalNumber o) throws CalculatorExceptions.UnitConversionException, CalculatorExceptions.IllegalFormulaException {
            CombinedUnit resultUnit = null;

            if (this.combinedUnit != null || o.combinedUnit != null) {
                if (this.combinedUnit != null && ! this.combinedUnit.isCalculatable()) {
                    return this.combinedUnit.makeCalculatableFromThisUnit(this).multiply(o);
                }
                if (o.combinedUnit != null && ! o.combinedUnit.isCalculatable()) {
                    o = o.combinedUnit.makeCalculatableFromThisUnit(o);
                }
                if (this.combinedUnit == null) {
                    resultUnit = new CombinedUnit(this.unitDirectory);
                } else {
                    resultUnit = this.combinedUnit;
                }
                resultUnit = resultUnit.multiply(o.combinedUnit);
            }

            return new CalculatorNumber.BigDecimalNumber(this.val.multiply(o.val), this.denominator.multiply(o.denominator), Precision.calculateLowerPrecision(this.precision, o.precision), resultUnit, this.unitDirectory);
        }

        @NonNull
        public BigDecimalNumber divide(BigDecimalNumber o) throws CalculatorExceptions.DivisionByZeroException, CalculatorExceptions.UnitConversionException, CalculatorExceptions.IllegalFormulaException {
            CombinedUnit resultUnit = null;

            if (this.combinedUnit != null || o.combinedUnit != null) {
                if (this.combinedUnit != null && ! this.combinedUnit.isCalculatable()) {
                    return this.combinedUnit.makeCalculatableFromThisUnit(this).divide(o);
                }
                if (o.combinedUnit != null && ! o.combinedUnit.isCalculatable()) {
                    o = o.combinedUnit.makeCalculatableFromThisUnit(o);
                }

                if (this.combinedUnit == null) {
                    resultUnit = new CombinedUnit(this.unitDirectory);
                } else {
                    resultUnit = this.combinedUnit;
                }
                resultUnit = resultUnit.divide(o.combinedUnit);
            }

            if (o.val.compareTo(BigDecimal.ZERO) == 0) {
                throw new CalculatorExceptions.DivisionByZeroException();
            }

            return new CalculatorNumber.BigDecimalNumber(this.val.multiply(o.denominator), this.denominator.multiply(o.val), Precision.calculateLowerPrecision(this.precision, o.precision), resultUnit, this.unitDirectory);
        }
    }

    public static class Precision {
        public static final int PRECISION_NO_ERROR =      1;
        public static final int PRECISION_SCALE_20 =      3;
        public static final int PRECISION_NULL     = 999999;

        public static int calculateLowerPrecision(int precision1, int precision2) {
            return Math.max(precision1, precision2);
        }
    }
}
