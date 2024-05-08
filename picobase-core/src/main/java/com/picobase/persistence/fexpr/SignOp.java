package com.picobase.persistence.fexpr;


import java.util.HashMap;
import java.util.Map;

public enum SignOp {
    SignEq("="),
    SignNeq("!="),
    SignLike("~"),
    SignNlike("!~"),
    SignLt("<"),
    SignLte("<="),
    SignGt(">"),
    SignGte(">="),

    // array/any operators
    SignAnyEq("?="),
    SignAnyNeq("?!="),
    SignAnyLike("?~"),
    SignAnyNlike("?!~"),
    SignAnyLt("?<"),
    SignAnyLte("?<="),
    SignAnyGt("?>"),
    SignAnyGte("?>=");
    private static final Map<String, SignOp> symbolMap = new HashMap<>();

    static {
        for (SignOp op : SignOp.values()) {
            symbolMap.put(op.symbol, op);
        }
    }

    private final String symbol;

    SignOp(String symbol) {
        this.symbol = symbol;
    }


    public static SignOp getEnumValue(String symbol) {
        SignOp signOp = symbolMap.get(symbol);
        if (signOp == null) {
            throw new IllegalArgumentException(String.format("unknown sign operator: %s", symbol));
        }
        return signOp;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "SignOp{" +
                "symbol='" + symbol + '\'' +
                '}';
    }
}