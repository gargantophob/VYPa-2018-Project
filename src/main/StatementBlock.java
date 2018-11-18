package main;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

public class StatementBlock /*implements Scope*/ {
    public List<Statement> statements;
    
    /*public Scope parentScope;
    public List<String> names;*/

    public StatementBlock(List<Statement> statements) {
        this.statements = statements;
    }

    /*@Override
    public String toString() {
        return statements.toString();
    }*/

    /*public void assertDefineteness(Scope parentScope) {
        this.parentScope = parentScope;
        names = new ArrayList<>();
        for(int i = 0; i < statements.size(); i++) {
            Statement s = statements.get(i);
            String name;
            switch(s.kind) {
                case DECLARATION:
                    Variable v = s.variable;
                    v.assertVoid();
                    v.assertObject();
                    v.assertDefineteness(this);
                    names.add(v.name);
                    break;
                case ASSIGNMENT:
                    s.path.assertSimpleness();
                    s.path.assertDefineteness(this);
                    s.expression.assertDefineteness(this);
                    break;
                case CONDITIONAL:
                    s.expression.assertDefineteness(this);
                    s.body.assertDefineteness(this);
                    s.bodyElse.assertDefineteness(this);
                    break;
                case ITERATION:
                    s.expression.assertDefineteness(this);
                    s.body.assertDefineteness(this);
                    break;
                case CALL:
                    s.call.assertDefineteness(this);
                    break;
                case RETURN:
                    if(s.expression != null) {
                        s.expression.assertDefineteness(this);
                    }
                    break;
            }
        }
    }*/

    /*@Override
    public boolean isDefinedVariableHere(String name) {
        return names.contains(name);
    }

    @Override
    public boolean isDefinedVariable(String name) {
        return isDefinedVariableHere(name) || parentScope.isDefinedVariable(name);
    }

    @Override
    public boolean isDefinedFunction(String name) {
        return parentScope.isDefinedFunction(name);
    }

    @Override
    public boolean isDefinedClass(String name) {
        return parentScope.isDefinedClass(name);
    }*/
}
