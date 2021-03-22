package fr.inria.astor.approaches.tos.core.evalTos.synthesis.dynamoth.spoon;


import java.util.List;

public interface Operator {
    String getSymbol();

    Class getReturnType();

    List<Class> getTypeParameters();
}

