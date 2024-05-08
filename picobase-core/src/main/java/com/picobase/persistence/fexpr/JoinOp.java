package com.picobase.persistence.fexpr;


public enum JoinOp {
    AND("&&"),
    OR("||");

    private final String symbol;

    JoinOp(String symbol) {
        this.symbol = symbol;
    }

    public static JoinOp getEnumValue(String symbol) {
        for (JoinOp op : JoinOp.values()) {
            if (op.symbol.equals(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException(String.format("unknown join operator: %s", symbol));
    }

    public String getSymbol() {
        return symbol;
    }
}