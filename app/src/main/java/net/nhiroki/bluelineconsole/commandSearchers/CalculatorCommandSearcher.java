package net.nhiroki.bluelineconsole.commandSearchers;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commands.calculator.Calculator;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorExceptions;
import net.nhiroki.bluelineconsole.commands.calculator.CalculatorNumber;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;

public class CalculatorCommandSearcher implements CommandSearcher {
    @Override
    public void refresh(Context context) {

    }

    @Override
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List <CandidateEntry> cands = new ArrayList<>();
        if (Calculator.seemsExpression(s)) {
            cands.add(new CalculatorCandidateEntry(s, context));
        }
        return cands;
    }

    @Override
    public void close() {}

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() {}

    private class CalculatorCandidateEntry implements CandidateEntry {
        private String title;
        private String resultText;
        private String subText;

        CalculatorCandidateEntry(String s, Context context) {
            char ltr = 0x200e;
            title = ltr + s;
            try {
                CalculatorNumber res = Calculator.calculate(s);
                resultText = ltr + "= " + res.toString();
                subText = String.format(context.getString(R.string.calculator_precision_format), getPrecisionText(context, res.getPrecision()));

            } catch (CalculatorExceptions.IllegalFormulaException e) {
                resultText = "= ...";
                subText = null;

            } catch (CalculatorExceptions.DivisionByZeroException e) {
                resultText = context.getString(R.string.calculator_error_division_by_zero);
                subText = null;

            } catch (CalculatorExceptions.CalculationException e) {
                // Keep this function new not to happen this error
                resultText = "Unknown calculation error";
                subText = null;
            }
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public boolean hasLongView() {
            return true;
        }

        @Override
        public View getView(Context context) {
            LinearLayout ret = new LinearLayout(context);
            ret.setOrientation(LinearLayout.VERTICAL);
            ret.setTextDirection(View.TEXT_DIRECTION_LTR);

            TextView resultView = new TextView(context);

            resultView.setText(resultText);
            resultView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
            resultView.setTypeface(null, Typeface.BOLD);
            resultView.setTextColor(ContextCompat.getColor(context, R.color.baseText));

            ret.addView(resultView);

            if (subText != null) {
                TextView precisionView = new TextView(context);
                precisionView.setText(subText);
                precisionView.setTypeface(null, Typeface.BOLD);
                ret.addView(precisionView);
            }

            return ret;
        }

        private String getPrecisionText(Context context, int precision) {
            switch (precision) {
                case CalculatorNumber.Precision.PRECISION_NO_ERROR:
                    return context.getString(R.string.calculator_precision_no_error);
                case CalculatorNumber.Precision.PRECISION_SCALE_20:
                    return context.getString(R.string.calculator_precision_rounding);
                default:
                    throw new RuntimeException("Unknown precision ID");
            }
        }


        @Override
        public boolean hasEvent() {
            return false;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return null;
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
        }
    }
}
