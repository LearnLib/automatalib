package net.automatalib.incremental.mealy;

import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.words.Alphabet;

public class IncrementalMealyDAGBuilderTest extends AbstractIncrementalMealyBuilderTest {
	
	@Override
	protected <I,O> IncrementalMealyBuilder<I,O> createIncrementalMealyBuilder(Alphabet<I> alphabet) {
		return new IncrementalMealyDAGBuilder<>(alphabet);
	}

}
