package net.automatalib.incremental.mealy;

import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.words.Alphabet;

import org.testng.annotations.Test;

@Test
public class IncrementalMealyTreeBuilderTest extends AbstractIncrementalMealyBuilderTest {
	
	@Override
	protected <I,O> IncrementalMealyBuilder<I,O> createIncrementalMealyBuilder(Alphabet<I> alphabet) {
		return new IncrementalMealyTreeBuilder<>(alphabet);
	}
}
