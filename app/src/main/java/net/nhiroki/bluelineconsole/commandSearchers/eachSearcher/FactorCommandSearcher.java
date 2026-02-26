package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class FactorCommandSearcher implements CommandSearcher {
    @Override
    public void refresh(Context context) { }

    @Override
    public void close() { }

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() { }

    private static class FactorCandidateEntry implements CandidateEntry {
        private final Context context;
        private final String query;

        private FactorCandidateEntry(String query, Context context) {
            this.context = context;
            this.query = query;
        }

        @Override
        public String getTitle() {
            return this.query;
        }

        private String factorToStr (BigInteger n) {
            if (n.compareTo(BigInteger.ZERO) != 1) {
                return context.getString(R.string.factor_error_not_positive_number);
            }

            List<Pair<BigInteger, Long>> factors = new ArrayList<>();
            BigInteger two = BigInteger.valueOf(2);
            BigInteger divisor = BigInteger.valueOf(2);
            BigInteger maxDivisor = BigInteger.valueOf(1000000);
            boolean divisorIsTwo = true;
            while (n.compareTo(BigInteger.ONE) == 1) {
                Long count = 0l;
                while (n.mod(divisor).equals(BigInteger.ZERO)) {
                    n = n.divide(divisor);
                    count += 1;
                }

                if (count != 0) {
                    factors.add(new Pair<>(divisor, count));
                }

                if (divisorIsTwo) {
                    divisor = BigInteger.valueOf(3);
                    divisorIsTwo = false;
                } else {
                    if (divisor.compareTo(maxDivisor) == 1) {
                        return context.getString(R.string.factor_error_factor_too_large, maxDivisor.toString());
                    }
                    divisor = divisor.add(two);
                }
            }

            String ret = "";
            for (int i = 0; i < factors.size(); ++i) {
                if (i != 0) {
                    ret += " × ";
                }
                long exp = factors.get(i).second;
                if (exp == 1l) {
                    ret += factors.get(i).first;
                } else {
                    StringBuilder expStr = new StringBuilder();
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
                        expStr.insert(0, c);
                        exp /= 10;
                    }
                    ret += factors.get(i).first + expStr.toString();
                }
            }
            return ret;
        }

        @Override
        public View getView(MainActivity mainActivity) {
            LinearLayout ret = new LinearLayout(mainActivity);
            ret.setOrientation(LinearLayout.VERTICAL);
            ret.setTextDirection(View.TEXT_DIRECTION_LTR);

            String[] splitted = this.query.split(" ");
            for (int i = 1; i < splitted.length; ++i) {
                String numStr = splitted[i];
                String result;
                try {
                    BigInteger number = new BigInteger(splitted[i]);
                    numStr = number.toString();
                    result = factorToStr(number);
                } catch (NumberFormatException e) {
                    result = context.getString(R.string.factor_error_not_valid_integer);
                }

                final TypedValue baseTextColor = new TypedValue();
                mainActivity.getTheme().resolveAttribute(R.attr.bluelineconsoleBaseTextColor, baseTextColor, true);

                TextView tv = new TextView(context);
                tv.setText(numStr + ": " + result);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                tv.setTextColor(baseTextColor.data);
                int paddingStart = (int) (12 * mainActivity.getResources().getDisplayMetrics().density);
                tv.setPaddingRelative(paddingStart, 0, 0, 0);

                ret.addView(tv);
            }

            return ret;
        }

        @Override
        public boolean hasLongView() {
            return true;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return null;
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
        }

        @Override
        public boolean hasEvent() {
            return false;
        }

        @Override
        public boolean isSubItem() {
            return false;
        }

        @Override
        public boolean viewIsRecyclable() {
            return true;
        }
    }

    @NonNull
    @Override
    public List<CandidateEntry> searchCandidateEntries(String query, Context context) {
        ArrayList<CandidateEntry> ret = new ArrayList<>();

        if (query.startsWith("factor ")) {
            ret.add(new FactorCandidateEntry(query, context));
        }
        return ret;
    }
}
