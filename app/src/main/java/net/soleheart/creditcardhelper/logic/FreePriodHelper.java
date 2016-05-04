package net.soleheart.creditcardhelper.logic;

import java.util.Calendar;

/**
 * Created by Kevin on 2016/2/24.
 */
public class FreePriodHelper {
    public static int calcFreePeriod(int billDay, int payDay) {
        Calendar calendar = Calendar.getInstance();
        int currDay = calendar.get(Calendar.DATE);

        // FIXME 暂时不考虑账单日，还款日超出2月日期的问题
        Calendar calBillDate = Calendar.getInstance();
        calBillDate.set(Calendar.DATE, billDay);

        Calendar calPayDate = Calendar.getInstance();
        calPayDate.set(Calendar.DATE, payDay);

        // 观察发现，有些银行(浦发银行)将账单日当天计为当前账单周期，而有些银行(招商)则计入下个账单周期
        // 保险起见，采用前者的算法
        if (payDay > billDay) {    // 账单日与还款日在同一个月
            if (currDay > billDay) {
                adjustCalendarByAddMonth(calPayDate, 1);
            }
        } else {    // 还款日在下一个月
            if (currDay > billDay) {
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

    /**
     * 计算下一个账单日/还款日距离今天的天数。
     *
     * @param specifiedDay 账单日/还款日日期，如5表示每个月5号
     * @return 下一个账单日/还款日距离今天的天数
     */
    public static int calcNextDateDaysLeft(int specifiedDay) {
        Calendar currCalc = Calendar.getInstance();
        int currDay = currCalc.get(Calendar.DATE);

        Calendar destCalc = Calendar.getInstance();
        destCalc.set(Calendar.DATE, specifiedDay);

        if (currDay > specifiedDay) {
            adjustCalendarByAddMonth(destCalc, 1);
        }

        // 计算间隔时间
        long periodInMs = destCalc.getTimeInMillis() - currCalc.getTimeInMillis();
        int days = Math.round(periodInMs / 24 / 60 / 60 / 1000);

        return days;
    }
}
