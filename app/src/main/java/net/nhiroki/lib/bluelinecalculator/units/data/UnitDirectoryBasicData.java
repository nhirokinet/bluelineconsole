package net.nhiroki.lib.bluelinecalculator.units.data;

import androidx.annotation.NonNull;

import net.nhiroki.lib.bluelinecalculator.CalculatorExceptions;
import net.nhiroki.lib.bluelinecalculator.CalculatorNumber;
import net.nhiroki.lib.bluelinecalculator.units.CombinedUnit;
import net.nhiroki.lib.bluelinecalculator.units.Unit;
import net.nhiroki.lib.bluelinecalculator.units.UnitDirectory;

import java.math.BigDecimal;

public class UnitDirectoryBasicData extends UnitDirectory {
    // 1-7: SI base units
    protected static final int DIMENSION_TIME                =    1;
    protected static final int DIMENSION_LENGTH              =    2;
    protected static final int DIMENSION_MASS                =    3;
    protected static final int DIMENSION_ELECTRIC_CURRENT    =    4;
    protected static final int DIMENSION_TEMPERATURE         =    5;
    protected static final int DIMENSION_AMOUNT_OF_SUBSTANCE =    6;
    protected static final int DIMENSION_LUMINOUS_INTENSITY  =    7;

    // 1000-1999: Normally calculatable dimension which is not in SI
    protected static final int DIMENSION_INFORMATION         = 1001;

    // 2000-2999: Special dimension definition for uncalculatable units
    protected static final int DIMENSION_CELSIUS             = 2001;
    protected static final int DIMENSION_FAHRENHEIT          = 2002;

    private static UnitDirectoryBasicData _singleton = null;

    private final Unit.NormalUnit second;


    public static synchronized UnitDirectoryBasicData getInstance() {
        if (_singleton == null) {
            _singleton = new UnitDirectoryBasicData();
        }
        return _singleton;
    }

