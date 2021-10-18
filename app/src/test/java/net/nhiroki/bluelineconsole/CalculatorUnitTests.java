package net.nhiroki.bluelineconsole;

import org.junit.Test;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.List;

import net.nhiroki.lib.bluelinecalculator.Calculator;
import net.nhiroki.lib.bluelinecalculator.CalculatorExceptions;
import net.nhiroki.lib.bluelinecalculator.CalculatorNumber;
import net.nhiroki.lib.bluelinecalculator.Operator;
import net.nhiroki.lib.bluelinecalculator.ParseResult;


public class CalculatorUnitTests {
    private static void assertBigDecimal(String expression, String result, int precision)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        CalculatorNumber actual = Calculator.calculate(expression).get(0);

        assertEquals(result, actual.generateFinalString());
        assertEquals(precision, actual.getPrecision());
    }

    private static void assertSecondaryBigDecimal(String expression, String result, int precision)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        List<CalculatorNumber> actual = Calculator.calculate(expression);
        if (result == null && actual.size() == 1) {
            return;
        }

        assertEquals(result, actual.get(1).generateFinalString());
        assertEquals(precision, actual.get(1).getPrecision());
    }

    @Test
    public void calculatorTest() throws Exception {
        assertBigDecimal("3", "3", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("((3) )", "3", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3+5", "8", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("31*5", "155", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("31*15", "465", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2+3*4", "14", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3*4+2", "14", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3+4+2", "9", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3+4*2", "11", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("     3 + \t4\t*   \t 2", "11", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3-4*2", "-5", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("39-41*23", "-904", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("100000000000000000000000001*100000000000000000000000001", "10000000000000000000000000200000000000000000000000001", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("-100000000000000000000000001*100000000000000000000000001", "-10000000000000000000000000200000000000000000000000001", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("100000000000000000000000001*(-100000000000000000000000001)", "-10000000000000000000000000200000000000000000000000001", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(3-4)*2", "-2", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2*(3-4)", "-2", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2*(-(3+5))", "-16", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(2*(-(3+5)))", "-16", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("21*(-70)", "-1470", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("-21*(-70)", "1470", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("0.2*0.5", "0.1", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3-2-2", "-1", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("13-2-2+4-2*3/2-1*3", "7", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("13.3-2+1.8-2/5*3", "11.9", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1/4", "0.25", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1/8", "0.125", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("36.0*0.0", "0", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("36.0  *  0.0", "0", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("100000000000000000000.00000 * 100000000000000000000", "10000000000000000000000000000000000000000", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1/0.000000000000000000000000000001", "1000000000000000000000000000000", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1+3.14159/(1.000000000000000000000000000001-1)", "3141590000000000000000000000001", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3/40000000000000000000000000000000000000000", "7.5E-41", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("123456789*(1+3.4)/(1.30-1/5)", "493827156", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 / 3.0 * 3.00", "1", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 / (1+2.0) * 3.00", "1", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 / 6 + 3 / 8 + 1 / 3 + 1 / 8", "1", CalculatorNumber.Precision.PRECISION_NO_ERROR);

        // If no precise operation available, scale is set to 20
        assertBigDecimal("1/3", "0.33333333333333333333", CalculatorNumber.Precision.PRECISION_SCALE_20);
        assertBigDecimal("2/3", "0.66666666666666666667", CalculatorNumber.Precision.PRECISION_SCALE_20);

        // Unit conversion function test
        assertBigDecimal("1 inch in cm", "2.54 cm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 inches in cm", "2.54 cm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 / (1 cm / 1 inch)", "2.54", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 inch in mm", "50.8 mm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 inches   in mm", "50.8 mm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 cm + 3 inch", "9.62 cm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 cm + 3 inch in mm", "96.2 mm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 m + 3 inch in mm", "2076.2 mm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 inch + 3 cm", "3.18110236220472440945 inch", CalculatorNumber.Precision.PRECISION_SCALE_20);
        assertBigDecimal("2 inch / 5 cm", "1.016", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 cm * 3 inch / 5 s", "3.048 cm²/s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("6cm/2s + 3inch/1s", "10.62 cm/s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(6cm/2s + 3inch/1s)/1s/1s", "10.62 cm/s³", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(300km+20000m)/150minute in km/h", "128 km/h", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(1mm / 3inch) / (1cm / 9yard)", "10.8", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 s in s", "2 s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 m in m", "2 m", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("42 seconds in minute", "0.7 minute", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("420 seconds in minute", "7 minute", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("54km/1h in m/s", "15 m/s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("54km/h", "54 km/h", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("54km/h in m/s", "15 m/s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("54km/hour in m/s", "15 m/s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("100mph in km/h", "160.9344 km/h", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("16.09344km/h in mph", "10 mile/h", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1kgf / 9.80665 / 1m * 1s * 1s", "1 kg", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(3*9.80665) * 1kg * 1m / 1s / 1s in kgf", "3 kgf", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(1kg*1m/1s/1s+1kgf)/1s", "10.80665 m⋅kg/s³", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1kg*1m/1s/1s+1kgf", "10.80665 N", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(1mile/1km)-1", "0.609344", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(1mile/1km)+2", "3.609344", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(1mile/1km)*1", "1.609344", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1*(1mile/1km)", "1.609344", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("(1mile/1km)/1", "1.609344", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1/(1km/1mile)", "1.609344", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("10km*((1mile/1km)-1) in km", "6.09344 km", CalculatorNumber.Precision.PRECISION_NO_ERROR);

        // Uncalculatable units system test
        assertBigDecimal("2 celsius + 1 kelvin", "276.15 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 kelvin + 1 celsius", "276.15 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 celsius + 1 celsius", "549.3 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 celsius - 1 celsius", "1 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("212 fahrenheit - 99 celsius", "1 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("0 celsius * 2", "546.3 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("2 * 0 celsius", "546.3 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("0 celsius / 273.15 kelvin", "1", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("273.15 kelvin / 0 celsius", "1", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3 celsius in kelvin", "276.15 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("3 kelvin in celsius", "-270.15 celsius", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("32 fahrenheit + 1 kelvin", "274.15 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kelvin + 212 fahrenheit", "374.15 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("32 fahrenheit + 32 fahrenheit", "546.3 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("32 fahrenheit in kelvin", "273.15 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("212 fahrenheit in kelvin", "373.15 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("273.15 kelvin in fahrenheit", "32 fahrenheit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("373.15 kelvin in fahrenheit", "212 fahrenheit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("-100 celsius in fahrenheit", "-148 fahrenheit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("-123 kelvin in kelvin", "-123 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("-123 celsius in celsius", "-123 celsius", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("-123 fahrenheit in fahrenheit", "-123 fahrenheit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("123 kelvin in kelvin", "123 K", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("123 celsius in celsius", "123 celsius", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("123 fahrenheit in fahrenheit", "123 fahrenheit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("123 celsius + 0K in celsius", "123 celsius", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("123 fahrenheit + 0K in fahrenheit", "123 fahrenheit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("0 celsius in fahrenheit", "32 fahrenheit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("100 celsius in fahrenheit", "212 fahrenheit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("32 fahrenheit in celsius", "0 celsius", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("212 fahrenheit in celsius", "100 celsius", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("273.15 celsius / 0 celsius", "2", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Mbit in kbit", "1000 kbit", CalculatorNumber.Precision.PRECISION_NO_ERROR);

        // Superscript generation test
        assertBigDecimal("1m", "1 m", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m", "1 m²", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m", "1 m³", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m", "1 m⁴", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m*1m", "1 m⁵", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m*1m*1m", "1 m⁶", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m*1m*1m*1m", "1 m⁷", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m*1m*1m*1m*1m", "1 m⁸", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m*1m*1m*1m*1m*1m", "1 m⁹", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m*1m*1m*1m*1m*1m*1m", "1 m¹⁰", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m", "1 m³¹", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*" +
                         "1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*" +
                         "1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*" +
                         "1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*1m*" +
                         "1m*1m*1m",
                         "1 m¹²³", CalculatorNumber.Precision.PRECISION_NO_ERROR);

        // Each unit rate test
        assertBigDecimal("1 hour in minute", "60 minute", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 minute in second", "60 s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 km in m", "1000 m", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 m in cm", "100 cm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 cm in mm", "10 mm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 nmi in m", "1852 m", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kg in g", "1000 g", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 liter in milliliter", "1000 milliliter", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1cm * 1cm * 1cm in milliliter", "1 milliliter", CalculatorNumber.Precision.PRECISION_NO_ERROR);

        // Each unit rate test for complex SI units
        assertBigDecimal("1V*1A", "1 W", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1V*1A*1s", "1 J", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Hz", "1 /s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1/1s", "1 /s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("7.2km/h/1m in Hz", "2 /s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("7.2km/h/1m in /s", "2 /s", CalculatorNumber.Precision.PRECISION_NO_ERROR);

        // Each unit rate test for imperial units
        assertBigDecimal("1 yd in m", "0.9144 m", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 ft in inch", "12 inch", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 yard in ft", "3 ft", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 mile in yard", "1760 yard", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 lb in kg", "0.45359237 kg", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 lb in oz", "16 oz", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 mph * 1h", "1 mile", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 lbf / 1 lb", "9.80665 m/s²", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 lbf / 1 inch / 1inch", "1 lbf/inch²", CalculatorNumber.Precision.PRECISION_NO_ERROR);


        // Each unit rate test for informational units
        assertBigDecimal("1 KiB in byte", "1024 byte", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 MiB in KiB", "1024 KiB", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 GiB in MiB", "1024 MiB", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 TiB in GiB", "1024 GiB", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kB in byte", "1000 byte", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 MB in kB", "1000 kB", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 GB in MB", "1000 MB", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 TB in GB", "1000 GB", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Kibit in bit", "1024 bit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Mibit in Kibit", "1024 Kibit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Gibit in Mibit", "1024 Mibit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Tibit in Gibit", "1024 Gibit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kbit in byte", "125 byte", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kbit in bit", "1000 bit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Mbit in kbit", "1000 kbit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Gbit in Mbit", "1000 Mbit", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 Tbit in Gbit", "1000 Gbit", CalculatorNumber.Precision.PRECISION_NO_ERROR);

        /// Each unit rate test for complex units
        assertBigDecimal("1 cal in J", "4.184 J", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kcal in kJ", "4.184 kJ", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kcal in J", "4184 J", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 knot * 1h", "1 nmi", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kgf in N", "9.80665 N", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kgf / 1 kg", "9.80665 m/s²", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kg * 1m / 1s / 1s in N", "1 N", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 byte / 1bps", "8 s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1kB / 1kbps", "8 s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1MB / 1Mbps", "8 s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1GB / 1Gbps", "8 s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1TB / 1Tbps", "8 s", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1m*1m*1m in liter", "1000 liter", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1liter in milliliter", "1000 milliliter", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1V*1A*1s", "1 J", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1V*1A", "1 W", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1V/1A", "1 ohm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1kV*1A", "1 kW", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1000V*1A in kW", "1 kW", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1kJ in J", "1000 J", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1Pa*1m*1m", "1 N", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1hPa in Pa", "100 Pa", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1kPa in Pa", "1000 Pa", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1MPa in kPa", "1000 kPa", CalculatorNumber.Precision.PRECISION_NO_ERROR);

        // Preferred units test
        assertSecondaryBigDecimal("31h / 3", "10:20:00", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("10h / 3", "3:20:00", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("1h / 3", "0:20:00", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("1h / 30", "0:02:00", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("1h / 120", "0:00:30", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("1h / 1200", "0:00:03", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("12h / 12000", "0:00:03.6", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertEquals(Calculator.calculate("1h / 12000").size(), 1);
        assertSecondaryBigDecimal("30km / (270km/h)", "0:06:40", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("25h", "1d 01:00:00", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("35h", "1d 11:00:00", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertSecondaryBigDecimal("2lb*40inch*40inch/1s/1s", "0.93644689097344 J", CalculatorNumber.Precision.PRECISION_NO_ERROR);
    }

    @Test
    public void readFormulaPartTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = Calculator.class.getDeclaredMethod("readFormulaPart", String.class, int.class);
        method.setAccessible(true);

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+4", 0);
            assertEquals(1, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals("3/1 No Unit with Precision 1", ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).toString());
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+4", 2);
            assertEquals(1, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals("4/1 No Unit with Precision 1", ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).toString());
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+3.142", 1);
            assertEquals(1, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof Operator.AddOperator);
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3-3.142", 1);
            assertEquals(1, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof Operator.SubtractOperator);
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3*3.142", 1);
            assertEquals(1, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof Operator.MultiplyOperator);
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3/3.142", 1);
            assertEquals(1, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof Operator.DivideOperator);
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+3.142", 2);
            assertEquals(5, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals("3.142/1 No Unit with Precision 1", ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).toString());
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+3.142*342", 2);
            assertEquals(5, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals("3.142/1 No Unit with Precision 1", ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).toString());
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+3.142 inch*342", 2);
            assertEquals(10, res.getConsumedChars());
            assertTrue(res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals("3.142/1 inch with Precision 1", ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).toString());
        }
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorIllegalFormatLeadingZero() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("01");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorIllegalFormat1() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("(3))");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorIllegalFormat2() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("(3");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorIllegalFormat3() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("(3+(5-3)");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorIllegalFormat4() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("2*-(3+5)");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorIllegalFormat5() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("2*-3");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorIllegalFormatLeadingZero1() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("01");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorIllegalFormatLeadingZero2() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("01.3");
    }

    @Test(expected = CalculatorExceptions.DivisionByZeroException.class)
    public void calculatorDivByZero1() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1/0");
    }

    @Test(expected = CalculatorExceptions.DivisionByZeroException.class)
    public void calculatorDivByZero2() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("3+(1/(3-4+1))");
    }

    @Test(expected = CalculatorExceptions.DivisionByZeroException.class)
    public void calculatorDivByZero3() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1/(-273.15 celsius)");
    }

    @Test(expected = CalculatorExceptions.DivisionByZeroException.class)
    public void calculatorDivByZeroScale20_1() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("(1/3)+(1/(3-4+1))");
    }

    @Test(expected = CalculatorExceptions.DivisionByZeroException.class)
    public void calculatorDivByZeroScale20_2() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("(1/(3-4+1))+1/3");
    }

    @Test(expected = CalculatorExceptions.UnitConversionException.class)
    public void calculatorUnitConversionError1() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1inch + 2s");
    }

    @Test(expected = CalculatorExceptions.UnitConversionException.class)
    public void calculatorUnitConversionError2() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1inch in s");
    }

    @Test(expected = CalculatorExceptions.UnitConversionException.class)
    public void calculatorUnitConversionError3() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1m / 1s + 1s / 1m");
    }

    @Test(expected = CalculatorExceptions.UnitConversionException.class)
    public void calculatorUnitConversionError4() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1 celsius + 1m");
    }

    @Test(expected = CalculatorExceptions.UnitConversionException.class)
    public void calculatorUnitConversionError5() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1 m + 1 celsius");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorUnitConversionError6() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1 celsius/s");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorUnitConversionError7() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1 s/celsius");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorUnitOnly1() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("m");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorUnitOnly2() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1 s m");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorUnitOnly3() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1 s * m");
    }

    @Test(expected = CalculatorExceptions.IllegalFormulaException.class)
    public void calculatorUnitOnly4() throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        Calculator.calculate("1 s/m m");
    }
}
