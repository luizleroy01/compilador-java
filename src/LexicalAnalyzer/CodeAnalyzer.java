package src.LexicalAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class CodeAnalyzer {

    private static final LinkedHashMap<String, String> TOKEN_PATTERNS = new LinkedHashMap<>();
    private final Map<String, String> lexemeToToken = new LinkedHashMap<>();

    static {
        // Palavras reservadas com tokens distintos
        TOKEN_PATTERNS.put("PROGRAM", "\\bprogram\\b");
        TOKEN_PATTERNS.put("BEGIN", "\\bbegin\\b");
        TOKEN_PATTERNS.put("END", "\\bend\\b");
        TOKEN_PATTERNS.put("INT", "\\bint\\b");
        TOKEN_PATTERNS.put("FLOAT", "\\bfloat\\b");
        TOKEN_PATTERNS.put("CHAR", "\\bchar\\b");
        TOKEN_PATTERNS.put("IF", "\\bif\\b");
        TOKEN_PATTERNS.put("THEN", "\\bthen\\b");
        TOKEN_PATTERNS.put("ELSE", "\\belse\\b");
        TOKEN_PATTERNS.put("REPEAT", "\\brepeat\\b");
        TOKEN_PATTERNS.put("UNTIL", "\\buntil\\b");
        TOKEN_PATTERNS.put("WHILE", "\\bwhile\\b");
        TOKEN_PATTERNS.put("DO", "\\bdo\\b");
        TOKEN_PATTERNS.put("IN", "\\bin\\b");
        TOKEN_PATTERNS.put("OUT", "\\bout\\b");

        // Outros padrões
        TOKEN_PATTERNS.put("LITERAL", "\"[^\"]*\"");
        TOKEN_PATTERNS.put("CHAR_CONST", "'[^']'");
        TOKEN_PATTERNS.put("ATTR_OP", ":=");
        TOKEN_PATTERNS.put("REL_OP", "==|!=|<=|>=|<|>");
        TOKEN_PATTERNS.put("ADD_OP", "\\+|\\-|\\|\\|");
        TOKEN_PATTERNS.put("MUL_OP", "\\*|/|&&");
        TOKEN_PATTERNS.put("SYMBOL", "[;:,]");
        TOKEN_PATTERNS.put("OPEN_PAR", "\\(");
        TOKEN_PATTERNS.put("CLOSE_PAR", "\\)");
        TOKEN_PATTERNS.put("ASSIGN_SYMBOL", "=");
        TOKEN_PATTERNS.put("FLOAT_CONST", "\\d+\\.\\d+");
        TOKEN_PATTERNS.put("INTEGER_CONST", "\\d+");
        TOKEN_PATTERNS.put("IDENTIFIER", "[a-zA-Z_$?!&%#@ç][a-zA-Z0-9_$?!&%#@ç]*");
    }

    public List<Token> analyze(String path) {
        List<Token> foundTokens = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> invalidTokens = new ArrayList<>();

            StringBuilder regexBuilder = new StringBuilder();
            List<String> tokenTypes = new ArrayList<>();
            for (Map.Entry<String, String> entry : TOKEN_PATTERNS.entrySet()) {
                regexBuilder.append("|(").append(entry.getValue()).append(")");
                tokenTypes.add(entry.getKey());
            }
            String finalRegex = regexBuilder.substring(1);
            Pattern pattern = Pattern.compile(finalRegex);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).replaceAll("\\{.*?}", "").replaceAll("%.*", "").trim();

                Matcher badFloat = Pattern.compile("\\b\\d+\\.(?!\\d)").matcher(line);
                while (badFloat.find()) {
                    String malformado = badFloat.group();
                    System.out.printf("Linha %d: Token inesperado: número malformado '%s'%n", i + 1, malformado);
                    invalidTokens.add("Linha " + (i + 1) + ": número malformado '" + malformado + "'");
                    line = line.replace(malformado, " ");
                }

                Matcher matcher = pattern.matcher(line);
                int lastEnd = 0;
                while (matcher.find()) {
                    if (matcher.start() > lastEnd) {
                        String invalid = line.substring(lastEnd, matcher.start()).trim();
                        if (!invalid.isEmpty()) {
                            System.out.printf("Linha %d: Token inválido: '%s'%n", i + 1, invalid);
                            invalidTokens.add("Linha " + (i + 1) + ": token inválido '" + invalid + "'");
                        }
                    }

                    lastEnd = matcher.end();
                    for (int g = 1; g <= tokenTypes.size(); g++) {
                        if (matcher.group(g) != null) {
                            String lexeme = matcher.group(g);
                            String tokenType = tokenTypes.get(g - 1);

                            if ("IDENTIFIER".equals(tokenType) &&
                                    lexeme.matches("program|begin|end|int|float|char|if|then|else|repeat|until|while|do|in|out")) {
                                continue; // já foram tratados como tokens reservados específicos
                            }

                            System.out.printf("Linha %d: Token: %-15s Lexema: %s%n", i + 1, tokenType, lexeme);
                            foundTokens.add(new Token(tokenType, lexeme, i + 1));
                            lexemeToToken.put(lexeme, tokenType);
                            break;
                        }
                    }
                }

                if (lastEnd < line.length()) {
                    String invalid = line.substring(lastEnd).trim();
                    if (!invalid.isEmpty()) {
                        System.out.printf("Linha %d: Token inválido: '%s'%n", i + 1, invalid);
                        invalidTokens.add("Linha " + (i + 1) + ": token inválido '" + invalid + "'");
                    }
                }
            }

            if (!invalidTokens.isEmpty()) {
                System.out.println("\nErros encontrados durante a análise:");
                invalidTokens.forEach(System.out::println);
            } else {
                System.out.println("\nAnálise concluída sem erros.");
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }

        return foundTokens;
    }
}
