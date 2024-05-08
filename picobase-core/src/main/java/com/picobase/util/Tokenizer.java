package com.picobase.util;

import cn.hutool.core.io.IoUtil;
import com.picobase.persistence.resolver.ResultCouple;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tokenizer implements a rudimentary tokens parser of buffered
 * BufferedReader while respecting quotes and parenthesis boundaries.
 * <p>
 * Example
 * <p>
 * var tk = Tokenizer.newFromString("a, b, (c, d)")
 * <p>
 * var result = tk.scanAll() // ["a", "b", "(c, d)"]
 */
public class Tokenizer implements Closeable {
    private static final char EOF = (char) -1;
    private static final char[] whitespaceChars = new char[]{'\t', '\n', '\u000B', '\f', '\r', ' ', 0x85, 0xA0};
    public static final char[] DefaultSeparators = new char[]{','};

    private final BufferedReader r;
    private String trimCutset;
    private char[] separators;

    private boolean keepSeparator;
    private boolean keepEmptyTokens;
    private boolean ignoreParenthesis;


    private Tokenizer(BufferedReader r) {
        this.r = r;
        this.setSeparators(DefaultSeparators);
    }

    /**
     * Scan reads and returns the next available token from the Tokenizer's buffer (trimmed!).
     * <p>
     * Empty tokens are skipped if t.keepEmptyTokens is not set (which is the default).
     * <p>
     * Returns [EOF] error when there are no more tokens to scan.
     */
    public ResultCouple<String> scan() {
        var ch = this.read();
        if (ch == EOF) {
            return new ResultCouple<>("", new Error("EOF"));
        }
        this.unread();
        String token = this.readToken();

        if (!this.keepEmptyTokens && token.isEmpty()) {
            // skip empty tokens，跳过空字符继续scan
            return this.scan();
        }

        return new ResultCouple<>(token, null);
    }

    public List<String> scanAll() {
        var tokens = new ArrayList<String>();
        while (true) {
            var scanRes = this.scan();
            if (scanRes.getError() != null) {
                break;
            }
            tokens.add(scanRes.getResult());
        }
        return tokens;
    }

    private String readToken() {
        StringBuilder sb = new StringBuilder();
        int parenthesis = 0;
        char quoteCh = EOF;
        char prevCh = EOF;

        while (true) {
            var ch = this.read();
            if (ch == EOF) {
                break;
            }

            if (!this.isEscapeChar(prevCh)) {
                if (!this.ignoreParenthesis && ch == '(' && quoteCh == EOF) {
                    parenthesis++; // opening parenthesis
                } else if (!this.ignoreParenthesis && ch == ')' && parenthesis > 0 && quoteCh == EOF) {
                    parenthesis--; // closing parenthesis
                } else if (this.isQuoteChar(ch)) {
                    if (quoteCh == EOF) {
                        quoteCh = ch;
                    } else if (quoteCh == ch) {
                        quoteCh = EOF;
                    }
                }

            }

            if (isSeperatorChar(ch) && parenthesis == 0 && quoteCh == EOF) {
                if (this.keepSeparator) {
                    sb.append(ch);
                }
                break;
            }

            prevCh = ch;
            sb.append(ch);

        }

        if (parenthesis > 0 || quoteCh != EOF) {
            throw new IllegalArgumentException("unbalanced parenthesis or quoted expression: " + sb);
        }

        return Tokenizer.trimByCutset(sb.toString(), this.trimCutset);
    }

    private char read() {
        try {
            r.mark(1);
            return (char) r.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void unread() {
        try {
            r.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * setSeparators defines the provided separatos of the current Tokenizer.
     */
    public void setSeparators(char... separators) {
        this.separators = separators;
        this.rebuildTrimCutset();
    }

    /**
     * rebuildTrimCutset rebuilds the tokenizer trimCutset based on its separator chars.
     */
    private void rebuildTrimCutset() {
        StringBuilder cutset = new StringBuilder();

        for (char w : whitespaceChars) {
            if (isSeperatorChar(w)) {
                continue;
            }
            cutset.append(w);
        }

        this.trimCutset = cutset.toString();
    }


    public static Tokenizer newFromString(String str) {
        if (str.isEmpty()) {
            return new Tokenizer(new BufferedReader(new StringReader(str)));
        }
        return new Tokenizer(new BufferedReader(new StringReader(str), str.length()));
    }

    private boolean isSeperatorChar(char ch) {
        if (this.separators == null) {
            return false;
        }
        for (char r : this.separators) {
            if (ch == r) {
                return true;
            }
        }
        return false;
    }

    private boolean isWhitespaceChar(char ch) {
        for (char c : whitespaceChars) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }

    private boolean isQuoteChar(char ch) {
        return ch == '\'' || ch == '"' || ch == '`';
    }

    private boolean isEscapeChar(char ch) {
        return ch == '\\';
    }


    /**
     * 去除字符串中开头和结尾包含在cutset中的字符
     * 如："###Hello, World!!!###"; cutset = "#!o"; 输出"Hello, World"
     */

    private static String trimByCutset(String str, String cutset) {
        String pattern = "^[" + Pattern.quote(cutset) + "]+|[" + Pattern.quote(cutset) + "]+$";
        return str.replaceAll(pattern, "");
    }

    @Override
    public void close() {
        IoUtil.close(r);
    }

   /* public static void main(String[] args) {
        String str = "# # #Hello, World!!!## #";
        String cutset = "#! o ";
        String trimmedStr = trimByCutset(str, cutset);
        System.out.println(trimmedStr); // 输出 "Hello, World"
    }*/

    public Tokenizer setKeepSeparator(boolean keepSeparator) {
        this.keepSeparator = keepSeparator;
        return this;
    }

    public Tokenizer setKeepEmptyTokens(boolean keepEmptyTokens) {
        this.keepEmptyTokens = keepEmptyTokens;
        return this;
    }

    public Tokenizer setIgnoreParenthesis(boolean ignoreParenthesis) {
        this.ignoreParenthesis = ignoreParenthesis;
        return this;
    }
}
