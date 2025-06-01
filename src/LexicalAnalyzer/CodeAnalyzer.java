package src.LexicalAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class CodeAnalyzer {

    private static final LinkedHashMap<String, String> TOKEN_PATTERNS = new LinkedHashMap<>();
    private final Map<String, String> lexemeToToken = new LinkedHashMap<>();
    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "program", "begin", "end", "int", "float", "char", "if", "then", "else",
            "repeat", "until", "while", "do", "in", "out"
    ));

    static {
        TOKEN_PATTERNS.put("LITERAL", "\"[^\"]*\"");
        TOKEN_PATTERNS.put("CHAR_CONST", "'[^']'");
        TOKEN_PATTERNS.put("ATTR_OP", ":=");
        TOKEN_PATTERNS.put("REL_OP", "==|!=|<=|>=|<|>");
        TOKEN_PATTERNS.put("ADD_OP", "\\+|\\-|\\|\\|");
        TOKEN_PATTERNS.put("MUL_OP", "\\*|/|&&");
        TOKEN_PATTERNS.put("SYMBOL", "[;:,()=]");
        TOKEN_PATTERNS.put("FLOAT_CONST", "\\d+\\.\\d+");
        TOKEN_PATTERNS.put("INTEGER_CONST", "\\d+");
        TOKEN_PATTERNS.put("IDENTIFIER", "[a-zA-Z_][a-zA-Z0-9_]*");
    }

    public void analyze(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> invalidTokens = new ArrayList<>();

            // Regex principal para tokens
            StringBuilder regexBuilder = new StringBuilder();
            List<String> tokenTypes = new ArrayList<>();
            for (Map.Entry<String, String> entry : TOKEN_PATTERNS.entrySet()) {
                regexBuilder.append("|(").append(entry.getValue()).append(")");
                tokenTypes.add(entry.getKey());
            }
            String finalRegex = regexBuilder.substring(1);
            Pattern pattern = Pattern.compile(finalRegex);

            // Validação da estrutura do programa
            boolean programFound = false;
            boolean beginFound = false;
            boolean endFound = false;
            boolean afterBegin = false;

            Set<String> validTypes = Set.of("int", "float", "char");

            int lineNum = 0;

            while (lineNum < lines.size()) {
                String line = lines.get(lineNum).trim();

                // Remove comentários entre chaves e depois '%' para comentários de linha
                line = line.replaceAll("\\{.*?}", "").replaceAll("%.*", "").trim();

                if (line.isEmpty()) {
                    lineNum++;
                    continue;
                }

                // 1. Verifica se o programa inicia com 'program'
                if (!programFound) {
                    if (line.startsWith("program")) {
                        programFound = true;
                        lineNum++;
                        continue;
                    } else {
                        System.out.printf("Linha %d: Erro sintático: programa deve iniciar com 'program'%n", lineNum + 1);
                        invalidTokens.add("Linha " + (lineNum + 1) + ": programa deve iniciar com 'program'");
                        break;
                    }
                }

                // 2. Enquanto não encontrar 'begin', validar declarações (decl-list)
                if (programFound && !beginFound) {
                    if (line.startsWith("begin")) {
                        beginFound = true;
                        afterBegin = true;
                        lineNum++;
                        continue;
                    }

                    // Declaração: tipo ":" ident-list ";"
                    Pattern declPattern = Pattern.compile(
                            "(int|float|char)\\s*:\\s*([a-zA-Z_][a-zA-Z0-9_]*(\\s*,\\s*[a-zA-Z_][a-zA-Z0-9_]*)*)\\s*;");

                    Matcher declMatcher = declPattern.matcher(line);
                    if (declMatcher.matches()) {
                        String type = declMatcher.group(1);
                        String idList = declMatcher.group(2);

                        if (!validTypes.contains(type)) {
                            System.out.printf("Linha %d: Tipo inválido na declaração: '%s'%n", lineNum + 1, type);
                            invalidTokens.add("Linha " + (lineNum + 1) + ": tipo inválido '" + type + "'");
                        }

                        String[] ids = idList.split(",");
                        for (String id : ids) {
                            id = id.trim();
                            if (!id.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                                System.out.printf("Linha %d: Identificador inválido: '%s'%n", lineNum + 1, id);
                                invalidTokens.add("Linha " + (lineNum + 1) + ": identificador inválido '" + id + "'");
                            }
                        }
                    } else {
                        System.out.printf("Linha %d: Declaração inválida ou sintaxe incorreta: '%s'%n", lineNum + 1, line);
                        invalidTokens.add("Linha " + (lineNum + 1) + ": declaração inválida ou sintaxe incorreta");
                    }

                    lineNum++;
                    continue;
                }

                // 3. Após 'begin' deve existir statements e terminar em 'end'
                if (afterBegin && !endFound) {
                    if (line.equals("end")) {
                        endFound = true;
                        lineNum++;
                        continue;
                    }

                    // Aqui pode-se validar statements (não implementado neste escopo)
                    lineNum++;
                    continue;
                }

                lineNum++;
            }

            if (!programFound) {
                System.out.println("Erro: programa não encontrado.");
            } else if (!beginFound) {
                System.out.println("Erro: 'begin' não encontrado.");
            } else if (!endFound) {
                System.out.println("Erro: 'end' não encontrado.");
            }

            // --- Agora executa a análise léxica linha a linha para imprimir tokens e erros léxicos ---

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).replaceAll("\\{.*?}", "").replaceAll("%.*", "");

                // 1. Verifica se existe identificador inválido antes de '('
                Matcher parenMatcher = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(").matcher(line);
                while (parenMatcher.find()) {
                    String beforeParen = parenMatcher.group(1);
                    if (!RESERVED_WORDS.contains(beforeParen)) {
                        System.out.printf("Linha %d: Lexema inválido antes de '(': '%s'%n", i + 1, beforeParen);
                        invalidTokens.add("Linha " + (i + 1) + ": lexema inválido antes de '(' → '" + beforeParen + "'");
                    }
                }

                // 2. Verifica se há parênteses não fechados após in( ou out(
                Matcher callMatcher = Pattern.compile("\\b(in|out)\\s*\\(").matcher(line);
                while (callMatcher.find()) {
                    int startIdx = callMatcher.end();
                    if (!line.substring(startIdx).contains(")")) {
                        System.out.printf("Linha %d: Erro léxico: chamada de '%s' sem parêntese fechando%n", i + 1, callMatcher.group(1));
                        invalidTokens.add("Linha " + (i + 1) + ": chamada de '" + callMatcher.group(1) + "' sem parêntese fechando");
                    }
                }

                // 3. Verifica se aspas estão desbalanceadas
                long quoteCount = line.chars().filter(ch -> ch == '"').count();
                if (quoteCount % 2 != 0) {
                    System.out.printf("Linha %d: Token inesperado: literal sem aspas fechando%n", i + 1);
                    invalidTokens.add("Linha " + (i + 1) + ": literal sem aspas fechando");
                }

                // 4. Verifica número malformado: "10."
                Matcher badFloat = Pattern.compile("\\d+\\.(?!\\d)").matcher(line);
                while (badFloat.find()) {
                    System.out.printf("Linha %d: Token inesperado: '%s'%n", i + 1, badFloat.group());
                    invalidTokens.add("Linha " + (i + 1) + ": número malformado '" + badFloat.group() + "'");
                }

                // 5. Tokenização geral
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

                            if ("IDENTIFIER".equals(tokenType) && RESERVED_WORDS.contains(lexeme)) {
                                tokenType = "RESERVED_WORD";
                            }

                            System.out.printf("Linha %d: Token: %-15s Lexema: %s%n", i + 1, tokenType, lexeme);
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

            // Imprime todos os erros coletados
            if (!invalidTokens.isEmpty()) {
                System.out.println("\nErros encontrados durante a análise:");
                invalidTokens.forEach(System.out::println);
            } else {
                System.out.println("\nAnálise concluída sem erros.");
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}
