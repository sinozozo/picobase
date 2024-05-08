package com.picobase.persistence.fexpr;

public class ExprGroup {

    private JoinOp join;
    private Object item;

    public ExprGroup() {
    }

    public ExprGroup(JoinOp join, Object item) {
        this.join = join;
        this.item = item;
    }

    public JoinOp getJoin() {
        return join;
    }

    public void setJoin(JoinOp join) {
        this.join = join;
    }

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return String.format("ExprGroup{join=%s, item=%s}", join, item);
    }
}