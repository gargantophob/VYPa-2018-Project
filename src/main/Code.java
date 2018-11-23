package main;

import java.util.List;
import java.util.ArrayList;

public class Code {
    private static StringBuilder sb = new StringBuilder();

    private static void print(String str) {
        sb.append(str);
    }

    private static void println(String str) {
        sb.append(str + "\n");
    }

    private static void newline() {
        sb.append("\n");
    }

    private static void comment(String str) {
    	println("# " + str);
    }

    private static void separator() {
    	println("# ----------------------");
    }

    public static String code() {
    	// Header
        comment("Generated by: xandri03");
        newline();

        comment("R0 - framepointer");
        comment("R1 - expression result / function return value");
        println("ALIAS FP $0");
        println("SET $0 0");
        println("SET $1 0");
        // println("SET $2 0");
        newline();

        comment("call main, exit afterwards");
        println("CALL [$SP] ::main");
        println("JUMP @exit");
        newline();

        comment("***** GLOBAL FUNCTIONS *****");
        newline();
        SymbolTable.functions().forEach(f -> code(f));
        newline();

        comment("***** CLASSES *****");
        newline();
        println("# TODO");
        newline();
        
        println("LABEL @exit");
        newline();

        // Success
        return sb.toString();
    }

    private static String labelTemplate;
    private static int labelOrder;

    public static void code(Function f) {
    	// Reset static fields
    	labelTemplate = "";
    	if(f.context != null) {
    		labelTemplate = f.context.name;
    	}    
		labelTemplate += "::"+ f.name;
    	labelOrder = 0;

    	// Skip ::print, ::readInt, ::readString
    	if(f.body == null) {
    		if(
    			f.name.equals("print") ||
    			f.name.equals("readInt") ||
    			f.name.equals("readString")
    		) {
    			return;
    		}
    	}

        separator();
        print("LABEL " + labelTemplate);
        newline();

        // Store frame pointer
        println("SET $FP $SP");

        // Allocate local variables, they will be initialized at their declaration site
        println("ADDI $SP $SP " + f.order);
        newline();

        // Generate code for statements
        if(f.body == null) {
        	// Built-in ::length or ::subStr
        	if(f.name.equals("length")) {
        		// ::length
        		println("GETSIZE $1 [$SP-1]");
        	} else {
        		// ::subStr
        		println("WRITES \"LOL\"");
        	}
        } else {
        	f.body.forEach(s -> code(s));
        }
        newline();

        comment("Hard return");
        if(f.type == Type.VOID) {
        } else if(f.type == Type.INT) {
            println("SET $1 0");
        } else if(f.type == Type.STRING) {
            println("SET $1 \"\"");
        } else {
            println("SET $1 0");
        }
        returnVoid();
        newline();
    }

    public static void code(Statement s) {
    	newline();

    	Variable v;
    	switch(s.option) {
    		case DECLARATION:
    			// Set default value
    			v = s.declaration;
    			if(v.type == Type.STRING) {
    				println(String.format("SET [$FP%+d] \"\"", v.order));
    			} else {
    				println(String.format("SET [$FP%+d] 0", v.order));
    			}
    			break;
    		case ASSIGNMENT:
    			code(s.ex);
    			if(s.path.path == null) {
    				// simple case
    				pop(s.path.handle);
    			} else {
    				Recover.notImplemented();
    			}
    			break;
    		case CONDITIONAL:
    			code(s.ex);
    			pop("$1");
    			println("JUMPZ " + localLabelStr() + "else" + " $1");
    			code(s.blockTrue);
    			println("JUMP " + localLabelStr() + "end_if");
    			localLabel("else");
    			code(s.blockFalse);
    			localLabel("end_if");
    			labelOrder++;
    			break;
    		case ITERATION:
    			localLabel("while");
    			code(s.ex);
    			pop("$1");
    			println("JUMPZ " + localLabelStr() + "end_while" + " $1");
    			code(s.blockTrue);
    			println("JUMP " + localLabelStr() + "while");
    			localLabel("end_while");
    			labelOrder++;
    			break;
    		case CALL:
    			code(s.call);
    			break;
    		case RETURN:
    			if(s.ex != null) {
    				code(s.ex);
    				pop("$1");
    			}
    			returnVoid();
    			break;
    	}
    }

