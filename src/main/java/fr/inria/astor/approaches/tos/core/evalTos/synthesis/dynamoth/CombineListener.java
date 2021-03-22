package fr.inria.astor.approaches.tos.core.evalTos.synthesis.dynamoth;

import fr.inria.lille.repair.expression.Expression;

public interface CombineListener {
	boolean check(Expression expression);
}
