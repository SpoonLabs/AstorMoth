package fr.inria.astor.approaches.tos.core.evalTos.ops;

import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface IOperatorWithTargetElement {

	public void setTargetElement(CtElement target);

	public boolean checkTargetCompatibility(CtElement target);
}
