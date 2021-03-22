package fr.inria.astor.approaches.tos.core.evalTos;

import java.util.ArrayList;
import java.util.List;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.tos.core.evalTos.ingredients.ClusterExpressions;
import fr.inria.astor.approaches.tos.core.evalTos.ingredients.DynaIngredientPool;
import fr.inria.astor.approaches.tos.core.evalTos.synthesis.dynamoth.DynamothSynthesisContext;
import fr.inria.astor.approaches.tos.core.evalTos.synthesis.dynamoth.DynamothSynthesizerWOracle;
import fr.inria.astor.approaches.tos.core.evalTos.synthesis.dynamoth.EvaluatedExpression;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.stats.Stats.GeneralStatEnum;
import fr.inria.astor.util.MapCounter;
import fr.inria.astor.util.MapList;
import fr.inria.lille.repair.common.Candidates;
import fr.inria.lille.repair.expression.Expression;
import fr.inria.lille.repair.expression.value.Value;
import fr.inria.main.AstorOutputStatus;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;

/**
 * An extension of EvalTOS, based on cluster
 * 
 * @author Matias Martinez
 *
 */
public class EvalTOSClusterApproach extends EvalTOSCoreApproach {

	public EvalTOSClusterApproach(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade)
			throws JSAPException {
		super(mutatorExecutor, projFacade);
	}

	@Override
	public boolean analyzeModificationPoint(ProgramVariant parentVariant, ModificationPoint iModifPoint)
			throws IllegalAccessException, Exception, IllegalAccessError {

		final boolean stop = true;
		List<ClusterExpressions> clusterEvaluatedExpressions = getEvaluatedExpression(iModifPoint);
		if (clusterEvaluatedExpressions == null)
			return false;

		List<CtCodeElement> holesFromMP = calculateHolesSorted(iModifPoint);
		log.info("For MP " + iModifPoint.identified + ",  total holes: " + holesFromMP.size());
		int maxMinutes = ConfigurationProperties.getPropertyInt("maxtime");

		int nrholefrommpi = 0;
		for (CtCodeElement iHole : holesFromMP) {
			nrholefrommpi++;
			if (!(iHole instanceof CtExpression)
					// We ignore holes that are statements
					|| (iHole instanceof CtStatement)) {
				continue;
			}

			if (iHole instanceof CtTypeAccess || iHole instanceof CtAnnotation) {
				log.debug("Discarting hole that is a CtTypeAccess/CTannotation:  " + iHole);
				continue;
			}

			// The hole to replace:
			CtExpression aholeExpression = (CtExpression) iHole;
			log.debug(
					"\n\n---hole-> `" + iHole + "`,  return type " + aholeExpression.getType().box().getQualifiedName()
							+ "--hole type: " + iHole.getClass().getCanonicalName());

			log.info("number of clusters " + clusterEvaluatedExpressions.size());

			int valuefromtesti = 0;
			for (ClusterExpressions i_cluster : clusterEvaluatedExpressions) {
				valuefromtesti++;
				if (i_cluster.size() > 0) {
					EvaluatedExpression firstExpressionOfCluster = i_cluster.get(0);

					if (firstExpressionOfCluster.asPatch().toString().equals(iHole.toString())) {
						continue;
					}
					operationsExecuted++;

					// let's check the types
					String classofExpression = i_cluster.getClusterType();// returnExpressionType(firstExpressionOfCluster);//
																			// firstExpressionOfCluster.getValue().getType().getCanonicalName();

					String classofiHole = aholeExpression.getType().box().getQualifiedName();

					if (!ConfigurationProperties.getPropertyBool("avoidtypecomparison")// In case that we dont want
																						// to compare hole types
							&& !classofiHole.equals(classofExpression)) {
						continue;
					}

					// log.info(String.format("--Analyzing value %d/%d from hole %d",
					// valuefromtesti,
					// nrtestfromholei, nrholefrommpi));

					currentStat.increment(GeneralStatEnum.NR_GENERATIONS);
					boolean isExpressionASolution = createAndEvaluatePatch(operationsExecuted, parentVariant,
							iModifPoint, aholeExpression, firstExpressionOfCluster);

					if (isExpressionASolution) {
						// log.info(String.format("Patch found with expresion %s evaluated as %s in
						// modif point %s ",
						// firstExpressionOfCluster.asPatch(),
						// firstExpressionOfCluster.getEvaluations().get(i_testName),
						// iModifPoint.toString()));
					}
					if (MAX_GENERATIONS <= operationsExecuted) {

						this.setOutputStatus(AstorOutputStatus.MAX_GENERATION);
						log.info("Stop-Max operator Applied " + operationsExecuted);
						return stop;
					}

					if (!(belowMaxTime(dateInitEvolution, maxMinutes))) {
						log.debug("\n Max time reached " + generationsExecuted);
						this.outputStatus = AstorOutputStatus.TIME_OUT;
						return stop;
					}

					boolean stopSearch = !this.solutions.isEmpty() && (ConfigurationProperties
							.getPropertyBool("stopfirst")
							// or nr solutions are greater than max
							// allowed
							|| (this.solutions.size() >= ConfigurationProperties.getPropertyInt("maxnumbersolutions")));

					if (stopSearch) {
						log.debug("\n Max Solution found " + this.solutions.size());
						this.outputStatus = AstorOutputStatus.STOP_BY_PATCH_FOUND;
						return stop;
					}

				}

			}

		}
		return !stop;

	}

