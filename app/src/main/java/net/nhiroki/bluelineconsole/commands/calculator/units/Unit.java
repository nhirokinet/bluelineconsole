package net.nhiroki.bluelineconsole.commands.calculator.units;

import net.nhiroki.bluelineconsole.commands.calculator.CalculatorExceptions;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorNumber;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface Unit extends Comparable<Unit> {
    public String getUnitName();
    public int getUnitId();
    public int getDimensionId();


    public class NormalUnit implements Unit {
        protected final int unitId;
        protected final int dimensionId;
        protected final String unitName;
        protected final NormalUnit canonicalUnit;
        protected final CalculatorNumber.BigDecimalNumber ratioAgainstCanonicalUnit;

        NormalUnit(int unitId, int dimensionId, String unitName, NormalUnit canonicalUnit, CalculatorNumber.BigDecimalNumber ratioAgainstCanonicalUnit) {
            if (canonicalUnit == null) {
                this.canonicalUnit = this;
            } else {
                this.canonicalUnit = canonicalUnit;
                ratioAgainstCanonicalUnit = ratioAgainstCanonicalUnit.multiply(new CalculatorNumber.BigDecimalNumber(BigDecimal.ONE, CalculatorNumber.Precision.PRECISION_NO_ERROR, CombinedUnit.createFractionCombinedUnit(canonicalUnit, this)));
            }

            this.ratioAgainstCanonicalUnit = ratioAgainstCanonicalUnit;
            this.unitId = unitId;
            this.dimensionId = dimensionId;
            this.unitName = unitName;
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

        public boolean equals(Unit o) {
            return this.unitId == o.getUnitId();
        }

        @Override
        public int compareTo(Unit o) {
            if (this.getDimensionId() > o.getDimensionId()) {
                return 1;
            }
            if (this.getDimensionId() < o.getDimensionId()) {
                return -1;
            }
            if (this.getUnitId() > o.getUnitId()) {
                return 1;
            }
            if (this.getUnitId() < o.getUnitId()) {
                return -1;
            }
            return 0;
        }

        public CalculatorNumber.BigDecimalNumber ratioAgainst(NormalUnit unit) throws CalculatorExceptions.UnitConversionException {
            if (unit == null || this.dimensionId != unit.getDimensionId()) {
                throw new CalculatorExceptions.UnitConversionException(new CombinedUnit(this), new CombinedUnit(unit));
            }

            if (this.equals(unit)) {
                return CalculatorNumber.BigDecimalNumber.ONE;
            }

            try {
                return this.ratioAgainstCanonicalUnit.divide(unit.ratioAgainst(this.canonicalUnit));
            } catch (CalculatorExceptions.DivisionByZeroException e) {
                throw new RuntimeException("Internal Error: division by zero within unit conversion");
            }
        }
    }

}
