package fr.inria.astor.approaches.tos.core.evalTos.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.approaches.tos.core.MetaGenerator;
import fr.inria.astor.approaches.tos.core.evalTos.ingredients.IngredientFromDyna;
import fr.inria.astor.approaches.tos.core.evalTos.ops.simple.SingleWrapIfOperator;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.meta.MetaOperator;
import fr.inria.astor.core.entities.meta.MetaOperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;

/**
 * 
 * @author Matias Martinez
 *
 */
public class WrapwithIfOp extends ReplaceOp
		implements MetaOperator, DynaIngredientOperator, IOperatorWithTargetElement {
	private CtElement targetElement = null;

	@Override
	public List<OperatorInstance> createOperatorInstances(ModificationPoint modificationPoint) {
		log.error("This op needs ingredients");
		return null;
	}

	@Override
	public CtTypeReference retrieveTargetTypeReference() {

		return MutationSupporter.getFactory().createCtTypeReference(Boolean.class);
	}

	@Override
	public List<MetaOperatorInstance> createMetaOperatorInstances(ModificationPoint modificationPoint) {
		log.error("This op needs ingredients");
		return null;
	}

	@Override
	public List<MetaOperatorInstance> createMetaOperatorInstances(ModificationPoint modificationPoint,
			List<IngredientFromDyna> ingredients) {

		// The parameters to be included in the new method
		List<CtVariableAccess> varsToBeParameters = SupportOperators.collectAllVarsFromDynaIngredients(ingredients,
				modificationPoint);

		// List of parameters
		List<CtParameter<?>> parameters = new ArrayList<>();
		List<CtExpression<?>> realParameters = new ArrayList<>();
		for (CtVariableAccess ctVariableAccess : varsToBeParameters) {
			// the parent is null, it is setter latter
			CtParameter pari = MutationSupporter.getFactory().createParameter(null, ctVariableAccess.getType(),
					ctVariableAccess.getVariable().getSimpleName());
			parameters.add(pari);
			realParameters.add(ctVariableAccess.clone().setPositions(new NoSourcePosition()));
		}

		// let's start with one, and let's keep the Zero for the default (all ifs are
		// false)
		int candidateNumber = 0;
		CtTypeReference returnType = MutationSupporter.getFactory().createCtTypeReference(Boolean.class);

		CtElement codeElement = (targetElement == null) ? modificationPoint.getCodeElement() : targetElement;

		// let's create the meta

		MetaOperatorInstance megaOp = MetaGenerator.createMetaStatementReplacement(modificationPoint, codeElement,
				MutationSupporter.getFactory().createCodeSnippetExpression("true"), candidateNumber,
				ingredients.stream().map(Ingredient.class::cast).collect(Collectors.toList())//
				, parameters, realParameters, this, returnType);
		List<MetaOperatorInstance> opsMega = new ArrayList();
		opsMega.add(megaOp);

		return opsMega;
	}

	@Override
	public OperatorInstance getConcreteOperatorInstance(MetaOperatorInstance operatorInstance, int metaIdentifier) {

		// We retrieve the information from the operator instance

		Ingredient ingredient = operatorInstance.getAllIngredients().get(metaIdentifier);

		ModificationPoint modificationPoint = operatorInstance.getModificationPoint();

		CtStatement statementPointed = (CtStatement) modificationPoint.getCodeElement();

		SingleWrapIfOperator opIfOperator = new SingleWrapIfOperator(modificationPoint,
				(CtExpression<Boolean>) ingredient.getCode(), statementPointed, this);

		//

		return opIfOperator;
	}

	@Override
	public void setTargetElement(CtElement target) {
		this.targetElement = target;

	}

	@Override
	public boolean checkTargetCompatibility(CtElement target) {

		return true;
	}

	@Override
	public String identifier() {
		return "wrapsIfElse_Others";
	}
}
