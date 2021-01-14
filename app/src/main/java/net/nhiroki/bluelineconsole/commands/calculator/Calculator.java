package net.nhiroki.bluelineconsole.commands.calculator;

import java.math.BigDecimal;
import java.util.Stack;

public class Calculator {
    public static CalculatorNumber calculate(final String expression) throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.CalculationException {
        try {
            final ParseResult parseResult = calculateInBigDecimal(expression, 0, CalculatorNumber.Precision.PRECISION_NO_ERROR);
            if (parseResult.getConsumedChars() == expression.length()) {
                return (CalculatorNumber)parseResult.getFormulaPart();
            }
        } catch (CalculatorExceptions.PrecisionNotAchievableException e) {
            // Just trying next below
        }

        try {
            final ParseResult parseResult = calculateInBigDecimal(expression, 0, CalculatorNumber.Precision.PRECISION_SCALE_20);
            if (parseResult.getConsumedChars() == expression.length()) {
                return (CalculatorNumber) parseResult.getFormulaPart();
            }
        } catch (CalculatorExceptions.PrecisionNotAchievableException e) {

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

    private static BigDecimal normalizeBigDecimal(final BigDecimal in, final int targetPrecision) {
        if (in.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal ret = in.stripTrailingZeros();
        if (targetPrecision == CalculatorNumber.Precision.PRECISION_NO_ERROR && ret.scale() < 0) {
            //noinspection BigDecimalMethodWithoutRoundingCalled
            ret = ret.setScale(0);
        }

        return ret;
    }

    private static int skipSpaces(final String expressions, final int start) {
        int ret = 0;

        while (start + ret < expressions.length() && (expressions.charAt(start + ret) == ' ' || expressions.charAt(start + ret) == '\t')){
            ++ret;
        }
        return ret;
    }

    private static ParseResult readFormulaPart(final String expression, final int start) throws CalculatorExceptions.IllegalFormulaException {
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
                return new ParseResult(new CalculatorNumber.BigDecimalNumber(ret, CalculatorNumber.Precision.PRECISION_NO_ERROR), curpos - start);
            }

            default:
                throw new CalculatorExceptions.IllegalFormulaException();
        }
    }

    private static void popUntilLowerPriorityInBigDecimal(Stack<FormulaPart> expressionStack, Stack<Operator> operatorsStack, final int priority, final int targetPrecision)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.PrecisionNotAchievableException, CalculatorExceptions.CalculationException {

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

            CalculatorNumber cn = (CalculatorNumber) expressionStack.pop();

            while (innerOperatorStack.size() > 0) {
                Operator.InfixOperator op = (Operator.InfixOperator) innerOperatorStack.pop();
                CalculatorNumber cn2 = innerNumberStack.pop();

                cn = op.operate(cn.getBigDecimal(), cn2.getBigDecimal(), targetPrecision);
            }
            expressionStack.push(cn);
        }
    }

    // TODO: more readable implementation
    private static ParseResult calculateInBigDecimal (final String expression, final int start, final int targetPrecision)
            throws CalculatorExceptions.IllegalFormulaException, CalculatorExceptions.PrecisionNotAchievableException, CalculatorExceptions.CalculationException {
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
                    ParseResult intermediate = calculateInBigDecimal(expression, curPos, targetPrecision);

                    curPos += intermediate.getConsumedChars();
                    expressionStack.push(new CalculatorNumber.BigDecimalNumber(((CalculatorNumber.BigDecimalNumber) intermediate.getFormulaPart()).getBigDecimal().multiply(new BigDecimal("-1")), targetPrecision));
                    if (expression.length() == curPos || expression.charAt(curPos) != ')') {
                        throw new CalculatorExceptions.IllegalFormulaException();
                    }
                    ++curPos;

                    continue;
                }

                ParseResult part = readFormulaPart(expression, curPos);
                if (part.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber) {
                    expressionStack.push(new CalculatorNumber.BigDecimalNumber(((CalculatorNumber.BigDecimalNumber)part.getFormulaPart()).getBigDecimal().multiply(new BigDecimal("-1")), targetPrecision));
                } else {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }

                curPos += part.getConsumedChars();

                continue;
            }

            if (curChar == '(') {
                ++curPos;
                ParseResult intermediate = calculateInBigDecimal(expression, curPos, targetPrecision);

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
                    popUntilLowerPriorityInBigDecimal(expressionStack, operatorsStack, ((Operator.InfixOperator) part.getFormulaPart()).getPriority(), targetPrecision);
                } else {
                    throw new CalculatorExceptions.IllegalFormulaException();
                }
            }

            if (part.getFormulaPart() instanceof CalculatorNumber) {
                if (! (part.getFormulaPart() instanceof CalculatorNumber.BigDecimalNumber)) {
                    throw new CalculatorExceptions.PrecisionNotAchievableException();
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

        popUntilLowerPriorityInBigDecimal(expressionStack, operatorsStack, 0, targetPrecision);

        if (expressionStack.size() == 1) {
            if (expressionStack.get(0) instanceof CalculatorNumber.BigDecimalNumber) {
                CalculatorNumber result = (CalculatorNumber)expressionStack.get(0);

                return new ParseResult(new CalculatorNumber.BigDecimalNumber(normalizeBigDecimal(result.getBigDecimal(), targetPrecision), targetPrecision), curPos - start);
            }
        }

        throw new CalculatorExceptions.IllegalFormulaException();
    }
}
