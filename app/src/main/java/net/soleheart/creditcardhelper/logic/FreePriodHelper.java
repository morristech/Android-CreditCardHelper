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
                adjustCalendarByAddMonth(calPayDate, 1);
            }
        } else {    // 还款日在下一个月
            if (currDay >= billDate) {
                adjustCalendarByAddMonth(calPayDate, 2);
            } else {
                adjustCalendarByAddMonth(calPayDate, 1);
            }
        }

        // 计算间隔时间
        long periodInMs = calPayDate.getTimeInMillis() - calendar.getTimeInMillis();
        int days = Math.round(periodInMs / 24 / 60 / 60 / 1000);

        return days;
    }

    static void adjustCalendarByAddMonth(Calendar calendar, int offsetMonth) {
        int oriMonth = calendar.get(Calendar.MONTH);
        int totalMonth = oriMonth + offsetMonth;
        int offsetYear = totalMonth / 12;

        int destMonth = totalMonth % 12;
        if (destMonth < 0) {
            destMonth += 12;
            offsetYear -= 1;
        }

        int oriYear = calendar.get(Calendar.YEAR);
        calendar.set(Calendar.YEAR, oriYear + offsetYear);
        calendar.set(Calendar.MONTH, destMonth);
    }
}
