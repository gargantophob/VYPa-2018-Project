package parsed;

import parser.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.List;
import java.util.ArrayList;

public class Expression {
    public static enum Option {
        LITERAL, PATH, NEW, CAST, CALL,
        NEG, MUL, DIV, ADD, SUB,
        LESS, MORE, LEQ, MEQ, EQ, NEQ, AND, OR
    }

    public Option option;
    
    public Literal literal;
    public Path path;
    public String classNew;
    public Type typeCast;
    public Call call;

    public Expression op1;
    public Expression op2;    
    
    public Expression(GrammarParser.ExContext ctx) {

        option = null;
        literal = null;
        path = null;
        classNew = null;
        typeCast = null;
        call = null;
        op1 = null;
        op2 = null;

        while(true) {
            if(ctx.getChild(0).getText().equals("(")) {
                // parentheses
                ctx = ctx.ex().get(0);
                continue;
            }
            break;
        }

        if(ctx.arguments() != null) {
            // call
            option = Option.CALL;
            call = new Call(ctx);
            return;
        }

        if(ctx.value() != null) {
            // direct value
            GrammarParser.ValueContext valueContext = ctx.value();
            if(valueContext.literal() != null) {
                option = Option.LITERAL;
                literal = new Literal(valueContext.literal());
            } else {
                option = Option.PATH;
                path = new Path(valueContext.path());
            }
            return;
        }

        if(ctx.type() != null) {
            // cast
            option = Option.CAST;
            typeCast = new Type(ctx.type());
            op1 = new Expression(ctx.ex().get(0));
            return;
        }

        if(ctx.name() != null) {
            // new
            option = Option.NEW;
            classNew = ctx.name().getText();
            return;
        }

        if(ctx.ex().size() == 1) {
            // negation
            option = Option.NEG;
            op1 = new Expression(ctx.ex().get(0));
            return;
        }

        // binary operation            
        op1 = new Expression(ctx.ex().get(0));
        op2 = new Expression(ctx.ex().get(1));
        switch(ctx.getChild(1).getText()) {
            case "*": option = Option.MUL; break;
            case "/": option = Option.DIV; break;
            case "+": option = Option.ADD; break;
            case "-": option = Option.SUB; break;
            case "<": option = Option.LESS; break;
            case ">": option = Option.MORE; break;
            case "<=": option = Option.LEQ; break;
            case ">=": option = Option.MEQ; break;
            case "==": option = Option.EQ; break;
            case "!=": option = Option.NEQ; break;
            case "&&": option = Option.AND; break;
            case "||": option = Option.OR; break;
            default: main.Recover.warn("!"); break;
        }
    }

    /*@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        switch(option) {
            case LITERAL: sb.append(literal); break;
            case PATH: sb.append(path); break;
            case NEW: sb.append("new " + classNew); break;
            case CAST: sb.append("(" + classNew + ") " + op1); break;
            case CALL: sb.append(call); break;
            case NEG: sb.append(option + " " + op1); break;
            default: sb.append(op1 + " " + option + " " + op2); break;
        }
        sb.append(")");
        return sb.toString();
    }*/
}
