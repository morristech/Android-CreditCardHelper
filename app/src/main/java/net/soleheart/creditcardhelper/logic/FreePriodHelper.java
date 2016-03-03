package net.soleheart.creditcardhelper.logic;

import java.util.Calendar;

/**
 * Created by Kevin on 2016/2/24.
 */
public class FreePriodHelper {
    public static int calcFreePeriod(int billDate, int payDate) {
        Calendar calendar = Calendar.getInstance();
        int currDay = calendar.get(Calendar.DATE);

        // FIXME 暂时不考虑账单日，还款日超出2月日期的问题
        Calendar calBillDate = Calendar.getInstance();
        calBillDate.set(Calendar.DATE, billDate);

        Calendar calPayDate = Calendar.getInstance();
        calPayDate.set(Calendar.DATE, payDate);

        if (payDate > billDate) {    // 账单日与还款日在同一个月
            if (currDay >= billDate) {
                updatePayDate(calPayDate, 1);
            }
        } else {    // 还款日在下一个月
            if (currDay >= billDate) {
                updatePayDate(calPayDate, 2);
            } else {
                updatePayDate(calPayDate, 1);
            }
        }

        // 计算间隔时间
        long periodInMs = calPayDate.getTimeInMillis() - calendar.getTimeInMillis();
        int days = Math.round(periodInMs / 24 / 60 / 60 / 1000);

        return days;
    }

    static void updatePayDate(Calendar calPayDate, int stepParam) {
        int oriPayMonth = calPayDate.get(Calendar.MONTH);
        if ((oriPayMonth + stepParam) > Calendar.DECEMBER) {
            int oriPayYear = calPayDate.get(Calendar.YEAR);
            calPayDate.set(Calendar.YEAR, oriPayYear + 1);
        }
        calPayDate.set(Calendar.MONTH, (oriPayMonth + stepParam) % Calendar.DECEMBER);
    }
}
