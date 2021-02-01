package com.xiaoxin.idworker;

import com.xiaoxin.idworker.strategy.DayPrefixRandomCodeStrategy;

public class DayCode {
    static com.xiaoxin.idworker.RandomCodeStrategy strategy;

    static {
        DayPrefixRandomCodeStrategy dayPrefixCodeStrategy = new DayPrefixRandomCodeStrategy("yyMM");
        dayPrefixCodeStrategy.setMinRandomSize(7);
        dayPrefixCodeStrategy.setMaxRandomSize(7);
        strategy = dayPrefixCodeStrategy;
        strategy.init();
    }

    public static synchronized String next() {
        return String.format("%d-%04d-%07d", com.xiaoxin.idworker.Id.getWorkerId(), strategy.prefix(), strategy.next());
    }
}
