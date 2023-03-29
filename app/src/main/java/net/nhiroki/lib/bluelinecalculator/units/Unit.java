package net.nhiroki.lib.bluelinecalculator.units;

import java.math.BigDecimal;

import net.nhiroki.lib.bluelinecalculator.CalculatorExceptions;
import net.nhiroki.lib.bluelinecalculator.CalculatorNumber;

public interface Unit extends Comparable<Unit> {
    String getUnitName();
    int getUnitId();
    int getDimensionId();

    // For celsius and fahrenheit, which is not quantity, but still used in life and conversion needed.
    // They both can be converted to kelvin, which is linear unit.
    boolean isLinear();
    CalculatorNumber.BigDecimalNumber makeLinearValueFromThisUnit(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException;
    CalculatorNumber.BigDecimalNumber makeThisUnitFromLinearValue(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException;

    class NormalUnit implements Unit {
        protected final int unitId;
        protected final int dimensionId;
        protected final String unitName;
        protected final NormalUnit canonicalUnit;
        protected final CalculatorNumber.BigDecimalNumber ratioAgainstCanonicalUnit;
        private UnitDirectory parentUnitDirectory;

        public NormalUnit(int unitId, int dimensionId, String unitName, NormalUnit canonicalUnit, CalculatorNumber.BigDecimalNumber ratioAgainstCanonicalUnit, UnitDirectory parentUnitDirectory) throws CalculatorExceptions.IllegalFormulaException {
            if (canonicalUnit == null) {
                this.canonicalUnit = this;
            } else {
                this.canonicalUnit = canonicalUnit;
                try {
                    ratioAgainstCanonicalUnit = ratioAgainstCanonicalUnit.multiply(new CalculatorNumber.BigDecimalNumber(BigDecimal.ONE, CalculatorNumber.Precision.PRECISION_NO_ERROR, CombinedUnit.createFractionCombinedUnit(canonicalUnit, this, this.parentUnitDirectory), this.parentUnitDirectory));
                } catch (CalculatorExceptions.UnitConversionException e) {
                    throw new RuntimeException("Failed to create Unit " + unitName);
                }
            }

            this.ratioAgainstCanonicalUnit = ratioAgainstCanonicalUnit;
            this.unitId = unitId;
            this.dimensionId = dimensionId;
            this.unitName = unitName;
            this.parentUnitDirectory = parentUnitDirectory;
        }

        @Override
        public int getUnitId() {
            return this.unitId;
        }

        @Override
        public int getDimensionId() {
            return this.dimensionId;
        }

        @Override
        public String getUnitName() {
            return this.unitName;
        }

        @Override
        public boolean isLinear() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Unit) {
                return this.unitId == ((Unit)o).getUnitId();
            }
            return false;
        }

        @Override
        public int compareTo(Unit o) {
            if (this.getDimensionId() > o.getDimensionId()) {
                return 1;
            }
            if (this.getDimensionId() < o.getDimensionId()) {
                return -1;
            }
            return Integer.compare(this.getUnitId(), o.getUnitId());
        }

        public CalculatorNumber.BigDecimalNumber ratioAgainst(NormalUnit unit) throws CalculatorExceptions.UnitConversionException, CalculatorExceptions.IllegalFormulaException {
            if (unit == null || this.dimensionId != unit.getDimensionId()) {
                throw new CalculatorExceptions.UnitConversionException(new CombinedUnit(this, this.parentUnitDirectory), new CombinedUnit(unit, this.parentUnitDirectory), this.parentUnitDirectory);
            }

            if (this.equals(unit)) {
                return CalculatorNumber.BigDecimalNumber.one(this.parentUnitDirectory);
            }

            try {
                return this.ratioAgainstCanonicalUnit.divide(unit.ratioAgainst(this.canonicalUnit));
            } catch (CalculatorExceptions.DivisionByZeroException e) {
                throw new RuntimeException("Internal Error: division by zero within unit conversion");
            }
        }

        @Override
        public CalculatorNumber.BigDecimalNumber makeLinearValueFromThisUnit(CalculatorNumber.BigDecimalNumber input) {
            return input;
        }

        @Override
        public CalculatorNumber.BigDecimalNumber makeThisUnitFromLinearValue(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            return input.convertUnit(new CombinedUnit(this, this.parentUnitDirectory));
        }
    }
}
