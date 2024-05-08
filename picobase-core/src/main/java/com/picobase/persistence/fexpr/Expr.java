package com.picobase.persistence.fexpr;

public class Expr {

    private Token left;
    private SignOp op;
    private Token right;

    public Expr() {
    }

    public Expr(Token left, SignOp op, Token right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public Token getLeft() {
        return left;
    }

    public void setLeft(Token left) {
        this.left = left;
    }

    public SignOp getOp() {
        return op;
    }

    public void setOp(SignOp op) {
        this.op = op;
    }

    public Token getRight() {
        return right;
    }

    public void setRight(Token right) {
        this.right = right;
    }

    public boolean isEmpty() {
        return op == null && left == null && right == null;
    }

    @Override
    public String toString() {
        return String.format("Expr{left=%s, op=%s, right=%s}", left, op, right);
    }
}