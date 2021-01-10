package net.nhiroki.bluelineconsole.calculator;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.nhiroki.bluelineconsole.commands.calculator.Calculator;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorExceptions;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorNumber;
import net.nhiroki.bluelineconsole.commands.calculator.Operator;
import net.nhiroki.bluelineconsole.commands.calculator.ParseResult;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CalculatorTest {
    private static void assertBigDecimal(String expression, String result, int precision)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        assertEquals(result, Calculator.calculate(expression).getBigDecimal().toString());
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

        // If no precise operation available, scale is set to 20
        assertBigDecimal("1 / 3.0 * 3.00", "0.99999999999999999999", CalculatorNumber.Precision.PRECISION_SCALE_20);
        assertBigDecimal("1 / (1+2.0) * 3.00", "0.99999999999999999999", CalculatorNumber.Precision.PRECISION_SCALE_20);
        assertBigDecimal("1/3", "0.33333333333333333333", CalculatorNumber.Precision.PRECISION_SCALE_20);
        assertBigDecimal("2/3", "0.66666666666666666667", CalculatorNumber.Precision.PRECISION_SCALE_20);
    }

    @Test
    public void readFormulaPartTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = Calculator.class.getDeclaredMethod("readFormulaPart", String.class, int.class);
        method.setAccessible(true);

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+4", 0);
            assertEquals(1, res.getConsumedChars());
            assertEquals(true, res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals(true, ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).getBigDecimal().equals(new BigDecimal("3")));
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+4", 2);
            assertEquals(1, res.getConsumedChars());
            assertEquals(true, res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals(true, ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).getBigDecimal().equals(new BigDecimal("4")));
        }


        {
            ParseResult res = (ParseResult) method.invoke(null, "3+3.142", 1);
            assertEquals(1, res.getConsumedChars());
            assertEquals(true, res.getFormulaPart() instanceof Operator.AddOperator);
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3-3.142", 1);
            assertEquals(1, res.getConsumedChars());
            assertEquals(true, res.getFormulaPart() instanceof Operator.SubtractOperator);
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3*3.142", 1);
            assertEquals(1, res.getConsumedChars());
            assertEquals(true, res.getFormulaPart() instanceof Operator.MultiplyOperator);
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3/3.142", 1);
            assertEquals(1, res.getConsumedChars());
            assertEquals(true, res.getFormulaPart() instanceof Operator.DivideOperator);
        }


        {
            ParseResult res = (ParseResult) method.invoke(null, "3+3.142", 2);
            assertEquals(5, res.getConsumedChars());
            assertEquals(true, res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals(true, ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).getBigDecimal().equals(new BigDecimal("3.142")));
        }

        {
            ParseResult res = (ParseResult) method.invoke(null, "3+3.142*342", 2);
            assertEquals(5, res.getConsumedChars());
            assertEquals(true, res.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber);
            assertEquals(true, ((CalculatorNumber.BigDecimalNumber) res.getFormulaPart()).getBigDecimal().equals(new BigDecimal("3.142")));
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
}
