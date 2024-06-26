package com.picobase.fun.strategy;

import java.time.ZonedDateTime;
import java.util.function.BiFunction;

@FunctionalInterface
public interface PbNextTimestampByCronExpressionFunction extends BiFunction<String, ZonedDateTime, Long> {


}