	public List<ClusterExpressions> getEvaluatedExpression(ModificationPoint iModifPoint) {
		DynamothSynthesisContext contextCollected = null;
		try {
			contextCollected = this.collectorFacade.collectValues(getProjectFacade(), iModifPoint);
		} catch (Exception e) {
			log.error("Error calling Dynamoth value recolection MP id: " + iModifPoint.identified);
			log.error("Failing collecting values from class: "
					+ iModifPoint.getCodeElement().getParent(CtClass.class).getQualifiedName());
			log.error(e);
			currentStat.increment(GeneralStatEnum.NR_ERRONEOUS_VARIANCES);
			return null;
		}
		// Collecting values:
		// values are collected from all test.

		// Creating combinations (do not depend on the Holes because
		// they are combination of variables in context of a
		// modification point)
		DynamothSynthesizerWOracle synthesizer = new DynamothSynthesizerWOracle(contextCollected);

		Candidates evaluatedExpressions = synthesizer.combineValues();

		if (evaluatedExpressions.isEmpty()) {
			log.error("Error: not collected values for MP " + iModifPoint);
		}
		// This is only for logging info...
		MapCounter<String> typesOfCandidatesCombined = getTypesOfExpressions(evaluatedExpressions);

		log.info("types Of Candidates: " + typesOfCandidatesCombined.sorted());

		log.info("Start clustering");
		// Key: test name, value list of clusters, each cluster is a list of
		// evaluated expressions
		List<ClusterExpressions> cluster = clusterCandidatesByValue(evaluatedExpressions);
		return cluster;
	}

	public DynaIngredientPool getClusteredEvaluatedExpression(ModificationPoint iModifPoint) {
		List<ClusterExpressions> cluster = this.getEvaluatedExpression(iModifPoint);
		return new DynaIngredientPool(cluster);
	}

	private MapCounter<String> getTypesOfExpressions(Candidates candidatesnew) {
		MapCounter<String> typesOfCandidates = new MapCounter<>();
		for (Expression expression : candidatesnew) {

			String type = returnExpressionType(expression);
			typesOfCandidates.add(type);
		}
		return typesOfCandidates;
	}

	protected String returnExpressionType(Expression expression) {

		if (expression.getValue() == null) {
			log.debug("Value of expression " + expression + "  is null");
			return null;
		}

		if (expression.getValue().getType() == null) {
			log.debug("Type of expression  value " + expression.getValue() + "  is null");
			return null;
		}

		String type = expression.getValue().getType().toString().replace("class ", "");

		if ("com.sun.tools.jdi.ObjectReferenceImpl".equals(type)) {
			type = expression.getValue().toString().replace("instance of ", "").split("\\(")[0];
		}
		return type;
	}

