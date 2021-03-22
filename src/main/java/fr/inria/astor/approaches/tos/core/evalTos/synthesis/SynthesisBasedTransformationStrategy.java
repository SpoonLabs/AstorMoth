package fr.inria.astor.approaches.tos.core.evalTos.synthesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.transformations.IngredientTransformationStrategy;
import fr.inria.main.evolution.AstorMain;
import fr.inria.main.evolution.PlugInLoader;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SynthesisBasedTransformationStrategy implements IngredientTransformationStrategy {

	ExecutionContextCollector contextColector = null;
	IngredientSynthesizer synthesizer = null;
	/**
	 * Stores the already analyzed contexts
	 */
	Map<ModificationPoint, ExecutionContext> cacheContent = new HashMap<>();

	public SynthesisBasedTransformationStrategy(ExecutionContextCollector contextColector,
			IngredientSynthesizer synthesizer) {
		super();
		this.contextColector = contextColector;
		this.synthesizer = synthesizer;
	}

	public SynthesisBasedTransformationStrategy() throws Exception {
		this.synthesizer = loadIngredientSynthesizer();
		this.contextColector = loadContextCollector();
	}

	@Override
	public List<Ingredient> transform(ModificationPoint modificationPoint, Ingredient ingredient) {

		ExecutionContext collectedValues = getContext(modificationPoint);

		CtType expectedType = null;
		if (modificationPoint.getCodeElement() instanceof CtExpression) {
			CtExpression exp = (CtExpression) modificationPoint.getCodeElement();
			expectedType = exp.getType().getTypeDeclaration();
		}
		List<CtElement> synthesizedElements = this.synthesizer.executeSynthesis(modificationPoint,
				modificationPoint.getCodeElement(), expectedType, modificationPoint.getContextOfModificationPoint(),
				collectedValues);

		List<Ingredient> ingredients = new ArrayList<>();
		for (CtElement ctElement : synthesizedElements) {
			ingredients.add(new Ingredient(ctElement));
		}

		return ingredients;
	}

	private ExecutionContext getContext(ModificationPoint modificationPoint) {
		if (this.cacheContent.containsKey(modificationPoint)) {
			return this.cacheContent.get(modificationPoint);
		} else {
			ExecutionContext collectedValues = contextColector.collectValues(AstorMain.projectFacade,
					modificationPoint);
			this.cacheContent.put(modificationPoint, collectedValues);
			return collectedValues;
		}
	}

	public ExecutionContextCollector loadContextCollector() throws Exception {

		//// CODE_SYNTHESIS("codesynthesis", IngredientSynthesizer.class), //

		return (ExecutionContextCollector) PlugInLoader.loadPlugin(ConfigurationProperties.getProperty("codesynthesis"),
				IngredientSynthesizer.class);

	}

	public IngredientSynthesizer loadIngredientSynthesizer() throws Exception {

		// CONTEXT_COLLECTOR("contextcollector", ExecutionContextCollector.class)
		return (IngredientSynthesizer) PlugInLoader.loadPlugin(ConfigurationProperties.getProperty("contextcollector"),
				ExecutionContextCollector.class);

	}

	public IngredientSynthesizer getSynthesizer() {
		return synthesizer;
	}

	public void setSynthesizer(IngredientSynthesizer synthesizer) {
		this.synthesizer = synthesizer;
	};

}
