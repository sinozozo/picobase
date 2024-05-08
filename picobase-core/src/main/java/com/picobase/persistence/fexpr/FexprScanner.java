package com.picobase.persistence.fexpr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;


public class FexprScanner {

    private static final Pattern IDENTIFIER_REGEX = Pattern.compile("^[\\@\\#\\_]?[\\w\\.\\:]*\\w+$");
    private static final char EOF = (char) -1;

    private final BufferedReader sqlBuf;

    public FexprScanner(String input) {
        this.sqlBuf = new BufferedReader(new StringReader(input), input.length());
    }

    /**
     * Scans the input and returns the corresponding token.
     *
     * @return the token generated from the input.
     */
    public Token scan() throws IOException {
        sqlBuf.mark(1);
        var ch = (char) sqlBuf.read();


        if (isWhitespace(ch)) {
            sqlBuf.reset();
            return scanWhitespace();
        }

        if (isGroupStart(ch)) {
            sqlBuf.reset();
            return scanGroup();
        }

        if (isIdentifierStart(ch)) {
            sqlBuf.reset();
            return scanIdentifier();
        }

        if (isNumberStart(ch)) {
            sqlBuf.reset();
            return scanNumber();
        }

        if (isTextStart(ch)) {
            sqlBuf.reset();
            return scanText();
        }

        if (isSignStart(ch)) {
            sqlBuf.reset();
            return scanSign();
        }

        if (isJoinStart(ch)) {
            sqlBuf.reset();
            return scanJoin();
        }

        if (isCommentStart(ch)) {
            sqlBuf.reset();
            return scanComment();
        }

        if (ch == EOF) {
            return new Token(TokenType.EOF, "");
        }

        throw new IllegalArgumentException("unexpected character :" + ch);
    }

    private Token scanWhitespace() throws IOException {
        var buf = new StringBuilder();
        // Reads every subsequent whitespace character into the buffer.
        // Non-whitespace runes and EOF will cause the loop to exit.
        while (true) {
            sqlBuf.mark(1);
            var ch = (char) sqlBuf.read();

            if (ch == EOF) {
                break;
            }

            if (!isWhitespace(ch)) {
                sqlBuf.reset();
                break;
            }

            // write the whitespace
            buf.append(ch);
        }

        return new Token(TokenType.WS, buf.toString());

    }

    private Token scanGroup() throws IOException {
        var buf = new StringBuilder();
        var firstChar = (char) sqlBuf.read();
        var openGroups = 1;

        while (true) {
            sqlBuf.mark(1);
            var ch = (char) sqlBuf.read();

            if (ch == EOF) {
                break;
            }

            if (isGroupStart(ch)) {
                // nested group
                openGroups++;
                buf.append(ch);
            } else if (isTextStart(ch)) {
                sqlBuf.reset();
                var token = scanText();
                // quote the literal to preserve the text start/end runes
                buf.append("\"").append(token.getLiteral()).append("\"");
            } else if (isGroupEnd(ch)) {
                openGroups--;
                if (openGroups <= 0) {
                    // main group end
                    break;
                } else {
                    buf.append(ch);
                }
            } else {
                buf.append(ch);
            }
        }

        if (!isGroupStart(firstChar) || openGroups > 0) {
            throw new IllegalArgumentException("invalid formatted group - missing " + openGroups + " closing bracket(s)");
        }

        return new Token(TokenType.Group, buf.toString());
    }


    /**
     * scanIdentifier consumes all contiguous ident
     *
     * @return
     * @throws IOException
     */
    private Token scanIdentifier() throws IOException {
        StringBuilder buf = new StringBuilder();

        while (true) {
            sqlBuf.mark(1);
            var ch = (char) sqlBuf.read();

            if (ch == EOF) {
                break;
            }

            if (!isIdentifierStart(ch) && !isDigit(ch) && ch != '.' && ch != ':') {
                sqlBuf.reset();
                break;
            }

            // write the ident
            buf.append(ch);
        }
        if (!isIdentifier(buf.toString())) {
            throw new IllegalArgumentException("Invalid identifier " + buf.toString());
        }

        return new Token(TokenType.Identifier, buf.toString());
    }

    private Token scanNumber() throws IOException {
        StringBuilder buf = new StringBuilder();

        // read the number first rune to skip the sign (if exist)
        buf.append((char) sqlBuf.read());

        while (true) {
            sqlBuf.mark(1);
            var ch = (char) sqlBuf.read();
            if (ch == EOF) {
                break;
            }

            if (!isDigit(ch) && ch != '.') {
                sqlBuf.reset();
                break;
            }

            buf.append(ch);
        }

        if (!isNumber(buf.toString())) {
            throw new IllegalArgumentException("invalid number " + buf.toString());
        }

        return new Token(TokenType.Number, buf.toString());
    }