	public List<ClusterExpressions> clusterCandidatesByValue(Candidates candidates) {

		log.debug("number candidates " + candidates.size());

		// For each test:
		// test name, cluster of expressions
		List<ClusterExpressions> clusters = new ArrayList<>();

		for (int i = 0; i < candidates.size(); i++) {
			EvaluatedExpression i_expression_under_Classification = (EvaluatedExpression) candidates.get(i);

			// For all test, the values are the similar
			boolean clustered = false;

			for (ClusterExpressions existingCluster : clusters) {

				// that should not happen, but just in case:
				if (existingCluster.size() == 0)
					continue;

				EvaluatedExpression alreadyClustered = existingCluster.get(0);
				boolean fulfillAllValues = true;
				for (String i_testName : alreadyClustered.getEvaluations().keySet()) {

					double similarity = calculateSimilarity(i_testName, alreadyClustered,
							i_expression_under_Classification);

					if (similarity < COMPARISON_THRESHOLD) {
						// i.e., don't belong to the cluster
						fulfillAllValues = false;
						break;
					}

				}
				// the cluster corresponds to the expression
				if (fulfillAllValues) {
					existingCluster.add(i_expression_under_Classification);
					clustered = true;
				}

			}
			// Any cluster is similar
			if (!clustered) {
				String expressionType = returnExpressionType(i_expression_under_Classification);
				ClusterExpressions evacluster = new ClusterExpressions(expressionType);
				evacluster.add(i_expression_under_Classification);
				clusters.add(evacluster);
			}

		}

		return clusters;
	}

	/**
	 * Key: test name, value list of clusters, each cluster is a list of evaluated
	 * expressions EACH cluster has a list of expressions that have the same value
	 * (the expression evaluation) on a i-th evaluation of the variable.
	 * 
	 * @param candidates
	 * @return
	 */

	public MapList<String, ClusterExpressions> clusterCandidatesByValueOLD(Candidates candidates) {

		log.debug("number candidates " + candidates.size());

		// For each test:
		// test name, cluster of expressions
		MapList<String, ClusterExpressions> cluster = new MapList<>();

		for (int i = 0; i < candidates.size(); i++) {
			EvaluatedExpression i_expression = (EvaluatedExpression) candidates.get(i);

			for (String i_testName : i_expression.getEvaluations().keySet()) {

				if (!cluster.containsKey(i_testName)) {

					String expressionType = returnExpressionType(i_expression);
					ClusterExpressions evacluster = new ClusterExpressions(expressionType);
					evacluster.add(i_expression);
					cluster.add(i_testName, evacluster);
				} else {

					List<ClusterExpressions> clusterOfTest = cluster.get(i_testName);
					boolean notClustered = true;
					for (List<EvaluatedExpression> elementsFromCluster : clusterOfTest) {

						if (elementsFromCluster != null && elementsFromCluster.size() > 0) {
							EvaluatedExpression alreadyClustered = elementsFromCluster.get(0);

							double similarity = calculateSimilarity(i_testName, alreadyClustered, i_expression);

							if (similarity >= COMPARISON_THRESHOLD) {

								elementsFromCluster.add(i_expression);
								notClustered = false;
								break;
							}
						}

					}
					if (notClustered) {
						// List<EvaluatedExpression> evacluster = new ArrayList<>();
						String expressionType = returnExpressionType(i_expression);
						ClusterExpressions evacluster = new ClusterExpressions(expressionType);
						evacluster.add(i_expression);
						clusterOfTest.add(evacluster);
					}

				}

			}

		}

		return cluster;
	}

	private double calculateSimilarity(String i_testName, EvaluatedExpression alreadyClustered,
			EvaluatedExpression i_expression) {
		List<Value> valuesofTestToCluster = i_expression.getEvaluations().get(i_testName);
		List<Value> valuesofTestAlreadyClustered = alreadyClustered.getEvaluations().get(i_testName);

		int max = valuesofTestToCluster.size() > valuesofTestAlreadyClustered.size() ? valuesofTestToCluster.size()
				: valuesofTestAlreadyClustered.size();

		int total = 0;
		for (int i = 0; i < max; i++) {

			if (i < valuesofTestToCluster.size() && i < valuesofTestAlreadyClustered.size()
					&& valuesofTestToCluster.get(i) != null && valuesofTestAlreadyClustered.get(i) != null) {
				try {
					Object v1 = valuesofTestToCluster.get(i).getRealValue();
					Object v2 = valuesofTestAlreadyClustered.get(i).getRealValue();
					if (v1 != null && v2 != null && v1.equals(v2)) {
						total += 1;
					}
				} catch (Exception e) {
					log.equals(e);
				}
			}
		}

		return (double) total / (double) max;
	}

}
