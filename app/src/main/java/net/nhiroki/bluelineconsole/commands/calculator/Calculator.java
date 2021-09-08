package net.nhiroki.bluelineconsole.commands.calculator;

import net.nhiroki.bluelineconsole.commands.calculator.units.CombinedUnit;
import net.nhiroki.bluelineconsole.commands.calculator.units.UnitDirectory;

import java.math.BigDecimal;
import java.util.Stack;

public class Calculator {
    public static CalculatorNumber calculate(String expression) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        CombinedUnit finalUnit = null;

        String[] split_expression = expression.split(" ");
        if (split_expression.length > 2 && split_expression[split_expression.length - 2].equals("in")) {
            try {
                String[] unitnameSplit = split_expression[split_expression.length - 1].split("/");

                if (unitnameSplit.length == 1) {
                    finalUnit = UnitDirectory.getInstance().getCombinedUnitFromName(unitnameSplit[0]);
                    expression = "";
                    for (int i = 0; i < split_expression.length - 2; ++i) {
                        expression += split_expression[i] + " ";
                    }
                }
                if (unitnameSplit.length == 2) {
                    finalUnit = UnitDirectory.getInstance().getCombinedUnitFromName(unitnameSplit[0]).divide(UnitDirectory.getInstance().getCombinedUnitFromName(unitnameSplit[1]));
                    expression = "";
                    for (int i = 0; i < split_expression.length - 2; ++i) {
                        expression += split_expression[i] + " ";
                    }
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
            result = result.generateFinalDecimalValue();

            return result;
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

                int curpos = start;
                for (; curpos < expression.length() && (('0' <= expression.charAt(curpos) && expression.charAt(curpos) <= '9') || expression.charAt(curpos) == '.'); ++curpos) {
                    if (expression.charAt(curpos) == '.') {
                        if (seenPeriod) {
                            throw new CalculatorExceptions.IllegalFormulaException();
                        }
                        seenPeriod = true;
                        continue;
                    }

                    final BigDecimal currentDigit = new BigDecimal(expression.charAt(curpos) - '0');
                    if (seenPeriod) {
                        //noinspection BigDecimalMethodWithoutRoundingCalled
                        multiplier = multiplier.divide(BigDecimal.TEN);
                        ret = ret.add(currentDigit.multiply(multiplier));
                    } else {
                        ret = ret.multiply(BigDecimal.TEN).add(currentDigit);
                    }
                }
                for (; curpos < expression.length() && expression.charAt(curpos) == ' '; ++curpos) {
                }
                String unitname = "";
                while (curpos < expression.length() && expression.charAt(curpos) == ' ') {
                    ++curpos;
                }
                for (; curpos < expression.length() && (('a' <= expression.charAt(curpos) && expression.charAt(curpos) <= 'z') || ('A' <= expression.charAt(curpos) && expression.charAt(curpos) <= 'Z')); ++curpos) {
                    unitname += expression.charAt(curpos);
                }
                CombinedUnit unit = null;
                if (! unitname.isEmpty()) {
                    unit = UnitDirectory.getInstance().getCombinedUnitFromName(unitname);
                }
                if (! unitname.isEmpty() && curpos < expression.length() - 1 && expression.charAt(curpos) == '/') {
                    String unitname2 = "";
                    int tmpcurpos = curpos + 1;
                    for (; tmpcurpos < expression.length() && (('a' <= expression.charAt(tmpcurpos) && expression.charAt(tmpcurpos) <= 'z') || ('A' <= expression.charAt(tmpcurpos) && expression.charAt(tmpcurpos) <= 'Z')); ++tmpcurpos) {
                        unitname2 += expression.charAt(tmpcurpos);
                    }

                    if (! unitname2.isEmpty()) {
                        try {
                            CombinedUnit unit2 = UnitDirectory.getInstance().getCombinedUnitFromName(unitname2);
                            unit = unit.divide(unit2);
                            curpos = tmpcurpos;
                        } catch (CalculatorExceptions.IllegalFormulaException e) {
                            // We did not see anything after slash
                        }
                    }
                }
                return new ParseResult(new CalculatorNumber.BigDecimalNumber(ret, CalculatorNumber.Precision.PRECISION_NO_ERROR, unit), curpos - start);
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
    private static ParseResult calculateInBigDecimal (String expression, final int start)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        int curPos = start;
        Stack<FormulaPart> expressionStack = new Stack<>();
        Stack<Operator> operatorsStack = new Stack<>();

        while (true) {
            if (curPos >= expression.length()) {
                break;
            }

            int spaces = skipSpaces(expression, curPos);
            if (spaces > 0) {
                curPos += spaces;
                continue;
            }

            char curChar = expression.charAt(curPos);

            if (curPos == start && curChar == '-') {
                ++curPos;
                if (expression.length() == curPos) {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }

                if (expression.charAt(curPos) == '(') {
                    ++curPos;
                    ParseResult intermediate = calculateInBigDecimal(expression, curPos);

                    curPos += intermediate.getConsumedChars();
                    CalculatorNumber.BigDecimalNumber tmp = (CalculatorNumber.BigDecimalNumber) intermediate.getFormulaPart();
                    expressionStack.push(tmp.multiply(new CalculatorNumber.BigDecimalNumber(new BigDecimal("-1"), CalculatorNumber.Precision.PRECISION_NO_ERROR, null)));
                    if (expression.length() == curPos || expression.charAt(curPos) != ')') {
                        throw new CalculatorExceptions.IllegalFormulaException();
                    }
                    ++curPos;

                    continue;
                }

                ParseResult part = readFormulaPart(expression, curPos);
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
                ParseResult intermediate = calculateInBigDecimal(expression, curPos);

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

            ParseResult part = readFormulaPart(expression, curPos);
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
