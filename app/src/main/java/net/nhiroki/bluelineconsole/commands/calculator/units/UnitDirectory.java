package net.nhiroki.bluelineconsole.commands.calculator.units;

import net.nhiroki.bluelineconsole.commands.calculator.CalculatorExceptions;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorNumber;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class UnitDirectory {
    private static UnitDirectory _singleton = null;

    private static final int DIMENSION_TIME = 1;
    private static final int DIMENSION_LENGTH = 2;
    private static final int DIMENSION_MASS = 3;

    private static final int DIMENSION_DUMMY = 999;


    private final Map<String, Unit> nameToUnitMap;
    private final Map<String, CombinedUnit> nameToCombinedUnitMap;

    private final Map<String, String> unitIdsToSpecialCombinedUnitName;


    public static UnitDirectory getInstance() {
        if (_singleton == null) {
            _singleton = new UnitDirectory();
        }

        return _singleton;
    }

    private UnitDirectory() {
        this.nameToUnitMap = new HashMap<>();
        this.nameToCombinedUnitMap = new HashMap<>();
        this.unitIdsToSpecialCombinedUnitName = new HashMap<>();

        int id = 1;

        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_TIME, "s", null, CalculatorNumber.BigDecimalNumber.ONE),
                new String[]{"s", "sec", "secs", "second", "seconds"});
        Unit.NormalUnit second = (Unit.NormalUnit)this.nameToUnitMap.get("s");

        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_TIME, "minute", second,
                        new CalculatorNumber.BigDecimalNumber(new BigDecimal("60"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)
                ),
                new String[]{"minute", "minutes"});
        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_TIME, "h", second,
                        new CalculatorNumber.BigDecimalNumber(new BigDecimal("3600"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)
                ),
                new String[]{"h", "hour", "hours"});


        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_LENGTH, "m", null, CalculatorNumber.BigDecimalNumber.ONE),
                new String[]{"m", "meter", "meters", "metre", "metres"});
        Unit.NormalUnit meter = (Unit.NormalUnit)this.nameToUnitMap.get("m");
        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_LENGTH, "cm", meter,
                        new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.01"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)
                ),
                new String[]{"cm", "centimeter", "centimeters", "centimetre", "centimetres"});
        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_LENGTH, "mm", meter,
                        new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.001"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)
                ),
                new String[]{"mm", "millimeter", "millimeters", "millimetre", "millimetres"});
        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_LENGTH, "km", meter,
                        new CalculatorNumber.BigDecimalNumber(new BigDecimal("1000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)
                ),
                new String[]{"km", "kilometer", "kilometers", "kilometre", "kilometres"});
        this.registerUnit(new Unit.NormalUnit(id++, DIMENSION_LENGTH, "inch", meter,
                new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.0254"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)),
                new String[]{"inch", "inches"});
        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_LENGTH, "ft", meter,
                        new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.3048"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)
                ),
                new String[]{"ft", "foot", "feet"});
        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_LENGTH, "yard", meter,
                        new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.9144"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)
                ),
                new String[]{"yd", "yard", "yards"});
        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_LENGTH, "mile", meter,
                        new CalculatorNumber.BigDecimalNumber(new BigDecimal("1609.344"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)
                ),
                new String[]{"mile", "miles"});

        this.registerUnit(
                new Unit.NormalUnit(id++, DIMENSION_MASS, "kg", null, CalculatorNumber.BigDecimalNumber.ONE),
                new String[]{"kg", "kilogram"});
        Unit.NormalUnit kilogram = (Unit.NormalUnit)this.nameToUnitMap.get("kg");

        this.registerCombinedUnit(
                CombinedUnit.createFractionCombinedUnit(this.nameToUnitMap.get("mile"), this.nameToUnitMap.get("h")),
                new String[]{"mph"},
                null
        );

        Unit.NormalUnit dummyUnitCanonical = new Unit.NormalUnit(id++, DIMENSION_DUMMY, "1", null, CalculatorNumber.BigDecimalNumber.ONE);

        this.registerCombinedUnit(
                new CombinedUnit(
                        new Unit[] {
                                new Unit.NormalUnit(id++, DIMENSION_DUMMY, "9.80665", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("9.80665")),
                                this.nameToUnitMap.get("kg"),
                                this.nameToUnitMap.get("m"),
                        },
                        new Unit[]{
                                dummyUnitCanonical,
                                this.nameToUnitMap.get("s"),
                                this.nameToUnitMap.get("s"),
                        }
                ),
                new String[]{"kgf"},
                "kgf"
        );

        this.registerCombinedUnit(
                new CombinedUnit(
                        new Unit[] {
                                this.nameToUnitMap.get("kg"),
                                this.nameToUnitMap.get("m"),
                        },
                        new Unit[]{
                                this.nameToUnitMap.get("s"),
                                this.nameToUnitMap.get("s"),
                        }
                ),
                new String[]{"N", "newton"},
                "N"
        );
    }

    public CombinedUnit getCombinedUnitFromName(String name) throws CalculatorExceptions.IllegalFormulaException {
        if (this.nameToCombinedUnitMap.containsKey(name)) {
            return this.nameToCombinedUnitMap.get(name);
        }
        if (this.nameToUnitMap.containsKey(name)) {
            return new CombinedUnit(this.nameToUnitMap.get(name));
        }

        throw new CalculatorExceptions.IllegalFormulaException();
    }

    private void registerUnit(Unit unit, String[] nameList) {
        for (String s: nameList) {
            this.nameToUnitMap.put(s, unit);
        }
    }

    private void registerCombinedUnit(CombinedUnit unit, String[] nameList, String combinedUnitSpecialName) {
        for (String s: nameList) {
            this.nameToCombinedUnitMap.put(s, unit);
        }

        if (combinedUnitSpecialName != null) {
            this.unitIdsToSpecialCombinedUnitName.put(unit.generateIdentifiableIntArray(), combinedUnitSpecialName);
        }
    }

    public boolean isNamedCombinedUnit(CombinedUnit unit) {
        return this.unitIdsToSpecialCombinedUnitName.containsKey(unit.generateIdentifiableIntArray());
    }

    public String getSpecialCombinedUnitName(CombinedUnit unit) {
        return this.unitIdsToSpecialCombinedUnitName.get(unit.generateIdentifiableIntArray());
    }
}
