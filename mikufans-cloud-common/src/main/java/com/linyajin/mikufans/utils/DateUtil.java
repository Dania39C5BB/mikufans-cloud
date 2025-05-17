package com.linyajin.mikufans.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DateUtil {

    private static final Object lockObj = new Object();
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
        if (tl == null) {
            synchronized (lockObj) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    tl = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }

        return tl.get();
    }

    public static String format(Date date, String pattern) {
        return getSdf(pattern).format(date);
    }

    public static Date parse(String dateStr, String pattern) {
        try {
            return getSdf(pattern).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    //获取前一天的日期
    public static String getBeforeDayDate(Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -day);
        return format(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 获取当前日期之前指定天数的日期列表
     * 如果今天是2023-05-20，调用getBeforeDays(3)将返回：
     * ["2023-05-17", "2023-05-18", "2023-05-19"]
     *
     * @param beforeDay 需要获取的天数（从当前日期往前推算）
     * @return 返回包含指定天数日期的字符串列表，格式为"yyyy-MM-dd"
     */
    public static List<String> getBeforeDays(Integer beforeDay) {
        // 获取当前日期作为结束日期
        LocalDate endDate = LocalDate.now();
        // 初始化一个空的ArrayList来存储日期字符串列表
        List<String> dateList = new ArrayList<>();
        // 定义日期格式化器，格式为"年-月-日"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 从beforeDay开始倒序循环到1
        for (int i = beforeDay; i > 0; i--) {
            // 计算当前日期减去i天后的日期，并格式化为字符串，然后添加到列表中
            dateList.add(endDate.minusDays(i).format(formatter));
        }

        return dateList;
    }
}