    private Token scanText() throws IOException {

        StringBuilder buf = new StringBuilder();
        // read the first rune to determine the quotes type
        var firstCh = (char) sqlBuf.read();
        buf.append(firstCh);// 这里注意 append 方法 的参数是 char、int 类型处理的方式不同 ， 如果是int 那么会转换成string进行append操作
        char prevCh = '\u0000'; //default value
        boolean hasMatchingQuotes = false;

        // Read every subsequent text rune into the buffer.
        // EOF and matching unescaped ending quote will cause the loop to exit.

        while (true) {
            sqlBuf.mark(1);
            var ch = (char) sqlBuf.read();
            if (ch == EOF) {
                break;
            }

            // write the text rune
            buf.append(ch);

            if (ch == firstCh && prevCh != '\\') {
                hasMatchingQuotes = true;
                break;
            }
            prevCh = ch;

        }


        if (!hasMatchingQuotes) {
            throw new IllegalArgumentException("invalid quoted text " + buf.toString());
        } else {
            // unquote
            buf.deleteCharAt(0);
            buf.deleteCharAt(buf.length() - 1);

            // remove escaped quotes prefix (aka. \)
            var firstChStr = String.valueOf(firstCh);

            var literal = buf.toString().replace("\\" + firstChStr, firstChStr);

            return new Token(TokenType.Text, literal);

        }


    }

    private Token scanSign() throws IOException {
        StringBuilder buf = new StringBuilder();

        // Read every subsequent sign rune into the buffer.
        // Non-sign runes and EOF will cause the loop to exit.
        while (true) {
            sqlBuf.mark(1);
            var ch = (char) sqlBuf.read();

            if (ch == EOF) {
                break;
            }

            if (!isSignStart(ch)) {
                sqlBuf.reset();
                break;
            }

            buf.append(ch);
        }


        String literal = buf.toString();
        if (!isSignOperator(literal)) {
            throw new IllegalArgumentException("Invalid sign operator: " + literal);
        }

        return new Token(TokenType.Sign, literal);
    }

    private Token scanJoin() throws IOException {
        StringBuilder buf = new StringBuilder();
        while (true) {
            sqlBuf.mark(1);
            var ch = (char) sqlBuf.read();

            if (ch == EOF) {
                break;
            }

            if (!isJoinStart(ch)) {
                sqlBuf.reset();
                break;
            }

            buf.append(ch);
        }

        String literal = buf.toString();
        if (!isJoinOperator(literal)) {
            throw new IllegalArgumentException("Invalid join operator: " + literal);
        }

        return new Token(TokenType.Join, literal);
    }


    private Token scanComment() throws IOException {
        StringBuilder buf = new StringBuilder();

        // Read the first 2 characters without writting them to the buffer.
        if (!isCommentStart((char) sqlBuf.read()) || !isCommentStart((char) sqlBuf.read())) {
            throw new IllegalArgumentException("invalid comment");
        }

        // Read every subsequent comment text rune into the buffer.
        // \n and EOF will cause the loop to exit.
        while (true) {
            var ch = (char) sqlBuf.read();
            if (ch == EOF || ch == '\n') {
                break;
            }
            buf.append(ch);
        }

        String literal = buf.toString().trim();
        return new Token(TokenType.Comment, literal);
    }

    private boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }

    private boolean isIdentifierStart(char ch) {
        return Character.isLetter(ch) || ch == '_' || ch == '@' || ch == '#';
    }

    private boolean isIdentifierPart(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == ':';
    }

    private boolean isNumberStart(char ch) {
        return Character.isDigit(ch) || ch == '-';
    }

    private boolean isDigit(char ch) {
        return Character.isDigit(ch);
    }

    private boolean isTextStart(char ch) {
        return ch == '\'' || ch == '"';
    }

    private boolean isSignStart(char ch) {
        return ch == '=' || ch == '!' || ch == '>' || ch == '<' || ch == '~' || ch == '?';
    }

    private boolean isSignPart(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '=' || ch == '!' || ch == '>' || ch == '<' || ch == '~' || ch == '?';
    }

    private boolean isJoinStart(char ch) {
        return ch == '&' || ch == '|';
    }

    private boolean isJoinPart(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '&' || ch == '|';
    }

    private boolean isGroupStart(char ch) {
        return ch == '(';
    }

    private boolean isGroupEnd(char ch) {
        return ch == ')';
    }

    private boolean isCommentStart(char ch) {
        return ch == '/';
    }

    private boolean isIdentifier(String literal) {
        return IDENTIFIER_REGEX.matcher(literal).matches();
    }

    private boolean isNumber(String literal) {
        try {
            Double.parseDouble(literal);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isSignOperator(String literal) {
        switch (SignOp.getEnumValue(literal)) {
            case SignEq,
                    SignNeq,
                    SignLt,
                    SignLte,
                    SignGt,
                    SignGte,
                    SignLike,
                    SignNlike,
                    SignAnyEq,
                    SignAnyNeq,
                    SignAnyLike,
                    SignAnyNlike,
                    SignAnyLt,
                    SignAnyLte,
                    SignAnyGt,
                    SignAnyGte:
                return true;
            default:
                return false;
        }
    }

    private boolean isJoinOperator(String literal) {
        return literal.equals("&&") || literal.equals("||");
    }


    public static void main(String[] args) throws IOException {
        String input = "name = 'John Doe' AND age > 21";
        FexprScanner scanner = new FexprScanner(input);

        Token token;
        do {
            token = scanner.scan();
            System.out.println(token);
        } while (token.getType() != TokenType.EOF);


        String sql = "SELECT * FROM table WHERE column = '😄生僻中文字'";

        for (int i = 0; i < sql.length(); i++) {
            int codePoint = sql.codePointAt(i);

            // 判断是否为代理对(表情符号的组成部分）
            if (Character.isSupplementaryCodePoint(codePoint)) {//是补充codepoint
                char[] chars = Character.toChars(codePoint);
                System.out.println(chars);
                i++; // 跳过额外的字符
            } else {
                System.out.println((char) codePoint);
            }
        }
    }
}