    public static void code(Expression ex) {
    	if(ex.option == Expression.Option.LITERAL) {
    		push(ex.literal.text);
    		return;
    	}

    	if(ex.option == Expression.Option.PATH) {
    		if(ex.path.path == null) {
				// simple case
				push(ex.path.handle);
			} else {
				Recover.notImplemented();
			}
			return;
    	}

    	if(ex.option == Expression.Option.NEW) {
			Recover.notImplemented();
			return;
    	}

    	if(ex.option == Expression.Option.CALL) {
    		code(ex.call);
			push("$1");
			return;
    	}
    	
    	// Unary or binary operations
		code(ex.op1);

    	if(ex.option == Expression.Option.INT2STRING) {
			println("INT2STRING $1 [$SP]");
			println("SET [$SP] $1");
			return;
    	}

    	if(ex.option == Expression.Option.CAST) {
			Recover.notImplemented();
			return;
    	}

    	if(ex.option == Expression.Option.NEG) {
    		println("NOT $1 [$SP]");
			println("SET [$SP] $1");
    	}
    	
    	// Binary operations

		code(ex.op2);
		String opcode = null;
		switch(ex.option) {
    		case MULI: opcode = "MULI"; break;
    		case DIVI: opcode = "DIVI"; break;
    		case ADDI: opcode = "ADDI"; break;
			case ADDS:
    			println("SET $1 [$SP-1] [$SP]");
    			break;
    		case SUBI: opcode = "SUBI"; break;
    		case LTI: opcode = "LTI"; break;
			case GTI: opcode = "GTI"; break;
    		case EQI: opcode = "EQI"; break;
			case NEQI:
    			println("EQI $1 [$SP-1] [$SP]");
    			println("NOT $1 $1");
    			break;
    		case LTS: opcode = "LTS"; break;
			case GTS: opcode = "GTS"; break;
    		case EQS: opcode = "EQS"; break;
			case NEQS:
    			println("EQS $1 [$SP-1] [$SP]");
    			println("NOT $1 $1");
    			break;
    		case AND: opcode = "AND"; break;
			case OR: opcode = "OR"; break;
    	}

    	if(opcode != null) {
    		println(opcode + " $1 [$SP-1] [$SP]");
    	}
		println("SUBI $SP $SP 1");
    	println("SET [$SP] $1");
    }

    public static void code(Block block) {
    	block.statements.forEach(s -> code(s));
    }

    public static void code(Call call) {
    	Function f = call.function;
    	if(f.body == null) {
    		// Built-in function call
    		if(f.name.equals("print")) {
    			call.arguments.forEach(ex -> {
    				code(ex);
    				pop("$1");
    				if(ex.type == Type.INT) {
	    				print("WRITEI");
    				} else {
    					print("WRITES");
    				}
    				println(" $1");
    			});
    			return;
    		} else if(f.name.equals("readInt")) {
    			println("READI $1");
    			return;
    		} else if(f.name.equals("readString")) {
    			println("READS $1");
    			return;
    		}
    	}

    	// store frame pointer
		push("$FP");

		// push arguments from right to left
		for(int i = call.arguments.size()-1; i >= 0; i--) {
			code(call.arguments.get(i));
		}

		// construct label and call, result will be in R1
		String callLabel = "";
		if(f.context != null) {
			callLabel = f.context.name;
		}
		callLabel += "::" + f.name;
		println("ADDI $SP $SP 1");
		println("CALL [$SP] " + callLabel);

		// pop return address and parameters
		println("SUBI $SP $SP " + (call.arguments.size()+1));

		// restore frame pointer
		pop("$FP");
    }

    public static String localLabelStr() {
    	return labelTemplate + ":" + labelOrder + ":";
    }

    public static void localLabel(String str) {
    	println("LABEL " + localLabelStr() + str);
    }

    public static void push(Variable src) {
    	push(String.format("[$FP%+d]", src.order));
    }

    public static void push(String src) {
		println("ADDI $SP $SP 1");
    	println("SET [$SP] " + src);
    }

    public static void pop(Variable dst) {
    	pop(String.format("[$FP%+d]", dst.order));
    }

    public static void pop(String dst) {
    	println("SET " + dst + " [$SP]");
		println("SUBI $SP $SP 1");
    }
    
    public static void returnVoid() {
    	println("SET $SP $FP");
        println("RETURN [$FP]");
    }
}