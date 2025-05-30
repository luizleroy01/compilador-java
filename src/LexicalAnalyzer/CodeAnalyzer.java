package src.LexicalAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeAnalyzer {

    // Ordem dos padrões importa!
    private static final LinkedHashMap<String, String> TOKEN_PATTERNS = new LinkedHashMap<>();

    static {
        TOKEN_PATTERNS.put("FLOAT_CONST", "\\d+\\.\\d+");
        TOKEN_PATTERNS.put("INTEGER_CONST", "\\d+");
        TOKEN_PATTERNS.put("CHAR_CONST", "'[^']'");
        TOKEN_PATTERNS.put("LITERAL", "\"[^\"]*\"");
        TOKEN_PATTERNS.put("IDENTIFIER", "[a-zA-Z_][a-zA-Z0-9_]*");
        TOKEN_PATTERNS.put("REL_OP", "==|!=|<=|>=|<|>");
        TOKEN_PATTERNS.put("ADD_OP", "\\+|-|\\|\\|");
        TOKEN_PATTERNS.put("MUL_OP", "\\*|/|&&");
        TOKEN_PATTERNS.put("SYMBOL", "[;:,()=]");
    }

    public void analyze(String path) {
        try {
            String input = Files.readString(Paths.get(path));

            // Remove comentários
            input = input.replaceAll("\\{.*?}", "");
            input = input.replaceAll("%.*", "");

            // Criar expressão combinada com grupos posicionais
            StringBuilder regexBuilder = new StringBuilder();
            List<String> tokenTypes = new ArrayList<>();

            for (Map.Entry<String, String> entry : TOKEN_PATTERNS.entrySet()) {
                regexBuilder.append("|(").append(entry.getValue()).append(")");
                tokenTypes.add(entry.getKey());
            }

            String finalRegex = regexBuilder.substring(1); // remove '|'
            Pattern pattern = Pattern.compile(finalRegex);

            // Limpeza de caracteres inválidos
            StringBuilder cleanedInput = new StringBuilder();
            int currentIndex = 0;

            while (currentIndex < input.length()) {
                Matcher tempMatcher = pattern.matcher(input);
                tempMatcher.region(currentIndex, input.length());

                if (tempMatcher.lookingAt()) {
                    cleanedInput.append(tempMatcher.group()).append(" ");
                    currentIndex = tempMatcher.end();
                } else {
                    char invalidChar = input.charAt(currentIndex);
                    if (!Character.isWhitespace(invalidChar)) {
                        System.out.printf("Caractere inválido removido: '%s'%n", invalidChar);
                    }
                    currentIndex++;
                }
            }

            // Conteúdo limpo
            String sanitizedInput = cleanedInput.toString();
            System.out.println("\n--- Conteúdo limpo ---\n");
            System.out.println(sanitizedInput);

            // Análise léxica
            Matcher matcher = pattern.matcher(sanitizedInput);
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.group(i) != null) {
                        System.out.printf("Token: %-15s Lexema: %s%n", tokenTypes.get(i - 1), matcher.group(i));
                        break;
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}