    protected UnitDirectoryBasicData() {
        super();

        int id = 1;

        try {
            this.second = new Unit.NormalUnit(id++, DIMENSION_TIME, "s", null, CalculatorNumber.BigDecimalNumber.one(this), this);
            this.registerUnit(
                    this.second,
                    new String[]{"s", "sec", "secs", "second", "seconds"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_TIME, "minute", second,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("60"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"minute", "minutes"});
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_TIME, "h", second,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("3600"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"h", "hour", "hours"});


            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LENGTH, "m", null, CalculatorNumber.BigDecimalNumber.one(this), this),
                    new String[]{"m", "meter", "meters", "metre", "metres"});
            Unit.NormalUnit meter = (Unit.NormalUnit) this.nameToUnitMap.get("m");
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LENGTH, "cm", meter,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.01"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"cm", "centimeter", "centimeters", "centimetre", "centimetres"});
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LENGTH, "mm", meter,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.001"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"mm", "millimeter", "millimeters", "millimetre", "millimetres"});
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LENGTH, "km", meter,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"km", "kilometer", "kilometers", "kilometre", "kilometres"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_MASS, "kg", null, CalculatorNumber.BigDecimalNumber.one(this), this),
                    new String[]{"kg", "kilogram"});
            Unit.NormalUnit kilogram = (Unit.NormalUnit) this.nameToUnitMap.get("kg");
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_MASS, "g", kilogram,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.001"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"g", "gram", "grams"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_TEMPERATURE, "K", null, CalculatorNumber.BigDecimalNumber.one(this), this),
                    new String[]{"K", "kelvin"});
            Unit.NormalUnit kelvin = (Unit.NormalUnit) this.nameToUnitMap.get("kelvin");

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_ELECTRIC_CURRENT, "A", null, CalculatorNumber.BigDecimalNumber.one(this), this),
                    new String[]{"A", "ampere"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_AMOUNT_OF_SUBSTANCE, "mol", null, CalculatorNumber.BigDecimalNumber.one(this), this),
                    new String[]{"mol", "mole"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LUMINOUS_INTENSITY, "cd", null, CalculatorNumber.BigDecimalNumber.one(this), this),
                    new String[]{"cd", "candela"});

            // Assume 1 byte = 1 octet
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "bit", null, CalculatorNumber.BigDecimalNumber.one(this), this),
                    new String[]{"bit", "bits"});
            Unit.NormalUnit bit = (Unit.NormalUnit) this.nameToUnitMap.get("bit");

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "byte", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"byte", "bytes"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "kbit", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"kbit", "kilobit", "kilobits"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "kB", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"kB", "kilobyte", "kilobytes"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "Kibit", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1024"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"Kibit", "kibibit", "kibibits"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "KiB", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8192"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"KiB", "kibibyte", "kibibytes"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "Mbit", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1000000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"Mbit", "megabit", "megabits"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "MB", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8000000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"MB", "megabyte", "megabytes"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "Mibit", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1048576"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"Mibit", "mebibit", "mebibits"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "MiB", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8388608"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"MiB", "mebibyte", "mebibytes"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "Gbit", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1000000000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"Gbit", "gigabit", "gigabits"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "GB", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8000000000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"GB", "gigabyte", "gigabytes"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "Gibit", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1073741824"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"Gibit", "gibibit", "gibibits"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "GiB", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8589934592"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"GiB", "gibibyte", "gibibytes"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "Tbit", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1000000000000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"Tbit", "terabit", "terabits"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "TB", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8000000000000"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"TB", "terabyte", "terabytes"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "Tibit", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1099511627776"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"Tibit", "tebibit", "tebibits"});

            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_INFORMATION, "TiB", bit,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("8796093022208"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"TiB", "tebibyte", "tebibytes"});

            // Here starts combined units
            Unit.NormalUnit dummyUnitCanonical = new Unit.NormalUnit(id++, DIMENSION_DUMMY, "1", null, CalculatorNumber.BigDecimalNumber.one(this), this);
            Unit.NormalUnit dummyUnitHecto = new Unit.NormalUnit(id++, DIMENSION_DUMMY, "hecto", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("100", this), this);
            Unit.NormalUnit dummyUnitKilo = new Unit.NormalUnit(id++, DIMENSION_DUMMY, "kilo", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("1000", this), this);
            Unit.NormalUnit dummyUnitMega = new Unit.NormalUnit(id++, DIMENSION_DUMMY, "mega", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("1000000", this), this);
            Unit.NormalUnit dummyUnitMilli = new Unit.NormalUnit(id++, DIMENSION_DUMMY, "milli", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("0.001", this), this);
            Unit.NormalUnit dummyUnitMicro = new Unit.NormalUnit(id++, DIMENSION_DUMMY, "micro", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("0.000001", this), this);
            Unit.NormalUnit dummyUnit9_80665 = new Unit.NormalUnit(id++, DIMENSION_DUMMY, "9.80665", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("9.80665", this), this);

            // Force  Dimension: kg * m * s^(-2)
            /*
             * Standard gravity = 9.80665 m * s^(-2)
             *
             * https://en.wikipedia.org/wiki/Standard_gravity
             * > It is defined by standard as 9.80665 m/s2 (about 32.17405 ft/s2).
             */
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnit9_80665,
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"kgf"},
                    "kgf"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"N", "newton"},
                    "N"
            );
            this.setPreferredCombinedUnits(new CombinedUnit[]{this.nameToCombinedUnitMap.get("N")});

            // Other SI units
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("ampere"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"V", "volt", "volts"},
                    "V"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnitKilo,
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("ampere"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"kV", "kilovolt", "kilovolts"},
                    "kV"
            );
            this.setPreferredCombinedUnits(new CombinedUnit[]{this.nameToCombinedUnitMap.get("V"), this.nameToCombinedUnitMap.get("kV")});

            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("ampere"),
                                    this.nameToUnitMap.get("ampere"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"ohm"},
                    "ohm"
            );

            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"W", "watt", "watts"},
                    "W"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnitKilo,
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"kW", "kilowatt", "kilowatts"},
                    "kW"
            );
            this.setPreferredCombinedUnits(new CombinedUnit[]{this.nameToCombinedUnitMap.get("W"), this.nameToCombinedUnitMap.get("kW")});

            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"J", "joule", "joules"},
                    "J"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnitKilo,
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"kJ"},
                    "kJ"
            );
            this.setPreferredCombinedUnits(new CombinedUnit[]{this.nameToCombinedUnitMap.get("J"), this.nameToCombinedUnitMap.get("kJ")});

            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("kg"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("m"),
                            },
                            this
                    ),
                    new String[]{"Pa", "pascal", "pascals"},
                    "Pa"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnitHecto,
                                    this.nameToUnitMap.get("kg"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("m"),
                            },
                            this
                    ),
                    new String[]{"hPa", "hectopascal", "hectopascals"},
                    "hPa"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnitKilo,
                                    this.nameToUnitMap.get("kg"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("m"),
                            },
                            this
                    ),
                    new String[]{"kPa", "kilopascal", "kilopascals"},
                    "kPa"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnitMega,
                                    this.nameToUnitMap.get("kg"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("m"),
                            },
                            this
                    ),
                    new String[]{"MPa", "megapascal", "megapascals"},
                    "MPa"
            );
            this.setPreferredCombinedUnits(new CombinedUnit[]{this.nameToCombinedUnitMap.get("Pa"), this.nameToCombinedUnitMap.get("hPa"), this.nameToCombinedUnitMap.get("kPa"), this.nameToCombinedUnitMap.get("MPa"), });


            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{},
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"Hz", "hertz"},
                    null
            );

            // Volume  Dimension: m^3
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnitMicro,
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                            },
                            this
                    ),
                    new String[]{"milliliter", "milliliters", "millilitre", "millilitres"},
                    "milliliter"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnitMilli,
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                            },
                            this
                    ),
                    new String[]{"liter", "liters", "litre", "litres"},
                    "liter"
            );

            // Information speed  Dimension: bit s^(-1)
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("bit"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"bps"},
                    "bps"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("kbit"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"kbps"},
                    "kbps"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("megabit"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"Mbps"},
                    "Mbps"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("gigabit"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"Gbps"},
                    "Gbps"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("terabit"),
                            },
                            new Unit[]{
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"Tbps"},
                    "Tbps"
            );

            // Daily units
            /*
             * 1 cal = 4.184 J
             *
             * https://elaws.e-gov.go.jp/document?lawid=404CO0000000357
             * カロリー
             * > ジュール又はワット秒の四・一八四倍
             */
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    new Unit.NormalUnit(id++, DIMENSION_DUMMY, "4.184", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("4.184", this), this),
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"cal", "calorie", "calories"},
                    "cal"
            );
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    new Unit.NormalUnit(id++, DIMENSION_DUMMY, "4184", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("4184", this), this),
                                    this.nameToUnitMap.get("kg"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"kcal"},
                    "kcal"
            );

            // Special Temperature units
            this.registerUnit(
                    new Celsius(id++, DIMENSION_CELSIUS, "celsius", kelvin, this),
                    new String[]{"celsius"});
            this.registerUnit(
                    new Fahrenheit(id++, DIMENSION_FAHRENHEIT, "fahrenheit", kelvin, this),
                    new String[]{"fahrenheit"});

            /*
             * Imperial units conversion
             *
             * 1 yard = 0.9144 m
             * 1 lb = 0.45359237 kg
             *
             * https://books.google.com/books?id=4aWN-VRV1AoC&pg=PA13  ([1] in Wikipedia link below on 2021/09/12)
             * > According to the agreement, the international yard equals 0.9144 meter and the international pound equals 0.453 592 37 kilogram.
             *
             * https://en.wikipedia.org/wiki/International_yard_and_pound
             * > The international yard and pound are two units of measurement that were the subject of an agreement among representatives of six nations signed on 1 July 1959; the United States, United Kingdom, Canada, Australia, New Zealand, and South Africa. The agreement defined the yard as exactly 0.9144 meters and the (avoirdupois) pound as exactly 0.45359237 kilograms.[1]
             *
             * https://www.legislation.gov.uk
             * yard
             * > 0.9144 metre
             *
             * https://elaws.e-gov.go.jp/document?lawid=404CO0000000357
             * ヤード
             * > メートルの〇・九一四四倍
             * ポンド
             * > キログラムの〇・四五三五九二三七倍
             *
             *
             * 1 mile = 1760 yard
             *
             * https://en.wikipedia.org/wiki/Mile
             * >  The statute mile was standardised between the British Commonwealth and the United States by an international agreement in 1959, when it was formally redefined with respect to SI units as exactly 1,609.344 metres.
             *
             * https://www.legislation.gov.uk/uksi/1995/1804/schedule/made
             * mile
             * > 1.609344 kilometres
             *
             * https://elaws.e-gov.go.jp/document?lawid=404CO0000000357
             * マイル
             * > ヤードの千七百六十倍
             */
            this.registerUnit(new Unit.NormalUnit(id++, DIMENSION_LENGTH, "inch", meter,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.0254"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this), this),
                    new String[]{"inch", "inches"});
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LENGTH, "ft", meter,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.3048"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"ft", "foot", "feet"});
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LENGTH, "yard", meter,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.9144"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"yd", "yard", "yards"});
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LENGTH, "mile", meter,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1609.344"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"mile", "miles"});
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_MASS, "oz", kilogram,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.028349523125"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"oz", "ounce", "grams"});
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_MASS, "lb", kilogram,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("0.45359237"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"lb", "pound", "pounds"});
            this.registerCombinedUnit(
                    CombinedUnit.createFractionCombinedUnit(this.nameToUnitMap.get("mile"), this.nameToUnitMap.get("h"), this),
                    new String[]{"mph"},
                    null
            );

            /*
             * 1 nautical mile = 1852 m
             *
             * https://en.wikipedia.org/wiki/Nautical_mile
             * > Today the international nautical mile is defined as exactly 1852 metres (6076 ft; 1.151 mi). The derived unit of speed is the knot, one nautical mile per hour.
             *
             * https://elaws.e-gov.go.jp/document?lawid=404CO0000000357
             * ノット
             * > 一時間に千八百五十二メートルの速さ
             */
            this.registerUnit(
                    new Unit.NormalUnit(id++, DIMENSION_LENGTH, "nmi", meter,
                            new CalculatorNumber.BigDecimalNumber(new BigDecimal("1852"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null, this),
                            this
                    ),
                    new String[]{"NM", "nmi", "nauticalmile"});
            this.registerCombinedUnit(
                    CombinedUnit.createFractionCombinedUnit(this.nameToUnitMap.get("nmi"), this.nameToUnitMap.get("h"), this),
                    new String[]{"knot"},
                    "knot"
            );

            /*
             * Standard gravity = 9.80665 m * s^(-2)
             *
             * https://en.wikipedia.org/wiki/Standard_gravity
             * > It is defined by standard as 9.80665 m/s2 (about 32.17405 ft/s2).
             *
             * This also applies to lbf
             *
             * https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication811e2008.pdf ([7] in Wikipedia on 2021/09/12)
             * > The  exact  conversion  factor  is  4.448  221  615  260  5  E+00  since  the  standard  value  of  the  acceleration  due  to  gravity, gn = 9.806 65 m/s2 exactly, is used to define the kilogram-force:  1 kgf = 9.806 65  E+00 N exactly.
             *
             * https://en.wikipedia.org/wiki/Pound_per_square_inch (on 2021/09/12)
             * This page multiplies 0.45359237kg and 9.80665 m/s^2
             *
             */
            this.registerCombinedUnit(
                    new CombinedUnit(
                            new Unit[]{
                                    dummyUnit9_80665,
                                    this.nameToUnitMap.get("lb"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{
                                    dummyUnitCanonical,
                                    this.nameToUnitMap.get("s"),
                                    this.nameToUnitMap.get("s"),
                            },
                            this
                    ),
                    new String[]{"lbf"},
                    "lbf"
            );
            CombinedUnit psi = new CombinedUnit(
                    new Unit[]{
                            dummyUnit9_80665,
                            this.nameToUnitMap.get("lb"),
                    },
                    new Unit[]{
                            new Unit.NormalUnit(id++, DIMENSION_DUMMY, "0.0254", dummyUnitCanonical, new CalculatorNumber.BigDecimalNumber("0.0254", this), this),
                            this.nameToUnitMap.get("inch"),
                            this.nameToUnitMap.get("s"),
                            this.nameToUnitMap.get("s"),
                    },
                    this
            );
            this.registerCombinedUnit(psi, new String[]{"psi"}, "lbf/inch²");
            // As long as unit is (X * lb/inch s s), this unit should be converted into lbf/in^2, which is (9.80665 lb / 0.0254 inch s^2), because (lb / inch s s) is far from typical pressure form
            this.registerCombinedUnitAsShouldConvert(psi);


            /*
            this.setPreferredCombinedUnits(new CombinedUnit[]{
                    new CombinedUnit(this.nameToUnitMap.get("m")),
                    new CombinedUnit(this.nameToUnitMap.get("cm")),
                    new CombinedUnit(this.nameToUnitMap.get("km")),
                    new CombinedUnit(this.nameToUnitMap.get("mm")),
            });

            this.setPreferredCombinedUnits(new CombinedUnit[]{
                    CombinedUnit.createFractionCombinedUnit(this.nameToUnitMap.get("km"), this.nameToUnitMap.get("h")),
            });

            this.setPreferredCombinedUnits(new CombinedUnit[]{
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{}
                    ),
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("cm"),
                                    this.nameToUnitMap.get("cm"),
                            },
                            new Unit[]{}
                    ),
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("mm"),
                                    this.nameToUnitMap.get("mm"),
                            },
                            new Unit[]{}
                    ),
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("km"),
                                    this.nameToUnitMap.get("km"),
                            },
                            new Unit[]{}
                    ),
            });

            this.setPreferredCombinedUnits(new CombinedUnit[]{
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                                    this.nameToUnitMap.get("m"),
                            },
                            new Unit[]{}
                    ),
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("cm"),
                                    this.nameToUnitMap.get("cm"),
                                    this.nameToUnitMap.get("cm"),
                            },
                            new Unit[]{}
                    ),
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("mm"),
                                    this.nameToUnitMap.get("mm"),
                                    this.nameToUnitMap.get("mm"),
                            },
                            new Unit[]{}
                    ),
                    new CombinedUnit(
                            new Unit[]{
                                    this.nameToUnitMap.get("km"),
                                    this.nameToUnitMap.get("km"),
                                    this.nameToUnitMap.get("km"),
                            },
                            new Unit[]{}
                    ),
            });
             */

        } catch (CalculatorExceptions.IllegalFormulaException | CalculatorExceptions.UnitConversionException e) {
            throw new RuntimeException("UnitDictionary initialization failed: " + e.toString());
        }
    }

    @Override
    public Unit.NormalUnit getSecond() {
        return this.second;
    }

    protected class Celsius implements Unit {
        private final int unitId;
        private final int dimensionId;
        private final String unitName;
        private final NormalUnit kelvin;
        private final UnitDirectory parentUnitDictionary;

        Celsius(int unitId, int dimensionId, String unitName, NormalUnit kelvin, UnitDirectory parentUnitDictionary) {
            this.unitId = unitId;
            this.dimensionId = dimensionId;
            this.unitName = unitName;
            this.kelvin = kelvin;
            this.parentUnitDictionary = parentUnitDictionary;
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

        @NonNull
        @Override
        public CalculatorNumber.BigDecimalNumber makeCalculatableFromThisUnit(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            if (! input.getCombinedUnit().equals(new CombinedUnit(this, this.parentUnitDictionary))) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this, this.parentUnitDictionary), this.parentUnitDictionary);
            }
            try {
                return input.removeCombinedUnit().add(new CalculatorNumber.BigDecimalNumber("273.15", parentUnitDictionary)).applyCombinedUnit(new CombinedUnit(this.kelvin, this.parentUnitDictionary));
            } catch (CalculatorExceptions.IllegalFormulaException e) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this.kelvin, this.parentUnitDictionary), this.parentUnitDictionary);
            }
        }

        @NonNull
        @Override
        public CalculatorNumber.BigDecimalNumber makeThisUnitFromCalculatable(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            if (! input.getCombinedUnit().isCalculatable()) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this, this.parentUnitDictionary), this.parentUnitDictionary);
            }
            CalculatorNumber.BigDecimalNumber expressionInKelvin = input.convertUnit(new CombinedUnit(kelvin, this.parentUnitDictionary));
            try {
                return expressionInKelvin.removeCombinedUnit().subtract(new CalculatorNumber.BigDecimalNumber("273.15", parentUnitDictionary)).applyCombinedUnit(new CombinedUnit(this, this.parentUnitDictionary));
            } catch (CalculatorExceptions.IllegalFormulaException e) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this.kelvin, this.parentUnitDictionary), this.parentUnitDictionary);
            }
        }
    }

    protected class Fahrenheit implements Unit {
        private final int unitId;
        private final int dimensionId;
        private final String unitName;
        private final NormalUnit kelvin;

        private final UnitDirectory parentUnitDictionary;

        Fahrenheit(int unitId, int dimensionId, String unitName, NormalUnit kelvin, UnitDirectory parentUnitDictionary)  {
            this.unitId = unitId;
            this.dimensionId = dimensionId;
            this.unitName = unitName;
            this.kelvin = kelvin;
            this.parentUnitDictionary = parentUnitDictionary;
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

        @NonNull
        @Override
        public CalculatorNumber.BigDecimalNumber makeCalculatableFromThisUnit(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            if (! input.getCombinedUnit().equals(new CombinedUnit(this, this.parentUnitDictionary))) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this, this.parentUnitDictionary), this.parentUnitDictionary);
            }
            try {
                return input.removeCombinedUnit().subtract(new CalculatorNumber.BigDecimalNumber("32", parentUnitDictionary)).divide(new CalculatorNumber.BigDecimalNumber("1.8", parentUnitDictionary)).
                        add(new CalculatorNumber.BigDecimalNumber("273.15", parentUnitDictionary)).applyCombinedUnit(new CombinedUnit(this.kelvin, this.parentUnitDictionary));

            } catch (CalculatorExceptions.DivisionByZeroException e) {
                throw new RuntimeException("Tried to divide by 1.8, but DivisionByZeroException occurred");

            } catch (CalculatorExceptions.IllegalFormulaException e) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this.kelvin, this.parentUnitDictionary), this.parentUnitDictionary);
            }
        }

        @NonNull
        @Override
        public CalculatorNumber.BigDecimalNumber makeThisUnitFromCalculatable(CalculatorNumber.BigDecimalNumber input) throws CalculatorExceptions.UnitConversionException {
            if (! input.getCombinedUnit().isCalculatable()) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this, this.parentUnitDictionary), this.parentUnitDictionary);
            }
            CalculatorNumber.BigDecimalNumber expressionInKelvin = input.convertUnit(new CombinedUnit(kelvin, this.parentUnitDictionary));
            try {
                return expressionInKelvin.removeCombinedUnit().subtract(new CalculatorNumber.BigDecimalNumber("273.15", parentUnitDictionary)).
                        multiply(new CalculatorNumber.BigDecimalNumber("1.8", parentUnitDictionary)).add(new CalculatorNumber.BigDecimalNumber("32", parentUnitDictionary)).
                        applyCombinedUnit(new CombinedUnit(this, this.parentUnitDictionary));
            } catch (CalculatorExceptions.IllegalFormulaException e) {
                throw new CalculatorExceptions.UnitConversionException(input.getCombinedUnit(), new CombinedUnit(this.kelvin, this.parentUnitDictionary), this.parentUnitDictionary);
            }
        }
    }
}
