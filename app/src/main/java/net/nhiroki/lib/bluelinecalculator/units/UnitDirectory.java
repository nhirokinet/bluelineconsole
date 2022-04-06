package net.nhiroki.lib.bluelinecalculator.units;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.nhiroki.lib.bluelinecalculator.CalculatorExceptions;
import net.nhiroki.lib.bluelinecalculator.CalculatorNumber;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class UnitDirectory {
    // Dummy dimension, which is actually same as 1, which must be eliminated before displaying
    protected static final int DIMENSION_DUMMY = -1;

    protected final Map<String, Unit> nameToUnitMap;
    protected final Map<String, CombinedUnit> nameToCombinedUnitMap;

    private final Map<String, String> unitIdsToSpecialCombinedUnitName;
    private final Map<String, CombinedUnit> unitIdsToShouldConvertCombinedUnit;

    private final Map<String, List<CombinedUnit>> dimensionIdsToPreferredCombinedUnit;

    private int nextLocalID = 1;


    public CombinedUnit getCombinedUnitFromName(String name) throws CalculatorExceptions.IllegalFormulaException {
        if (this.nameToCombinedUnitMap.containsKey(name)) {
            return this.nameToCombinedUnitMap.get(name);
        }
        if (this.nameToUnitMap.containsKey(name)) {
            return new CombinedUnit(this.nameToUnitMap.get(name), this);
        }

        throw new CalculatorExceptions.IllegalFormulaException();
    }

    protected void registerUnit(Unit unit, String[] nameList) {
        for (String s: nameList) {
            this.nameToUnitMap.put(s, unit);
        }
    }

    protected void registerCombinedUnit(CombinedUnit unit, String[] nameList, String combinedUnitSpecialName) {
        for (String s: nameList) {
            this.nameToCombinedUnitMap.put(s, unit);
        }

        if (combinedUnitSpecialName != null) {
            this.unitIdsToSpecialCombinedUnitName.put(unit.generateIdentifiableIntArray(), combinedUnitSpecialName);
        }
    }

    protected void registerCombinedUnitAsShouldConvert(CombinedUnit unit) {
        this.unitIdsToShouldConvertCombinedUnit.put(unit.generateIdentifiableIntArrayWithoutDummy(), unit);
    }

    @Nullable
    public CombinedUnit getShouldConvertFrom(CombinedUnit unit) {
        if (this.unitIdsToShouldConvertCombinedUnit.containsKey(unit.generateIdentifiableIntArrayWithoutDummy())){
            return this.unitIdsToShouldConvertCombinedUnit.get(unit.generateIdentifiableIntArrayWithoutDummy());
        }
        return null;
    }

    public boolean isNamedCombinedUnit(CombinedUnit unit) {
        return this.unitIdsToSpecialCombinedUnitName.containsKey(unit.generateIdentifiableIntArray());
    }

    public String getSpecialCombinedUnitName(CombinedUnit unit) {
        return this.unitIdsToSpecialCombinedUnitName.get(unit.generateIdentifiableIntArray());
    }

    private static String dimensionIdsToUniqueStr(CombinedUnit unit, boolean includeDummy) {
        StringBuilder posStr = new StringBuilder();
        StringBuilder negStr = new StringBuilder();

        int posCur = 0;
        int negCur = 0;

        while (posCur < unit.getPositiveUnits().length || negCur < unit.getNegativeUnits().length) {
            if (posCur < unit.getPositiveUnits().length && negCur < unit.getNegativeUnits().length) {
                if (unit.getPositiveUnits()[posCur].getDimensionId() == unit.getNegativeUnits()[negCur].getDimensionId()){
                    ++posCur;
                    ++negCur;
                    continue;
                } else if (unit.getPositiveUnits()[posCur].getDimensionId() > unit.getNegativeUnits()[negCur].getDimensionId()){
                    if (includeDummy || unit.getNegativeUnits()[negCur].getDimensionId() != DIMENSION_DUMMY) {
                        negStr.append(",").append(unit.getNegativeUnits()[negCur].getDimensionId());
                    }
                    ++negCur;
                    continue;
                } else {
                    if (includeDummy || unit.getPositiveUnits()[posCur].getDimensionId() != DIMENSION_DUMMY) {
                        posStr.append(",").append(unit.getPositiveUnits()[posCur].getDimensionId());
                    }
                    ++posCur;
                    continue;
                }
            }

            if (posCur < unit.getPositiveUnits().length) {
                if (includeDummy || unit.getPositiveUnits()[posCur].getDimensionId() != DIMENSION_DUMMY) {
                    posStr.append(",").append(unit.getPositiveUnits()[posCur].getDimensionId());
                }
                ++posCur;
            }
            if (negCur < unit.getNegativeUnits().length) {
                if (includeDummy || unit.getNegativeUnits()[negCur].getDimensionId() != DIMENSION_DUMMY) {
                    negStr.append(",").append(unit.getNegativeUnits()[negCur].getDimensionId());
                }
                ++negCur;
            }

        }
        return posStr + "/" + negStr;
    }

    @NonNull
    public List<CombinedUnit> getPreferredCombinedUnits(CombinedUnit unit) {
        String dimensionStr = dimensionIdsToUniqueStr(unit, false);
        if (this.dimensionIdsToPreferredCombinedUnit.containsKey(dimensionStr)) {
            return Objects.requireNonNull(this.dimensionIdsToPreferredCombinedUnit.get(dimensionStr));
        }
        return new ArrayList<>();
    }

    public void setPreferredCombinedUnits(@NonNull CombinedUnit[] combinedUnits) throws CalculatorExceptions.UnitConversionException {
        if (combinedUnits.length == 0) {
            return;
        }
        String dimensionStr = dimensionIdsToUniqueStr(combinedUnits[0], false);
        for (CombinedUnit u:combinedUnits) {
            if (! dimensionIdsToUniqueStr(u, false).equals(dimensionStr)) {
                throw new CalculatorExceptions.UnitConversionException(u, combinedUnits[0], this);
            }
        }

        class ObjForSort implements Comparable<ObjForSort> {
            final CombinedUnit combinedUnit;
            ObjForSort(CombinedUnit combinedUnit) {
                this.combinedUnit = combinedUnit;
            }
            @Override
            public int compareTo(ObjForSort o) {
                try {
                    return new CalculatorNumber.BigDecimalNumber(BigDecimal.ONE, CalculatorNumber.Precision.PRECISION_NO_ERROR, this.combinedUnit, UnitDirectory.this).compareTo(new CalculatorNumber.BigDecimalNumber(BigDecimal.ONE, CalculatorNumber.Precision.PRECISION_NO_ERROR, o.combinedUnit, UnitDirectory.this));
                } catch (CalculatorExceptions.UnitConversionException e) {
                    throw new RuntimeException("Different dimension in setPreferredCombinedUnits");
                } catch (CalculatorExceptions.IllegalFormulaException e) {
                    throw new RuntimeException("Different dimension in setPreferredCombinedUnits");
                }
            }
        }

        List <ObjForSort> objects = new ArrayList<>();
        for (CombinedUnit u: combinedUnits) {
            objects.add(new ObjForSort(u));
        }

        Collections.sort(objects);

        List<CombinedUnit> copy = new ArrayList<>();

        for (ObjForSort o: objects) {
            copy.add(o.combinedUnit);
        }

        this.dimensionIdsToPreferredCombinedUnit.put(dimensionStr, copy);
    }

    public String calculateSpecialPreferredForm(CalculatorNumber.BigDecimalNumber number) {
        throw new RuntimeException("calculateSpecialPreferredForm is not implemented in this instance");
    }

    public CalculatorNumber.BigDecimalNumber findSpecialPreferredForm(CalculatorNumber.BigDecimalNumber number) {
        return null;
    }

    protected int issueLocalID() {
        return nextLocalID++;
    }

    public UnitDirectory() {
        this.nameToUnitMap = new HashMap<>();
        this.nameToCombinedUnitMap = new HashMap<>();
        this.unitIdsToSpecialCombinedUnitName = new HashMap<>();
        this.dimensionIdsToPreferredCombinedUnit = new HashMap<>();
        this.unitIdsToShouldConvertCombinedUnit = new HashMap<>();
    }
}
