package com.legv8.simulator.parser;

import com.legv8.simulator.instruction.Mnemonic;
import com.legv8.simulator.lexer.Token;
import com.legv8.simulator.lexer.TokenType;

import java.util.Objects;

/**
 * The <code>ParserState</code> enumeration defines all states and transitions in the parser FSM
 * <p>
 * See the LEGv8_Grammar.ppt slides for a graphical description and explanation of the LEGv8 parser FSM
 *
 * @see Parser
 * @author Jonathan Wright, 2016
 * @author Rodrigo Bautista Hernández, 2025
 */
public enum ParserState {
    INIT(false, null) {
        @Override
        public ParserState transition(Token t) throws UnsupportedInstructionException {
            return switch (t.getType()) {
                case MNEMONIC_R -> R1;
                case MNEMONIC_RR -> RR1;
                case MNEMONIC_RRR -> RRR1;
                case MNEMONIC_RI -> RI1;
                case MNEMONIC_RRI -> RRI1;
                case MNEMONIC_RM -> RM1;
                case MNEMONIC_RRM -> RRM1;
                case MNEMONIC_RISI -> RISI1;
                case MNEMONIC_L -> L1;
                case MNEMONIC_RL -> RL1;
                case LABEL -> G1;
                case MNEMONIC_SYS -> SYS1;
                default -> throw new UnsupportedInstructionException(INIT);
            };
        }
    },
    G1(true, null) { @Override
    public ParserState transition(Token t) throws UnsupportedInstructionException {
        return switch (t.getType()) {
            case MNEMONIC_R -> R1;
            case MNEMONIC_RR -> RR1;
            case MNEMONIC_RRR -> RRR1;
            case MNEMONIC_RI -> RI1;
            case MNEMONIC_RRI -> RRI1;
            case MNEMONIC_RM -> RM1;
            case MNEMONIC_RRM -> RRM1;
            case MNEMONIC_RISI -> RISI1;
            case MNEMONIC_L -> L1;
            case MNEMONIC_RL -> RL1;
            case MNEMONIC_SYS -> SYS1;
            default -> throw new UnsupportedInstructionException(G1);
        };
    }
    },
    R1(false, new TokenType[]{TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return R2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    R2(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    RR1(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RR2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RR2(false, new TokenType[]{TokenType.COMMA, TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RR3;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RR3(false, new TokenType[]{TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RR4;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RR4(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    RRR1(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.REGISTER, TokenType.COMMA, TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RRR2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRR2(false, new TokenType[]{TokenType.COMMA, TokenType.REGISTER,
            TokenType.COMMA, TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RRR3;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRR3(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RRR4;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRR4(false, new TokenType[]{TokenType.COMMA, TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RRR5;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRR5(false, new TokenType[]{TokenType.REGISTER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RRR6;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRR6(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    RI1(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RI2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RI2(false, new TokenType[]{TokenType.COMMA, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RI3;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RI3(false, new TokenType[]{TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.IMMEDIATE) {
            return RI4;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RI4(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    RRI1(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.REGISTER, TokenType.COMMA, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RRI2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRI2(false, new TokenType[]{TokenType.COMMA, TokenType.REGISTER,
            TokenType.COMMA, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RRI3;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRI3(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RRI4;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRI4(false, new TokenType[]{TokenType.COMMA, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RRI5;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRI5(false, new TokenType[]{TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.IMMEDIATE) {
            return RRI6;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRI6(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    RM1(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.LBRACKET, TokenType.REGISTER, TokenType.COMMA,
            TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RM2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RM2(false, new TokenType[]{TokenType.COMMA, TokenType.LBRACKET, TokenType.REGISTER,
            TokenType.COMMA, TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RM3;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RM3(false, new TokenType[]{TokenType.LBRACKET, TokenType.REGISTER,
            TokenType.COMMA, TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.LBRACKET) {
            return RM4;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RM4(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RM5;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RM5(false, new TokenType[]{TokenType.COMMA, TokenType.IMMEDIATE,
            TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        return switch (t.getType()) {
            case RBRACKET -> RM6;
            case COMMA -> RM7;
            default -> throw new InvalidTokenException(expected[0]);
        };
    }
    },
    RM6(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    RM7(false, new TokenType[]{TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.IMMEDIATE) {
            return RM8;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RM8(false, new TokenType[]{TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.RBRACKET) {
            return RM6;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRM1(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA, TokenType.REGISTER,
            TokenType.COMMA, TokenType.LBRACKET, TokenType.REGISTER, TokenType.COMMA,
            TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RRM2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRM2(false, new TokenType[]{TokenType.COMMA, TokenType.REGISTER,
            TokenType.COMMA, TokenType.LBRACKET, TokenType.REGISTER,
            TokenType.COMMA, TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RRM3;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRM3(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.LBRACKET, TokenType.REGISTER, TokenType.COMMA,
            TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RRM4;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRM4(false, new TokenType[]{TokenType.COMMA, TokenType.LBRACKET, TokenType.REGISTER,
            TokenType.COMMA, TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RRM5;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRM5(false, new TokenType[]{TokenType.LBRACKET, TokenType.REGISTER,
            TokenType.COMMA, TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.LBRACKET) {
            return RRM6;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRM6(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RRM7;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRM7(false, new TokenType[]{TokenType.COMMA, TokenType.IMMEDIATE,
            TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        return switch (t.getType()) {
            case RBRACKET -> RRM8;
            case COMMA -> RRM9;
            default -> throw new InvalidTokenException(expected[0]);
        };
    }
    },
    RRM8(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    RRM9(false, new TokenType[]{TokenType.IMMEDIATE, TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.IMMEDIATE) {
            return RRM10;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RRM10(false, new TokenType[]{TokenType.RBRACKET}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.RBRACKET) {
            return RRM8;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RISI1(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA, TokenType.IMMEDIATE,
            TokenType.COMMA, TokenType.MNEMONIC_RRI, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RISI2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RISI2(false, new TokenType[]{TokenType.COMMA, TokenType.IMMEDIATE,
            TokenType.COMMA, TokenType.MNEMONIC_RRI, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RISI3;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RISI3(false, new TokenType[]{TokenType.IMMEDIATE, TokenType.COMMA,
            TokenType.MNEMONIC_RRI, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.IMMEDIATE) {
            return RISI4;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RISI4(true, new TokenType[]{TokenType.COMMA, TokenType.MNEMONIC_RRI, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RISI5;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RISI5(false, new TokenType[]{TokenType.MNEMONIC_RRI, TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.MNEMONIC_RRI) {
            if (t.getData().trim().equalsIgnoreCase("lsl") && !t.getData().trim().equals(t.getData())) {
                return RISI6;
            } else {
                throw new InvalidTokenException(expected[0], Mnemonic.LSL);
            }
        }
        throw new InvalidTokenException(expected[0], Mnemonic.LSL);
    }
    },
    RISI6(false, new TokenType[]{TokenType.IMMEDIATE}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.IMMEDIATE) {
            return RISI7;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RISI7(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    L1(false, new TokenType[]{TokenType.IDENTIFIER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.IDENTIFIER) {
            return L2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    L2(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    L3(true, null) { @Override
    public ParserState transition(Token t) { return L3; }
    },
    RL1(false, new TokenType[]{TokenType.REGISTER, TokenType.COMMA,
            TokenType.IDENTIFIER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.REGISTER) {
            return RL2;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RL2(false, new TokenType[]{TokenType.COMMA, TokenType.IDENTIFIER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.COMMA) {
            return RL3;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RL3(false, new TokenType[]{TokenType.IDENTIFIER}) { @Override
    public ParserState transition(Token t) throws InvalidTokenException {
        if (Objects.requireNonNull(t.getType()) == TokenType.IDENTIFIER) {
            return RL4;
        }
        throw new InvalidTokenException(expected[0]);
    }
    },
    RL4(true, null) { @Override
    public ParserState transition(Token t) throws UnexpectedTokenException {
        throw new UnexpectedTokenException();
    }
    },
    SYS1(false, new TokenType[]{TokenType.IMMEDIATE}) {
        @Override
        public ParserState transition(Token t) throws InvalidTokenException {
            if (Objects.requireNonNull(t.getType()) == TokenType.IMMEDIATE) {
                return SYS2;
            }
            throw new InvalidTokenException(expected[0]);
        }
    },
    SYS2(true, null) {
        @Override
        public ParserState transition(Token t) throws UnexpectedTokenException {
            throw new UnexpectedTokenException();
        }
    };;

    private ParserState(boolean accepting, TokenType[] expected) {
        this.expected = expected;
        this.accepting = accepting;
    }

    /**
     * The transition function for this parser state
     *
     * @param next	the next token read by the parser
     * @return		the next parser state - the result of the transition function
     *
     * @throws UnsupportedInstructionException
     * @throws InvalidTokenException
     * @throws UnexpectedTokenException
     */
    public abstract ParserState transition(Token next) throws UnsupportedInstructionException,
            InvalidTokenException, UnexpectedTokenException;

    /**
     * Whether the this state of the parser FSM is accepting
     */
    public final boolean accepting;

    /**
     * The token sequence required for the parser to reach an accepting state from this state
     */
    public final TokenType[] expected;

}
