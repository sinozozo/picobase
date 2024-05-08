package com.picobase.persistence.fexpr;

import java.util.ArrayList;
import java.util.List;

public class FexprParser {

    private static final String ERR_EMPTY = "empty filter expression";
    private static final String ERR_INCOMPLETE = "invalid or incomplete filter expression";

    private static final int STEP_BEFORE_SIGN = 0;
    private static final int STEP_SIGN = 1;
    private static final int STEP_AFTER_SIGN = 2;
    private static final int STEP_JOIN = 3;

    /**
     * // Parse parses the provided text and returns its processed AST
     * // in the form of `ExprGroup` slice(s).
     * //
     * // Comments and whitespaces are ignored.
     *
     * @param text
     * @return
     * @throws Exception
     */
    public static List<ExprGroup> parse(String text) throws Exception {
        List<ExprGroup> result = new ArrayList<>();
        FexprScanner scanner = new FexprScanner(text);

        int step = STEP_BEFORE_SIGN;
        JoinOp join = JoinOp.AND;

        Expr expr = new Expr();

        while (true) {
            var t = scanner.scan();

            if (t.getType() == TokenType.EOF) {
                break;
            }

            if (t.getType() == TokenType.WS || t.getType() == TokenType.Comment) {
                continue;
            }

            if (t.getType() == TokenType.Group) {
                List<ExprGroup> groupResult = parse(t.getLiteral());
                if (!groupResult.isEmpty()) {
                    result.add(new ExprGroup(join, groupResult));
                }

                step = STEP_JOIN;
                continue;
            }

            switch (step) {
                case STEP_BEFORE_SIGN:
                    if (t.getType() != TokenType.Identifier && t.getType() != TokenType.Text && t.getType() != TokenType.Number) {
                        throw new Exception(String.format("expected left operand (identifier, text or number), got %s (%s)", t.getLiteral(), t.getType()));
                    }

                    expr = new Expr(t, null, null);

                    step = STEP_SIGN;
                    break;

                case STEP_SIGN:
                    if (t.getType() != TokenType.Sign) {
                        throw new Exception(String.format("expected a sign operator, got %s (%s)", t.getLiteral(), t.getType()));
                    }

                    expr.setOp(SignOp.getEnumValue(t.getLiteral()));
                    step = STEP_AFTER_SIGN;
                    break;

                case STEP_AFTER_SIGN:
                    if (t.getType() != TokenType.Identifier && t.getType() != TokenType.Text && t.getType() != TokenType.Number) {
                        throw new Exception(String.format("expected right operand (identifier, text or number), got %s (%s)", t.getLiteral(), t.getType()));
                    }

                    expr.setRight(t);
                    result.add(new ExprGroup(join, expr));

                    step = STEP_JOIN;
                    break;
                case STEP_JOIN:
                    if (t.getType() != TokenType.Join) {
                        throw new Exception(String.format("expected && or ||, got %s (%s)", t.getLiteral(), t.getType()));
                    }

                    join = JoinOp.AND;
                    if (t.getLiteral().equals("||")) {
                        join = JoinOp.OR;
                    }

                    step = STEP_BEFORE_SIGN;
                    break;

            }

        }


        if (step != STEP_JOIN) {
            if (result.isEmpty() && expr.isEmpty()) {
                throw new Exception(ERR_EMPTY);
            }

            throw new Exception(ERR_INCOMPLETE);
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        String text = "(name = \"John Doe\" || age > 21) && active = true";
        List<ExprGroup> result = parse(text);

        System.out.println(result);
    }


}
