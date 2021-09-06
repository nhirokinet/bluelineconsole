package net.nhiroki.bluelineconsole.commands.calculator.units;

import android.util.Log;

import net.nhiroki.bluelineconsole.commands.calculator.CalculatorExceptions;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorNumber;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombinedUnit {
    private List<Unit> positiveUnits = new ArrayList<>();
    private List<Unit> negativeUnits = new ArrayList<>();

    public CombinedUnit() {
    }

    public CombinedUnit(Unit unit) {
        this.positiveUnits.add(unit);
    }

    public CombinedUnit(List<Unit> positiveUnits, List<Unit> negativeUnits) {
        this.positiveUnits = positiveUnits;
        this.negativeUnits = negativeUnits;
    }

    public CombinedUnit(Unit[] positiveUnits, Unit[] negativeUnits) {
        for (Unit u: positiveUnits) {
            this.positiveUnits.add(u);
        }
        for (Unit u: negativeUnits) {
            this.negativeUnits.add(u);
        }
    }

    public static CombinedUnit createFractionCombinedUnit(Unit numerator, Unit denominator) {
        CombinedUnit ret = new CombinedUnit();
        ret.positiveUnits.add(numerator);
        ret.negativeUnits.add(denominator);

        return ret;
    }

    public boolean equals(CombinedUnit o) {
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

        int pos_me_array[] = new int[this.positiveUnits.size()];
        for (int i = 0; i < this.positiveUnits. size(); ++i) {
            pos_me_array[i] = this.positiveUnits.get(i).getUnitId();
        }
        Arrays.sort(pos_me_array);

        int pos_ob_array[] = new int[o.positiveUnits.size()];
        for (int i = 0; i < o.positiveUnits. size(); ++i) {
            pos_ob_array[i] = o.positiveUnits.get(i).getUnitId();
        }
        Arrays.sort(pos_ob_array);

        int neg_me_array[] = new int[this.negativeUnits.size()];
        for (int i = 0; i < this.negativeUnits. size(); ++i) {
            neg_me_array[i] = this.negativeUnits.get(i).getUnitId();
        }
        Arrays.sort(neg_me_array);

        int neg_ob_array[] = new int[o.negativeUnits.size()];
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

    public String generateIdentifiableIntArray() {
        Collections.sort(this.positiveUnits);
        Collections.sort(this.negativeUnits);

        String ret = "";

        for (Unit u: this.positiveUnits) {
            ret += u.getUnitId() + ",";
        }
        ret += "/";
        for (Unit u: this.negativeUnits) {
            ret += u.getUnitId() + ",";
        }

        return ret;
    }

    public boolean dimensionEquals(CombinedUnit o) {
        this.normalize();

        if (o == null) {
            o = new CombinedUnit();
        }

        if (this.positiveUnits.size() + o.negativeUnits.size() != this.negativeUnits.size() + o.positiveUnits.size()) {
            return false;
        }

        int pos_me_array[] = new int[this.positiveUnits.size() + o.negativeUnits.size()];
        for (int i = 0; i < this.positiveUnits.size(); ++i) {
            pos_me_array[i] = this.positiveUnits.get(i).getDimensionId();
        }
        for (int i = 0; i < o.negativeUnits. size(); ++i) {
            pos_me_array[this.positiveUnits.size() + i] = o.negativeUnits.get(i).getDimensionId();
        }
        Arrays.sort(pos_me_array);

        int neg_me_array[] = new int[this.negativeUnits.size() + o.positiveUnits.size()];
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

    public CalculatorNumber.BigDecimalNumber calculateRatioAgainst(CombinedUnit o) throws CalculatorExceptions.UnitConversionException {
        CombinedUnit diff = this.divide(o);
        if (! diff.dimensionEquals(null)) {
            throw new CalculatorExceptions.UnitConversionException(this, o);
        }

        CalculatorNumber.BigDecimalNumber ret = CalculatorNumber.BigDecimalNumber.ONE;

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
        if (UnitDirectory.getInstance().isNamedCombinedUnit(this)) {
            return CalculatorNumber.BigDecimalNumber.ONE;
        }

        Map<Integer, Unit> unitForDimension = new HashMap<>();

        CombinedUnit diff = new CombinedUnit();

        for (Unit u: this.positiveUnits) {
            if (unitForDimension.containsKey(u.getDimensionId())) {
                diff = diff.multiply(new CombinedUnit(u)).divide(new CombinedUnit(unitForDimension.get(u.getDimensionId())));
            } else {
                unitForDimension.put(u.getDimensionId(), u);
            }
        }

        for (Unit u: this.negativeUnits) {
            if (unitForDimension.containsKey(u.getDimensionId())) {
                diff = diff.divide(new CombinedUnit(u)).multiply(new CombinedUnit(unitForDimension.get(u.getDimensionId())));
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
    public String toString() {
        Collections.sort(this.positiveUnits);
        Collections.sort(this.negativeUnits);

        String ret = "";

        for (int i = 0; i < this.positiveUnits.size(); ++i) {
            if (i != 0) {
                ret += "⋅";
            }
            int exp = 1;
            while (i < this.positiveUnits.size() - 1 && this.positiveUnits.get(i + 1).getUnitId() == this.positiveUnits.get(i).getUnitId()) {
                ++exp;
                ++i;
            }
            ret += this.positiveUnits.get(i).getUnitName();
            if (exp > 1) {
                String expStr = "";
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
                    expStr = c + expStr;
                    exp /= 10;
                }
                ret += expStr;
            }
        }

        if (! this.negativeUnits.isEmpty()) {
            ret += "/";
            for (int i = 0; i < this.negativeUnits.size(); ++i) {
                if (i != 0) {
                    ret += "⋅";
                }
                int exp = 1;
                while (i < this.negativeUnits.size() - 1 && this.negativeUnits.get(i + 1).getUnitId() == this.negativeUnits.get(i).getUnitId()) {
                    ++exp;
                    ++i;
                }
                ret += this.negativeUnits.get(i).getUnitName();
                if (exp > 1) {
                    String expStr = "";
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
                        expStr = c + expStr;
                        exp /= 10;
                    }
                    ret += expStr;
                }
            }
        }

        return ret;
    }

    public String calculateDisplayName() {
        this.normalize();

        if (UnitDirectory.getInstance().isNamedCombinedUnit(this)) {
            return UnitDirectory.getInstance().getSpecialCombinedUnitName(this);
        }

        return this.toString();
    }

    public CombinedUnit multiply(CombinedUnit o) {
        if (this == null) {
            return o;
        }

        if (o == null) {
            return this;
        }

        CombinedUnit ret = new CombinedUnit();
        ret.positiveUnits = new ArrayList<>(this.positiveUnits);
        ret.negativeUnits = new ArrayList<>(this.negativeUnits);

        for (Unit u: o.positiveUnits) {
            ret.positiveUnits.add(u);
        }

        for (Unit u: o.negativeUnits) {
            ret.negativeUnits.add(u);
        }

        ret.normalize();

        return ret;
    }

    public CombinedUnit divide(CombinedUnit o) {
        if (o == null) {
            return this;
        }

        CombinedUnit ret = new CombinedUnit();
        if (this != null) {
            ret.positiveUnits = new ArrayList<>(this.positiveUnits);
            ret.negativeUnits = new ArrayList<>(this.negativeUnits);
        }

        for (Unit u: o.positiveUnits) {
            ret.negativeUnits.add(u);
        }

        for (Unit u: o.negativeUnits) {
            ret.positiveUnits.add(u);
        }

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
                continue;
            } else if (compareRes < 0) {
                newPositiveUnits.add(this.positiveUnits.get(posCur++));
                continue;
            } else {
                ++posCur;
                ++negCur;
            }
        }

        this.positiveUnits = newPositiveUnits;
        this.negativeUnits = newNegativeUnits;
    }
}
