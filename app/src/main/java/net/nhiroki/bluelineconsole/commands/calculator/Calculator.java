package net.nhiroki.bluelineconsole.commands.calculator;

import net.nhiroki.bluelineconsole.commands.calculator.units.CombinedUnit;
import net.nhiroki.bluelineconsole.commands.calculator.units.UnitDirectory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Calculator {
    public static List<CalculatorNumber> calculate(String expression) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        CombinedUnit finalUnit = null;

        String[] split_expression = expression.split(" ");
        if (split_expression.length > 2 && split_expression[split_expression.length - 2].equals("in")) {
            try {
                final String[] unitNameSplit = split_expression[split_expression.length - 1].split("/");

                if (unitNameSplit.length == 1) {
                    finalUnit = UnitDirectory.getInstance().getCombinedUnitFromName(unitNameSplit[0]).explicitCombinedUnit();
                    StringBuilder expressionBuilder = new StringBuilder();
                    for (int i = 0; i < split_expression.length - 2; ++i) {
                        expressionBuilder.append(split_expression[i]).append(" ");
                    }
                    expression = expressionBuilder.toString();
                }
                if (unitNameSplit.length == 2) {
                    CombinedUnit positive = unitNameSplit[0].isEmpty() ? new CombinedUnit() : UnitDirectory.getInstance().getCombinedUnitFromName(unitNameSplit[0]).explicitCombinedUnit();
                    finalUnit = positive.divide(UnitDirectory.getInstance().getCombinedUnitFromName(unitNameSplit[1])).explicitCombinedUnit();
                    StringBuilder expressionBuilder = new StringBuilder();
                    for (int i = 0; i < split_expression.length - 2; ++i) {
                        expressionBuilder.append(split_expression[i]).append(" ");
                    }
                    expression = expressionBuilder.toString();
                }
            } catch (CalculatorExceptions.IllegalFormulaException e) {
                // Do nothing and continue
            }
        }

        final ParseResult parseResult = calculateInBigDecimal(expression, 0);
        if (parseResult.getConsumedChars() == expression.length()) {
            CalculatorNumber result = (CalculatorNumber)parseResult.getFormulaPart();
            if (finalUnit != null) {
                result = result.convertUnit(finalUnit);
            }
            List <CalculatorNumber> ret = new ArrayList<>();
            ret.add(result.generateFinalDecimalValue());

            if (finalUnit == null) {
                CalculatorNumber secondary = result.generatePossiblyPreferredOutputValue();
                if (secondary != null) {
                    ret.add(secondary);
                }
            }

            return ret;
        }

        throw new CalculatorExceptions.IllegalFormulaException();
    }

    public static boolean seemsExpression(final String expression) {
        if (expression.length() == 0) {
            return false;
        }

        switch (expression.charAt(0)) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '(':
            case ')':
            case '-':
                return true;

            default:
                return false;
        }
    }

    private static int skipSpaces(final String expressions, final int start) {
        int ret = 0;

        while (start + ret < expressions.length() && (expressions.charAt(start + ret) == ' ' || expressions.charAt(start + ret) == '\t')){
            ++ret;
        }
        return ret;
    }

    private static ParseResult readFormulaPart(String expression, final int start) throws CalculatorExceptions.IllegalFormulaException {
        if (start >= expression.length()) {
            throw new CalculatorExceptions.IllegalFormulaException();
        }

        switch (expression.charAt(start)) {
            case '+':
                return new ParseResult(new Operator.AddOperator(), 1);

            case '-':
                return new ParseResult(new Operator.SubtractOperator(), 1);

            case '*':
                return new ParseResult(new Operator.MultiplyOperator(), 1);

            case '/':
                return new ParseResult(new Operator.DivideOperator(), 1);

            case '0':
                if (start + 1 < expression.length()) {
                    if ('0' <= expression.charAt(start + 1) && expression.charAt(start + 1) <= '9') {
                        // Coming here means that the user provided a number with leading zeros (e.g.: 03, 00123, 00.3, 002.718).
                        // Some users, or even this Calculator in future, may consider leading zero as Octal expression (e.g. 0100 means sixty four, while 100 means a hundred).
                        // To prevent confusion, leading zero is currently not allowed.
                        throw new CalculatorExceptions.IllegalFormulaException();
                    }
                }
                // Confirmed non-existence of ambiguous leading zero. Continuing.
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {
                BigDecimal ret = BigDecimal.ZERO;
                BigDecimal multiplier = BigDecimal.ONE;

                boolean seenPeriod = false;

                int curPos = start;
                for (; curPos < expression.length() && (('0' <= expression.charAt(curPos) && expression.charAt(curPos) <= '9') || expression.charAt(curPos) == '.'); ++curPos) {
                    if (expression.charAt(curPos) == '.') {
                        if (seenPeriod) {
                            throw new CalculatorExceptions.IllegalFormulaException();
                        }
                        seenPeriod = true;
                        continue;
                    }

                    final BigDecimal currentDigit = new BigDecimal(expression.charAt(curPos) - '0');
                    if (seenPeriod) {
                        //noinspection BigDecimalMethodWithoutRoundingCalled
                        multiplier = multiplier.divide(BigDecimal.TEN);
                        ret = ret.add(currentDigit.multiply(multiplier));
                    } else {
                        ret = ret.multiply(BigDecimal.TEN).add(currentDigit);
                    }
                }
                while (curPos < expression.length() && expression.charAt(curPos) == ' ') {
                    ++curPos;
                }
                StringBuilder unitName = new StringBuilder();
                while (curPos < expression.length() && expression.charAt(curPos) == ' ') {
                    ++curPos;
                }
                for (; curPos < expression.length() && (('a' <= expression.charAt(curPos) && expression.charAt(curPos) <= 'z') || ('A' <= expression.charAt(curPos) && expression.charAt(curPos) <= 'Z')); ++curPos) {
                    unitName.append(expression.charAt(curPos));
                }
                CombinedUnit unit = null;
                if (unitName.length() > 0) {
                    unit = UnitDirectory.getInstance().getCombinedUnitFromName(unitName.toString()).explicitCombinedUnitIfSingle();

                    if (curPos < expression.length() - 1 && expression.charAt(curPos) == '/') {
                        StringBuilder unitName2 = new StringBuilder();
                        int tmpCurPos = curPos + 1;
                        for (; tmpCurPos < expression.length() && (('a' <= expression.charAt(tmpCurPos) && expression.charAt(tmpCurPos) <= 'z') || ('A' <= expression.charAt(tmpCurPos) && expression.charAt(tmpCurPos) <= 'Z')); ++tmpCurPos) {
                            unitName2.append(expression.charAt(tmpCurPos));
                        }

                        if (unitName2.length() > 0) {
                            try {
                                CombinedUnit unit2 = UnitDirectory.getInstance().getCombinedUnitFromName(unitName2.toString()).explicitCombinedUnitIfSingle();
                                unit = unit.divide(unit2);
                                curPos = tmpCurPos;
                            } catch (CalculatorExceptions.IllegalFormulaException e) {
                                // We did not see anything after slash
                            }
                        }
                    }
                }
                return new ParseResult(new CalculatorNumber.BigDecimalNumber(ret, CalculatorNumber.Precision.PRECISION_NO_ERROR, unit), curPos - start);
            }

            default:
                throw new CalculatorExceptions.IllegalFormulaException();
        }
    }

    private static void popUntilLowerPriorityInBigDecimal(Stack<FormulaPart> expressionStack, Stack<Operator> operatorsStack, final int priority)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {

        while ( !operatorsStack.empty() &&
                operatorsStack.peek() instanceof Operator.InfixOperator &&
                operatorsStack.peek().getPriority() > priority) {

            // Concatenate from left while priorities are the same
            // Currently all Operators are InfixOperator.
            Stack<CalculatorNumber> innerNumberStack = new Stack<>();
            Stack<Operator> innerOperatorStack = new Stack<>();

            final int currentPriority = operatorsStack.peek().getPriority();

            while (operatorsStack.size() > 0 && operatorsStack.peek().getPriority() == currentPriority) {
                Operator.InfixOperator op = (Operator.InfixOperator) operatorsStack.pop();
                if (!(expressionStack.peek() instanceof CalculatorNumber)) {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
                innerNumberStack.push((CalculatorNumber) expressionStack.pop());
                innerOperatorStack.push(op);
                if (expressionStack.pop() != op) {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
            }

            if (!(expressionStack.peek() instanceof CalculatorNumber)) {
                throw new CalculatorExceptions.IllegalFormulaException();
            }

            CalculatorNumber.BigDecimalNumber cn = (CalculatorNumber.BigDecimalNumber) expressionStack.pop();

            while (innerOperatorStack.size() > 0) {
                Operator.InfixOperator op = (Operator.InfixOperator) innerOperatorStack.pop();
                CalculatorNumber.BigDecimalNumber cn2 = (CalculatorNumber.BigDecimalNumber) innerNumberStack.pop();

                cn = op.operate(cn, cn2);
            }
            expressionStack.push(cn);
        }
    }

    // TODO: more readable implementation
    private static ParseResult calculateInBigDecimal (final String expression, final int start)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        int curPos = start;
        final Stack<FormulaPart> expressionStack = new Stack<>();
        final Stack<Operator> operatorsStack = new Stack<>();

        while (curPos < expression.length()) {
            final int spaces = skipSpaces(expression, curPos);
            if (spaces > 0) {
                curPos += spaces;
                continue;
            }

            final char curChar = expression.charAt(curPos);

            if (curPos == start && curChar == '-') {
                ++curPos;
                if (expression.length() == curPos) {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }

                if (expression.charAt(curPos) == '(') {
                    ++curPos;
                    final ParseResult intermediate = calculateInBigDecimal(expression, curPos);

                    curPos += intermediate.getConsumedChars();
                    final CalculatorNumber.BigDecimalNumber tmp = (CalculatorNumber.BigDecimalNumber) intermediate.getFormulaPart();
                    expressionStack.push(tmp.multiply(new CalculatorNumber.BigDecimalNumber(new BigDecimal("-1"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)));
                    if (expression.length() == curPos || expression.charAt(curPos) != ')') {
                        throw new CalculatorExceptions.IllegalFormulaException();
                    }
                    ++curPos;

                    continue;
                }

                final ParseResult part = readFormulaPart(expression, curPos);
                if (part.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber) {
                    CalculatorNumber.BigDecimalNumber tmp = (CalculatorNumber.BigDecimalNumber) part.getFormulaPart();
                    expressionStack.push(tmp.applyMinusJustToNumber());
                } else {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }

                curPos += part.getConsumedChars();

                continue;
            }

            if (curChar == '(') {
                ++curPos;
                final ParseResult intermediate = calculateInBigDecimal(expression, curPos);

                curPos += intermediate.getConsumedChars();
                expressionStack.push(intermediate.getFormulaPart());

                if (expression.length() == curPos || expression.charAt(curPos) != ')') {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
                ++curPos;

                continue;
            }

            if (curChar == ')') {
                // ')' corresponding to the '(' which I read does not come here.
                break;
            }

            final ParseResult part = readFormulaPart(expression, curPos);
            curPos += part.getConsumedChars();

            boolean tmpOk = false;
            if (part.getFormulaPart() instanceof Operator) {
                tmpOk = true;
                if (part.getFormulaPart() instanceof Operator.InfixOperator) {
                    if (expressionStack.empty() || ! (expressionStack.peek() instanceof CalculatorNumber)) {
                        throw new CalculatorExceptions.IllegalFormulaException();
                    }
                    popUntilLowerPriorityInBigDecimal(expressionStack, operatorsStack, ((Operator.InfixOperator) part.getFormulaPart()).getPriority());
                } else {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
            }

            if (part.getFormulaPart() instanceof CalculatorNumber) {
                if (! (part.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber)) {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
                tmpOk = true;
                if (!expressionStack.empty() && !(expressionStack.peek() instanceof Operator.InfixOperator)) {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
            }

            if (!tmpOk) {
                throw new CalculatorExceptions.IllegalFormulaException();
            }

            expressionStack.push(part.getFormulaPart());

            if (part.getFormulaPart() instanceof Operator) {
                operatorsStack.push((Operator) part.getFormulaPart());
            }
        }

        popUntilLowerPriorityInBigDecimal(expressionStack, operatorsStack, 0);

        if (expressionStack.size() == 1) {
            if (expressionStack.get(0) instanceof CalculatorNumber.BigDecimalNumber) {
                CalculatorNumber result = (CalculatorNumber)expressionStack.get(0);

                return new ParseResult(result, curPos - start);
            }
        }

        throw new CalculatorExceptions.IllegalFormulaException();
    }
}
