package net.nhiroki.bluelineconsole;

import org.junit.Test;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.math.BigDecimal;

import net.nhiroki.bluelineconsole.commands.calculator.Calculator;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorExceptions;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorNumber;
import net.nhiroki.bluelineconsole.commands.calculator.Operator;
import net.nhiroki.bluelineconsole.commands.calculator.ParseResult;


public class CalculatorUnitTests {
    private static void assertBigDecimal(String expression, String result, int precision)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        assertEquals(result, Calculator.calculate(expression).generateFinalDecimalValue().generateFinalString());
        assertEquals(precision, Calculator.calculate(expression).getPrecision());
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

        // If no precise operation available, scale is set to 20
        assertBigDecimal("1/3", "0.33333333333333333333", CalculatorNumber.Precision.PRECISION_SCALE_20);
        assertBigDecimal("2/3", "0.66666666666666666667", CalculatorNumber.Precision.PRECISION_SCALE_20);

        // Unit conversion function test
        assertBigDecimal("1 inch in cm", "2.54 cm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 inches in cm", "2.54 cm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
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
        assertBigDecimal("1 inch in cm", "2.54 cm", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 ft in inch", "12 inch", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 yard in ft", "3 ft", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 mile in yard", "1760 yard", CalculatorNumber.Precision.PRECISION_NO_ERROR);
        assertBigDecimal("1 kgf in N", "9.80665 N", CalculatorNumber.Precision.PRECISION_NO_ERROR);
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
}