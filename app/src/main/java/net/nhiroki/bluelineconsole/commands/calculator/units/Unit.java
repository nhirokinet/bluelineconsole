package net.nhiroki.bluelineconsole.commands.calculator.units;

import net.nhiroki.bluelineconsole.commands.calculator.CalculatorExceptions;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorNumber;

import java.math.BigDecimal;

public interface Unit extends Comparable<Unit> {
    String getUnitName();
    int getUnitId();
    int getDimensionId();
    boolean isCalculatable();
    CalculatorNumber.BigDecimalNumber makeCalculatableFromThisUnit(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException;
    CalculatorNumber.BigDecimalNumber makeThisUnitFromCalculatable(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException;

    class NormalUnit implements Unit {
        protected final int unitId;
        protected final int dimensionId;
        protected final String unitName;
        protected final NormalUnit canonicalUnit;
        protected final CalculatorNumber.BigDecimalNumber ratioAgainstCanonicalUnit;

        NormalUnit(int unitId, int dimensionId, String unitName, NormalUnit canonicalUnit, CalculatorNumber.BigDecimalNumber ratioAgainstCanonicalUnit) throws CalculatorExceptions.IllegalFormulaException {
            if (canonicalUnit == null) {
                this.canonicalUnit = this;
            } else {
                this.canonicalUnit = canonicalUnit;
                try {
                    ratioAgainstCanonicalUnit = ratioAgainstCanonicalUnit.multiply(new CalculatorNumber.BigDecimalNumber(BigDecimal.ONE, CalculatorNumber.Precision.PRECISION_NO_ERROR, CombinedUnit.createFractionCombinedUnit(canonicalUnit, this)));
                } catch (CalculatorExceptions.UnitConversionException e) {
                    throw new RuntimeException("Failed to create Unit " + unitName);
                }
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

        @Override
        public boolean isCalculatable() {
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

        @Override
        public CalculatorNumber.BigDecimalNumber makeCalculatableFromThisUnit(CalculatorNumber.BigDecimalNumber input) {
            return input;
        }

        @Override
        public CalculatorNumber.BigDecimalNumber makeThisUnitFromCalculatable(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            return input.convertUnit(new CombinedUnit(this));
        }
    }

    class Celsius implements Unit {
        private final int unitId;
        private final int dimensionId;
        private final String unitName;
        private final NormalUnit kelvin;

        Celsius(int unitId, int dimensionId, String unitName, NormalUnit kelvin)  {
            this.unitId = unitId;
            this.dimensionId = dimensionId;
            this.unitName = unitName;
            this.kelvin = kelvin;
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
        public boolean isCalculatable() {
            return false;
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

        @Override
        public CalculatorNumber.BigDecimalNumber makeCalculatableFromThisUnit(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            if (! input.getCombinedUnit().equals(new CombinedUnit(this))) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this));
            }
            try {
                return input.removeCombinedUnit().add(new CalculatorNumber.BigDecimalNumber("273.15")).applyCombinedUnit(new CombinedUnit(this.kelvin));
            } catch (CalculatorExceptions.IllegalFormulaException e) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this.kelvin));
            }
        }

        @Override
        public CalculatorNumber.BigDecimalNumber makeThisUnitFromCalculatable(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            if (! input.getCombinedUnit().isCalculatable()) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this));
            }
            CalculatorNumber.BigDecimalNumber expressionInKelvin = input.convertUnit(new CombinedUnit(kelvin));
            try {
                return expressionInKelvin.removeCombinedUnit().subtract(new CalculatorNumber.BigDecimalNumber("273.15")).applyCombinedUnit(new CombinedUnit(this));
            } catch (CalculatorExceptions.IllegalFormulaException e) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this.kelvin));
            }
        }
    }

    class Fahrenheit implements Unit {
        private final int unitId;
        private final int dimensionId;
        private final String unitName;
        private final NormalUnit kelvin;

        Fahrenheit(int unitId, int dimensionId, String unitName, NormalUnit kelvin)  {
            this.unitId = unitId;
            this.dimensionId = dimensionId;
            this.unitName = unitName;
            this.kelvin = kelvin;
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
        public boolean isCalculatable() {
            return false;
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

        @Override
        public CalculatorNumber.BigDecimalNumber makeCalculatableFromThisUnit(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            if (! input.getCombinedUnit().equals(new CombinedUnit(this))) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this));
            }
            try {
                return input.removeCombinedUnit().subtract(new CalculatorNumber.BigDecimalNumber("32")).divide(new CalculatorNumber.BigDecimalNumber("1.8")).
                        add(new CalculatorNumber.BigDecimalNumber("273.15")).applyCombinedUnit(new CombinedUnit(this.kelvin));

            } catch (CalculatorExceptions.DivisionByZeroException e) {
                throw new RuntimeException("Tryed to divide by 1.8, but DivisionByZeroExceptionOccured");

            } catch (CalculatorExceptions.IllegalFormulaException e) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this.kelvin));
            }
        }

        @Override
        public CalculatorNumber.BigDecimalNumber makeThisUnitFromCalculatable(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            if (! input.getCombinedUnit().isCalculatable()) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this));
            }
            CalculatorNumber.BigDecimalNumber expressionInKelvin = input.convertUnit(new CombinedUnit(kelvin));
            try {
                return expressionInKelvin.removeCombinedUnit().subtract(new CalculatorNumber.BigDecimalNumber("273.15")).
                        multiply(new CalculatorNumber.BigDecimalNumber("1.8")).add(new CalculatorNumber.BigDecimalNumber("32")).
                        applyCombinedUnit(new CombinedUnit(this));
            } catch (CalculatorExceptions.IllegalFormulaException e) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this.kelvin));
            }
        }
    }
}
