package com.picobase.model;

import com.picobase.exception.BadRequestException;
import com.picobase.persistence.resolver.ResultCouple;
import com.picobase.util.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Identifier {
    private String original;
    private String alias;

    public Identifier(String original, String alias) {
        this.original = original;
        this.alias = alias;
    }

    public Identifier() {
    }

    public final static Pattern joinReplaceRegex = Pattern.compile("(?im)\\s+(inner join|outer join|left join|right join|join)\\s+?");
    public final static Pattern discardReplaceRegex = Pattern.compile("(?im)\\s+(where|group by|having|order|limit|with)\\s+?");
    public final static Pattern commentsReplaceRegex = Pattern.compile("(?m)(\\/\\*[\\s\\S]+\\*\\/)|(--.+$)");

    public final static Pattern castRegex = Pattern.compile("(?i)^cast\\s*\\(.*\\s+as\\s+(\\w+)\\s*\\)$");


    public static IdentifiersParser parse(String selectQuery) {
        String str = selectQuery.trim().replaceAll(";", "");
        str = joinReplaceRegex.matcher(str).replaceAll(" _join_ ");
        str = discardReplaceRegex.matcher(str).replaceAll(" _discard_ ");
        str = commentsReplaceRegex.matcher(str).replaceAll("");

        Tokenizer tk = Tokenizer.newFromString(str);
        tk.setSeparators(',', ' ', '\n', '\t');
        tk.setKeepSeparator(true);

        boolean skip = false;
        String partType = "";
        StringBuilder activeBuilder = null;
        StringBuilder selectParts = new StringBuilder();
        StringBuilder fromParts = new StringBuilder();
        StringBuilder joinParts = new StringBuilder();

        while (true) {
            ResultCouple<String> token = tk.scan();
            if (null != token.getError()) {
                if (!Objects.equals(token.getError().getMessage(), "EOF")) {
                    throw new BadRequestException("");
                }
                break;
            }

            String trimmed = token.getResult().trim().toLowerCase();

            switch (trimmed) {
                case "select" -> {
                    skip = false;
                    partType = "select";
                    activeBuilder = selectParts;
                }
                case "distinct" -> {
                    // ignore as it is not important for the identifiers parsing
                }
                case "from" -> {
                    skip = false;
                    partType = "from";
                    activeBuilder = fromParts;
                }
                case "_join_" -> {
                    skip = false;

                    if (partType.equals("join")) {
                        joinParts.append(",");
                    }

                    partType = "join";
                    activeBuilder = joinParts;
                }
                case "_discard_" -> skip = true;
                default -> {
                    boolean isJoin = partType.equals("join");

                    if (isJoin && trimmed.equals("on")) {
                        skip = true;
                    }

                    if (!skip && activeBuilder != null) {
                        activeBuilder.append(" ").append(token.getResult());
                    }
                }
            }
        }

        tk.close();
        List<Identifier> selects = extractIdentifiers(selectParts.toString());
        List<Identifier> froms = extractIdentifiers(fromParts.toString());
        List<Identifier> joins = extractIdentifiers(joinParts.toString());

        froms.addAll(joins);
        return new IdentifiersParser(selects, froms);
    }


    public static List<Identifier> extractIdentifiers(String rawExpression) {
        try (Tokenizer rawTk = Tokenizer.newFromString(rawExpression)) {
            rawTk.setSeparators(',');

            List<String> rawIdentifiers = rawTk.scanAll();

            List<Identifier> result = new ArrayList<>(rawIdentifiers.size());

            for (String rawIdentifier : rawIdentifiers) {
                try (Tokenizer tk = Tokenizer.newFromString(rawIdentifier)) {
                    tk.setSeparators(' ', '\n', '\t');

                    List<String> parts = tk.scanAll();
                    Identifier resolved = identifierFromParts(parts.toArray(new String[0]));

                    result.add(resolved);
                }
            }
            return result;
        }
    }


    public static Identifier identifierFromParts(String[] parts) throws IllegalArgumentException {
        Identifier result = switch (parts.length) {
            case 3 -> {
                if (!parts[1].equalsIgnoreCase("as")) {
                    throw new IllegalArgumentException("Invalid identifier part - expected \"as\", got " + parts[1]);
                }
                yield new Identifier(parts[0], parts[2]);
            }
            case 2 -> new Identifier(parts[0], parts[1]);
            case 1 -> {
                String[] subParts = parts[0].split("\\.", -1);
                yield new Identifier(parts[0], subParts[subParts.length - 1]);
            }
            default -> throw new IllegalArgumentException("Invalid identifier parts " + Arrays.toString(parts));
        };

        result.setOriginal(trimRawIdentifier(result.getOriginal()));

        // Trim the single quote even though it is not a valid column quote character
        // because SQLite allows it if the context expects an identifier and not string literal
        // (https://www.sqlite.org/lang_keywords.html)
        result.setAlias(trimRawIdentifier(result.getAlias(), "'"));

        return result;
    }


    public static String trimRawIdentifier(String rawIdentifier, String... extraTrimChars) {
        String trimChars = "`\"\\[\\];";
        if (extraTrimChars.length > 0) {
            trimChars += String.join("", extraTrimChars);
        }

        String[] parts = rawIdentifier.split("\\.", -1);

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replaceAll("[" + trimChars + "]", "");
        }

        return String.join(".", parts);
    }

    public String getOriginal() {
        return original;
    }

    public Identifier setOriginal(String original) {
        this.original = original;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Identifier setAlias(String alias) {
        this.alias = alias;
        return this;
    }
}

