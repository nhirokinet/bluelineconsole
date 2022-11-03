package net.nhiroki.lib.bluelinecalculator.units;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.nhiroki.lib.bluelinecalculator.CalculatorExceptions;
import net.nhiroki.lib.bluelinecalculator.CalculatorNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombinedUnit {
    private List<Unit> positiveUnits = new ArrayList<>();
    private List<Unit> negativeUnits = new ArrayList<>();

    private List<Unit> explicitUnits = new ArrayList<>();

    private final UnitDirectory parentUnitDictionary;


    public CombinedUnit(UnitDirectory parentUnitDictionary) {
        this.parentUnitDictionary = parentUnitDictionary;
    }

    public CombinedUnit(Unit unit, UnitDirectory parentUnitDictionary) {
        this.positiveUnits.add(unit);
        this.explicitUnits.add(unit);
        this.parentUnitDictionary = parentUnitDictionary;
    }

    public CombinedUnit(Unit[] positiveUnits, Unit[] negativeUnits, UnitDirectory parentUnitDictionary) throws CalculatorExceptions.IllegalFormulaException {
        for (Unit u: positiveUnits) {
            if (! u.isLinear()) {
                throw new CalculatorExceptions.IllegalFormulaException();
            }
            this.positiveUnits.add(u);
        }
        for (Unit u: negativeUnits) {
            if (! u.isLinear()) {
                throw new CalculatorExceptions.IllegalFormulaException();
            }
            this.negativeUnits.add(u);
        }
        this.parentUnitDictionary = parentUnitDictionary;
    }

    public static CombinedUnit createFractionCombinedUnit(Unit numerator, Unit denominator, UnitDirectory parentUnitDictionary) throws CalculatorExceptions.IllegalFormulaException {
        CombinedUnit ret = new CombinedUnit(parentUnitDictionary);
        if (! numerator.isLinear()) {
            throw new CalculatorExceptions.IllegalFormulaException();
        }
        if (! denominator.isLinear()) {
            throw new CalculatorExceptions.IllegalFormulaException();
        }
        ret.positiveUnits.add(numerator);
        ret.negativeUnits.add(denominator);

        return ret;
    }

    public CombinedUnit explicitCombinedUnit() {
        CombinedUnit ret = new CombinedUnit(this.parentUnitDictionary);

        ret.positiveUnits = new ArrayList<>(this.positiveUnits);
        ret.negativeUnits = new ArrayList<>(this.negativeUnits);

        ret.explicitUnits.addAll(ret.positiveUnits);

        ret.explicitUnits.addAll(ret.negativeUnits);

        return ret;
    }

    public CombinedUnit explicitCombinedUnitIfSingle() {
        if (this.positiveUnits.size() + this.negativeUnits.size() == 1) {
            return this.explicitCombinedUnit();
        }
        return this;
    }

    public CalculatorNumber.BigDecimalNumber makeLinearValueFromThisUnit(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
        if (this.positiveUnits.size() != 1 || this.negativeUnits.size() != 0) {
            throw new CalculatorExceptions.UnitConversionException(this, this, this.parentUnitDictionary);
        }
        return this.positiveUnits.get(0).makeLinearValueFromThisUnit(input);
    }

    public CalculatorNumber.BigDecimalNumber makeThisUnitFromLinearValue(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
        if (this.positiveUnits.size() != 1 || this.negativeUnits.size() != 0) {
            throw new CalculatorExceptions.UnitConversionException(this, this, this.parentUnitDictionary);
        }
        return this.positiveUnits.get(0).makeThisUnitFromLinearValue(input);
    }

    public boolean isLinear() {
        for (Unit u: this.positiveUnits) {
            if (!u.isLinear()) {
                return false;
            }
        }
        for (Unit u: this.negativeUnits) {
            if (!u.isLinear()) {
                return false;
            }
        }

        return true;
    }

    public boolean equals(@Nullable CombinedUnit o) {
        this.normalize();

        if (o == null) {
            return positiveUnits.isEmpty() && negativeUnits.isEmpty();
        }
        o.normalize();


        if (positiveUnits.size() != o.positiveUnits.size()) {
            return false;
        }
        if (negativeUnits.size() != o.negativeUnits.size()) {
            return false;
        }

        int[] pos_me_array = new int[this.positiveUnits.size()];
        for (int i = 0; i < this.positiveUnits. size(); ++i) {
            pos_me_array[i] = this.positiveUnits.get(i).getUnitId();
        }
        Arrays.sort(pos_me_array);

        int[] pos_ob_array = new int[o.positiveUnits.size()];
        for (int i = 0; i < o.positiveUnits. size(); ++i) {
            pos_ob_array[i] = o.positiveUnits.get(i).getUnitId();
        }
        Arrays.sort(pos_ob_array);

        int[] neg_me_array = new int[this.negativeUnits.size()];
        for (int i = 0; i < this.negativeUnits. size(); ++i) {
            neg_me_array[i] = this.negativeUnits.get(i).getUnitId();
        }
        Arrays.sort(neg_me_array);

        int[] neg_ob_array = new int[o.negativeUnits.size()];
        for (int i = 0; i < o.negativeUnits. size(); ++i) {
            neg_ob_array[i] = o.negativeUnits.get(i).getUnitId();
        }
        Arrays.sort(neg_ob_array);

        for (int i = 0; i < this.positiveUnits.size(); ++i) {
            if (pos_me_array[i] != pos_ob_array[i]) {
                return false;
            }
        }

        for (int i = 0; i < this.negativeUnits.size(); ++i) {
            if (neg_me_array[i] != neg_ob_array[i]) {
                return false;
            }
        }

        return true;
    }

    public Unit[] getPositiveUnits() {
        this.normalize();

        Unit[] ret = new Unit[this.positiveUnits.size()];

        for (int i = 0; i < this.positiveUnits.size(); ++i) {
            ret[i] = this.positiveUnits.get(i);
        }

        return ret;
    }

    public Unit[] getNegativeUnits() {
        this.normalize();

        Unit[] ret = new Unit[this.negativeUnits.size()];

        for (int i = 0; i < this.negativeUnits.size(); ++i) {
            ret[i] = this.negativeUnits.get(i);
        }

        return ret;
    }

    public String generateIdentifiableIntArray() {
        Collections.sort(this.positiveUnits);
        Collections.sort(this.negativeUnits);

        StringBuilder ret = new StringBuilder();

        for (Unit u: this.positiveUnits) {
            ret.append(u.getUnitId()).append(",");
        }
        ret.append("/");
        for (Unit u: this.negativeUnits) {
            ret.append(u.getUnitId()).append(",");
        }

        return ret.toString();
    }

    public String generateIdentifiableIntArrayWithoutDummy() {
        Collections.sort(this.positiveUnits);
        Collections.sort(this.negativeUnits);

        StringBuilder ret = new StringBuilder();

        for (Unit u: this.positiveUnits) {
            if (u.getDimensionId() == UnitDirectory.DIMENSION_DUMMY) {
                continue;
            }
            ret.append(u.getUnitId()).append(",");
        }
        ret.append("/");
        for (Unit u: this.negativeUnits) {
            if (u.getDimensionId() == UnitDirectory.DIMENSION_DUMMY) {
                continue;
            }
            ret.append(u.getUnitId()).append(",");
        }

        return ret.toString();
    }

    public boolean dimensionEquals(@Nullable CombinedUnit o) {
        this.normalize();

        if (o == null) {
            o = new CombinedUnit(this.parentUnitDictionary);
        }

        if (this.positiveUnits.size() + o.negativeUnits.size() != this.negativeUnits.size() + o.positiveUnits.size()) {
            return false;
        }

        int[] pos_me_array = new int[this.positiveUnits.size() + o.negativeUnits.size()];
        for (int i = 0; i < this.positiveUnits.size(); ++i) {
            pos_me_array[i] = this.positiveUnits.get(i).getDimensionId();
        }
        for (int i = 0; i < o.negativeUnits. size(); ++i) {
            pos_me_array[this.positiveUnits.size() + i] = o.negativeUnits.get(i).getDimensionId();
        }
        Arrays.sort(pos_me_array);

        int[] neg_me_array = new int[this.negativeUnits.size() + o.positiveUnits.size()];
        for (int i = 0; i < this.negativeUnits.size(); ++i) {
            neg_me_array[i] = this.negativeUnits.get(i).getDimensionId();
        }
        for (int i = 0; i < o.positiveUnits.size(); ++i) {
            neg_me_array[this.negativeUnits.size() + i] = o.positiveUnits.get(i).getDimensionId();
        }
        Arrays.sort(neg_me_array);

        for (int i = 0; i < this.positiveUnits.size(); ++i) {
            if (pos_me_array[i] != neg_me_array[i]) {
                return false;
            }
        }

        return true;
    }

    public CalculatorNumber.BigDecimalNumber calculateRatioAgainst(CombinedUnit o) throws CalculatorExceptions.UnitConversionException, CalculatorExceptions.IllegalFormulaException {
        CombinedUnit diff = this.divide(o);
        if (! diff.dimensionEquals(null)) {
            throw new CalculatorExceptions.UnitConversionException(this, o, this.parentUnitDictionary);
        }

        CalculatorNumber.BigDecimalNumber ret = CalculatorNumber.BigDecimalNumber.one(this.parentUnitDictionary);

        for (Unit u: diff.positiveUnits) {
            if (u instanceof Unit.NormalUnit) {
                ret = ret.multiply(((Unit.NormalUnit)u).ratioAgainst(((Unit.NormalUnit)u).canonicalUnit));
            }
        }

        for (Unit u: diff.negativeUnits) {
            try {
                if (u instanceof Unit.NormalUnit) {
                    ret = ret.divide(((Unit.NormalUnit)u).ratioAgainst(((Unit.NormalUnit)u).canonicalUnit));
                }
            } catch (CalculatorExceptions.DivisionByZeroException e) {
                throw new RuntimeException("zero division while conversion");
            }
        }

        return ret;
    }

    public CalculatorNumber.BigDecimalNumber calculateRatioToUnifyUnitInEachDimension() throws CalculatorExceptions.IllegalFormulaException {
        if (this.parentUnitDictionary.isNamedCombinedUnit(this)) {
            return CalculatorNumber.BigDecimalNumber.one(this.parentUnitDictionary);
        }

        Map<Integer, Unit> unitForDimension = new HashMap<>();

        CombinedUnit diff = new CombinedUnit(this.parentUnitDictionary);

        for (Unit u: this.explicitUnits) {
            if (! unitForDimension.containsKey(u.getDimensionId())) {
                unitForDimension.put(u.getDimensionId(), u);
            }
        }

        for (Unit u: this.positiveUnits) {
            if (unitForDimension.containsKey(u.getDimensionId())) {
                diff = diff.multiply(new CombinedUnit(u, this.parentUnitDictionary)).divide(new CombinedUnit(unitForDimension.get(u.getDimensionId()), this.parentUnitDictionary));
            } else {
                unitForDimension.put(u.getDimensionId(), u);
            }
        }

        for (Unit u: this.negativeUnits) {
            if (unitForDimension.containsKey(u.getDimensionId())) {
                diff = diff.divide(new CombinedUnit(u, this.parentUnitDictionary)).multiply(new CombinedUnit(unitForDimension.get(u.getDimensionId()), this.parentUnitDictionary));
            } else {
                unitForDimension.put(u.getDimensionId(), u);
            }
        }


        try {
            return diff.calculateRatioAgainst(null);
        } catch (CalculatorExceptions.UnitConversionException e) {
            throw new RuntimeException("calculateRatioToUnifyUnitInEachDimension failed from " + e.getFrom().toString() + " to " + e.getTo().toString());
        }
    }

    @Override
    @NonNull
    public String toString() {
        Collections.sort(this.positiveUnits);
        Collections.sort(this.negativeUnits);

        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < this.positiveUnits.size(); ++i) {
            if (i != 0) {
                ret.append("⋅");
            }
            int exp = 1;
            while (i < this.positiveUnits.size() - 1 && this.positiveUnits.get(i + 1).getUnitId() == this.positiveUnits.get(i).getUnitId()) {
                ++exp;
                ++i;
            }
            ret.append(this.positiveUnits.get(i).getUnitName());
            if (exp > 1) {
                StringBuilder expStr = new StringBuilder();
                while (exp > 0) {
                    char c;
                    if (exp % 10 == 1) {
                        c = '¹';
                    } else if(exp % 10 == 2) {
                        c = '²';
                    } else if(exp % 10 == 3) {
                        c = '³';
                    } else {
                        c = (char)('⁰' + exp % 10);
                    }
                    expStr.insert(0, c);
                    exp /= 10;
                }
                ret.append(expStr);
            }
        }

        if (! this.negativeUnits.isEmpty()) {
            ret.append("/");
            for (int i = 0; i < this.negativeUnits.size(); ++i) {
                if (i != 0) {
                    ret.append("⋅");
                }
                int exp = 1;
                while (i < this.negativeUnits.size() - 1 && this.negativeUnits.get(i + 1).getUnitId() == this.negativeUnits.get(i).getUnitId()) {
                    ++exp;
                    ++i;
                }
                ret.append(this.negativeUnits.get(i).getUnitName());
                if (exp > 1) {
                    StringBuilder expStr = new StringBuilder();
                    while (exp > 0) {
                        char c;
                        if (exp % 10 == 1) {
                            c = '¹';
                        } else if(exp % 10 == 2) {
                            c = '²';
                        } else if(exp % 10 == 3) {
                            c = '³';
                        } else {
                            c = (char)('⁰' + exp % 10);
                        }
                        expStr.insert(0, c);
                        exp /= 10;
                    }
                    ret.append(expStr);
                }
            }
        }

        return ret.toString();
    }

    public String calculateDisplayName() {
        this.normalize();

        if (this.parentUnitDictionary.isNamedCombinedUnit(this)) {
            return this.parentUnitDictionary.getSpecialCombinedUnitName(this);
        }

        return this.toString();
    }

    public CombinedUnit multiply(@Nullable CombinedUnit o) throws CalculatorExceptions.IllegalFormulaException {
        if (! this.isLinear()) {
            throw new CalculatorExceptions.IllegalFormulaException();
        }

        if (o == null) {
            return this;
        }

        if (! o.isLinear()) {
            throw new CalculatorExceptions.IllegalFormulaException();
        }

        CombinedUnit ret = new CombinedUnit(this.parentUnitDictionary);
        ret.positiveUnits = new ArrayList<>(this.positiveUnits);
        ret.negativeUnits = new ArrayList<>(this.negativeUnits);
        ret.explicitUnits = new ArrayList<>(this.explicitUnits);

        ret.positiveUnits.addAll(o.positiveUnits);

        ret.negativeUnits.addAll(o.negativeUnits);

        ret.explicitUnits.addAll(o.explicitUnits);

        ret.normalize();

        return ret;
    }

    public CombinedUnit divide(CombinedUnit o) throws CalculatorExceptions.IllegalFormulaException {
        if (o == null) {
            return this;
        }

        if (! o.isLinear()) {
            throw new CalculatorExceptions.IllegalFormulaException();
        }

        CombinedUnit ret = new CombinedUnit(this.parentUnitDictionary);
        if (! this.isLinear()) {
            throw new CalculatorExceptions.IllegalFormulaException();
        }

        ret.positiveUnits = new ArrayList<>(this.positiveUnits);
        ret.negativeUnits = new ArrayList<>(this.negativeUnits);
        ret.explicitUnits = new ArrayList<>(this.explicitUnits);

        ret.negativeUnits.addAll(o.positiveUnits);

        ret.positiveUnits.addAll(o.negativeUnits);

        ret.explicitUnits.addAll(o.explicitUnits);

        ret.normalize();

        return ret;
    }

    private void normalize() {
        Collections.sort(this.positiveUnits);
        Collections.sort(this.negativeUnits);

        List <Unit> newPositiveUnits = new ArrayList<>();
        List <Unit> newNegativeUnits = new ArrayList<>();

        int posCur = 0;
        int negCur = 0;

        while (posCur < this.positiveUnits.size() || negCur < this.negativeUnits.size()) {
            if (posCur >= this.positiveUnits.size()) {
                newNegativeUnits.add(this.negativeUnits.get(negCur++));
                continue;
            }
            if (negCur >= this.negativeUnits.size()) {
                newPositiveUnits.add(this.positiveUnits.get(posCur++));
                continue;
            }
            int compareRes = this.positiveUnits.get(posCur).compareTo(this.negativeUnits.get(negCur));
            if (compareRes > 0) {
                newNegativeUnits.add(this.negativeUnits.get(negCur++));
            } else if (compareRes < 0) {
                newPositiveUnits.add(this.positiveUnits.get(posCur++));
            } else {
                ++posCur;
                ++negCur;
            }
        }

        this.positiveUnits = newPositiveUnits;
        this.negativeUnits = newNegativeUnits;
    }
}
