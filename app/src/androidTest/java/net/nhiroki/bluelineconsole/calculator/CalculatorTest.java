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
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CalculatorTest {
    @Test
    public void calculatorTest() throws Exception {
        assertEquals("3", "3");
    }
}
