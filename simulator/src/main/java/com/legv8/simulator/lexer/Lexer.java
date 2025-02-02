package com.legv8.simulator.lexer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <code>Lexer</code> is used to process individual lines of LEGv8 source code from the
 * text editor. A list of lexical tokens is returned which can be parsed by the
 * <code>Parser</code> class.
 *
 * @see	Parser
 * @author Jonathan Wright, 2016
 * @author Rodrigo Bautista Hernández, 2025
 *
 */
public class Lexer {

    /**
     * @param input	a line of LEGv8 source code
     * @return 		the list of lexical tokens found in the <code>input</code>. Whitespace tokens are omitted.
     */
    public static ArrayList<Token> lex(String input) {

        ArrayList<Token> tokens = new ArrayList<Token>();

        // build single regular expression from the groups defined in TokenType class
        StringBuffer tokenPatternsBuffer = new StringBuffer();
        for (TokenType type : TokenType.values()) {
            tokenPatternsBuffer.append("|(" + type.pattern + ")");
        }

        // Compile the regex pattern (Java does not have a "g" flag, we use find() instead)
        Pattern tokenPatterns = Pattern.compile(tokenPatternsBuffer.substring(1));
        Matcher matcher = tokenPatterns.matcher(input);

        // Find all matches
        while (matcher.find()) {
            for (TokenType type : TokenType.values()) {
                if (type == TokenType.WHITESPACE) {
                    continue;
                }
                if (matcher.group(type.groupNumber) != null) {
                    tokens.add(new Token(type, matcher.group(type.groupNumber)));
                    break;
                }
            }
        }

        return tokens;
    }
}
