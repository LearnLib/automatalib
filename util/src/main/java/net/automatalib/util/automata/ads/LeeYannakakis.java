package net.automatalib.util.automata.ads;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import net.automatalib.automata.ads.ADSNode;
import net.automatalib.automata.ads.impl.ADSFinalNode;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.graphs.base.compact.CompactSimpleGraph;
import net.automatalib.util.graphs.Path;
import net.automatalib.util.graphs.ShortestPaths;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Algorithm of Lee & Yannakakis for computing adaptive distinguishing sequences (of length at most n^2) in O(n^2) time
 * (where n denotes the number of states of the automaton).
 * <br/>
 * See: D. Lee and M. Yannakakis - "Testing Finite-State Machines: State Identification and Verification",
 * IEEE Transactions on Computers 43.3 (1994)
 *
 * @author frohme
 */
public class LeeYannakakis {

	private enum Validity {
		A_VALID,
		B_VALID,
		C_VALID,
		INVALID
	}

	/**
	 * Computes an ADS using the algorithm of Lee & Yannakakis.
	 *
	 * @param automaton The automaton for which an ADS should be computed
	 * @param input     the input alphabet of the automaton
	 * @param <S>       (hypothesis) state type
	 * @param <I>       input alphabet type
	 * @param <O>       output alphabet type
	 * @return A {@link LYResult} containing an adaptive distinguishing sequence (if existent) and a possible set of
	 * indistinguishable states.
	 */
	public static <S, I, O> LYResult<S, I, O> compute(final MealyMachine<S, I, ?, O> automaton,
													  final Alphabet<I> input) {

		final SplitTreeResult<S, I, O> str = computeSplitTree(automaton, input);

		if (str.isPresent()) {
			final Set<S> states = new HashSet<>(automaton.getStates());
			return new LYResult<>(extractADS(automaton,
											 str.get(),
											 states,
											 states.stream()
													 .collect(Collectors.toMap(Function.identity(),
																			   Function.identity())),
											 null));
		}

		return new LYResult<>(str.getIndistinguishableStates());
	}

	private static <S, I, O> SplitTreeResult<S, I, O> computeSplitTree(final MealyMachine<S, I, ?, O> automaton,
																	   final Alphabet<I> input) {

		final SplitTree<S, I, O> st = new SplitTree<>(new HashSet<>(automaton.getStates()));
		final Set<SplitTree<S, I, O>> leaves = Sets.newHashSetWithExpectedSize(automaton.size());
		leaves.add(st);

		while (leaves.stream().anyMatch(LeeYannakakis::needsRefinement)) {

			final int maxCardinality = leaves.stream().mapToInt(x -> x.getPartition().size()).max().getAsInt();
			final Set<SplitTree<S, I, O>> R =
					leaves.stream().filter(x -> x.getPartition().size() == maxCardinality).collect(Collectors.toSet());

			final Map<Validity, Set<Pair<Word<I>, SplitTree<S, I, O>>>> validitySetMap =
					computeValidities(automaton, input, R, leaves);

			if (!validitySetMap.get(Validity.INVALID).isEmpty()) {
				final Set<Pair<Word<I>, SplitTree<S, I, O>>> set = validitySetMap.get(Validity.INVALID);

				final Set<S> indistinguishableStates = new HashSet<>();

				for (final Pair<Word<I>, SplitTree<S, I, O>> pair : set) {
					indistinguishableStates.addAll(pair.getSecond().getPartition());
				}

				return new SplitTreeResult<>(indistinguishableStates);
			}

			// a-valid partitions
			for (final Pair<Word<I>, SplitTree<S, I, O>> B_a : validitySetMap.get(Validity.A_VALID)) {

				assert B_a.getFirst().size() == 1 : new IllegalStateException(
						"a-valid inputs should always contain exactly 1 symbol");

				final I aValidInput = B_a.getFirst().firstSymbol();
				final SplitTree<S, I, O> nodeToRefine = B_a.getSecond();
				final Map<O, Set<S>> successorMap = nodeToRefine.getPartition()
						.stream()
						.collect(Collectors.groupingBy(s -> automaton.getOutput(s, aValidInput), Collectors.toSet()));

				nodeToRefine.setSequence(Word.fromSymbols(aValidInput));
				leaves.remove(nodeToRefine);

				for (Map.Entry<O, Set<S>> entry : successorMap.entrySet()) {
					final SplitTree<S, I, O> child = new SplitTree<>(entry.getValue());
					nodeToRefine.getSuccessors().put(entry.getKey(), child);
					leaves.add(child);
				}
				for (final S s : nodeToRefine.getPartition()) {
					nodeToRefine.getMapping().put(s, automaton.getSuccessor(s, aValidInput));
				}
			}

			// b-valid partitions
			for (final Pair<Word<I>, SplitTree<S, I, O>> B_b : validitySetMap.get(Validity.B_VALID)) {

				assert B_b.getFirst().size() == 1 : new IllegalStateException(
						"b-valid inputs should always contain exactly 1 symbol");

				final I bValidInput = B_b.getFirst().firstSymbol();
				final SplitTree<S, I, O> nodeToRefine = B_b.getSecond();
				final Map<S, S> successorsToNodes = nodeToRefine.getPartition()
						.stream()
						.collect(Collectors.toMap(x -> automaton.getSuccessor(x, bValidInput), Function.identity()));
				final SplitTree<S, I, O> v =
						st.findLowestSubsetNode(successorsToNodes.keySet()).orElseThrow(IllegalStateException::new);

				nodeToRefine.setSequence(v.getSequence().prepend(bValidInput));
				leaves.remove(nodeToRefine);

				for (final Map.Entry<O, SplitTree<S, I, O>> entry : v.getSuccessors().entrySet()) {

					final Set<S> wSet = entry.getValue().getPartition();
					final Set<S> intersection = new HashSet<>(successorsToNodes.keySet());
					intersection.retainAll(wSet);

					if (!intersection.isEmpty()) {
						final Set<S> indistinguishableNodes =
								intersection.stream().map(successorsToNodes::get).collect(Collectors.toSet());
						final SplitTree<S, I, O> newChild = new SplitTree<>(indistinguishableNodes);
						nodeToRefine.getSuccessors().put(entry.getKey(), newChild);
						leaves.add(newChild);
					}
				}
				for (final S s : nodeToRefine.getPartition()) {
					nodeToRefine.getMapping().put(s, v.getMapping().get(automaton.getSuccessor(s, bValidInput)));
				}
			}

			// c-valid partitions
			for (final Pair<Word<I>, SplitTree<S, I, O>> B_c : validitySetMap.get(Validity.C_VALID)) {
				final Word<I> cValidInput = B_c.getFirst();
				final SplitTree<S, I, O> nodeToRefine = B_c.getSecond();
				final Map<S, S> successorsToNodes = nodeToRefine.getPartition()
						.stream()
						.collect(Collectors.toMap(x -> automaton.getSuccessor(x, cValidInput), Function.identity()));
				final SplitTree<S, I, O> C =
						st.findLowestSubsetNode(successorsToNodes.keySet()).orElseThrow(IllegalStateException::new);

				nodeToRefine.setSequence(cValidInput.concat(C.getSequence()));
				leaves.remove(nodeToRefine);

				for (final Map.Entry<O, SplitTree<S, I, O>> entry : C.getSuccessors().entrySet()) {

					final Set<S> wSet = entry.getValue().getPartition();
					final Set<S> intersection = new HashSet<>(successorsToNodes.keySet());
					intersection.retainAll(wSet);

					if (!intersection.isEmpty()) {
						final Set<S> indistinguishableNodes =
								intersection.stream().map(successorsToNodes::get).collect(Collectors.toSet());
						final SplitTree<S, I, O> newChild = new SplitTree<>(indistinguishableNodes);
						nodeToRefine.getSuccessors().put(entry.getKey(), newChild);
						leaves.add(newChild);
					}
				}
				for (final S s : nodeToRefine.getPartition()) {
					nodeToRefine.getMapping().put(s, C.getMapping().get(automaton.getSuccessor(s, cValidInput)));
				}
			}
		}

		return new SplitTreeResult(st);
	}

	private static <S, I, O> ADSNode<S, I, O> extractADS(final MealyMachine<S, I, ?, O> automaton,
														 final SplitTree<S, I, O> st,
														 final Set<S> currentSet,
														 final Map<S, S> currentToInitialMapping,
														 final ADSNode<S, I, O> predecessor) {

		if (currentSet.size() == 1) {
			final S currentNode = currentSet.iterator().next();

			assert currentToInitialMapping.containsKey(currentNode) : new IllegalStateException();

			return new ADSFinalNode<>(predecessor, currentToInitialMapping.get(currentNode));
		}

		final SplitTree<S, I, O> u = st.findLowestSubsetNode(currentSet).orElseThrow(IllegalStateException::new);
		final Pair<ADSNode<S, I, O>, ADSNode<S, I, O>> ads =
				ADSUtil.buildFromTrace(automaton, u.getSequence(), currentSet.iterator().next());
		final ADSNode<S, I, O> head = ads.getFirst();
		final ADSNode<S, I, O> tail = ads.getSecond();

		head.setParent(predecessor);

		for (final Map.Entry<O, SplitTree<S, I, O>> entry : u.getSuccessors().entrySet()) {

			final O output = entry.getKey();
			final SplitTree<S, I, O> u_i = entry.getValue();
			final Set<S> intersection = new HashSet<>(u_i.getPartition());
			intersection.retainAll(currentSet);

			if (!intersection.isEmpty()) {
				final Map<S, S> nextCurrentToInitialMapping = intersection.stream()
						.collect(Collectors.toMap(key -> u.getMapping().get(key), currentToInitialMapping::get));

				final Set<S> nextCurrent =
						intersection.stream().map(x -> u.getMapping().get(x)).collect(Collectors.toSet());
				tail.getChildren()
						.put(output, extractADS(automaton, st, nextCurrent, nextCurrentToInitialMapping, tail));
			}
		}

		return head;
	}

	private static <S, I, O> boolean needsRefinement(final SplitTree<S, I, O> node) {
		return node.getPartition().size() > 1;
	}

	private static <S, I, O> boolean isValidInput(final MealyMachine<S, I, ?, O> automaton,
												  final I input,
												  final Set<S> states) {

		final Map<O, Set<S>> successors = new HashMap<>();

		for (final S s : states) {
			final O output = automaton.getOutput(s, input);
			final S successor = automaton.getSuccessor(s, input);

			if (!successors.containsKey(output)) {
				successors.put(output, new HashSet<>());
			}

			if (!successors.get(output).add(successor)) {
				return false;
			}
		}

		return true;
	}

	private static <S, I, O> Map<Validity, Set<Pair<Word<I>, SplitTree<S, I, O>>>> computeValidities(final MealyMachine<S, I, ?, O> automaton,
																									 final Alphabet<I> inputs,
																									 final Set<SplitTree<S, I, O>> R,
																									 final Set<SplitTree<S, I, O>> pi) {

		final Map<Validity, Set<Pair<Word<I>, SplitTree<S, I, O>>>> result = new EnumMap<>(Validity.class);
		final Map<S, Integer> stateToPartitionMap = new HashMap<>();
		final BiMap<Integer, SplitTree<S, I, O>> partitionToNodeMap = HashBiMap.create();

		int counter = 0;
		final Iterator<SplitTree<S, I, O>> partitionIterator = pi.iterator();
		while (partitionIterator.hasNext()) {
			final SplitTree<S, I, O> partition = partitionIterator.next();
			for (final S s : partition.getPartition()) {
				final Integer previousValue = stateToPartitionMap.put(s, counter);
				assert previousValue == null : new IllegalStateException("Not a true partition");
			}
			partitionToNodeMap.put(counter, partition);
			counter++;
		}

		for (final Validity v : Validity.values()) {
			result.put(v, new HashSet<>());
		}

		final Set<SplitTree<S, I, O>> pendingCs = new HashSet<>();
		final Map<Integer, Validity> partitionToClassificationMap = new HashMap<>();

		final CompactSimpleGraph<I> implicationGraph = new CompactSimpleGraph<>(partitionToNodeMap.size());

		for (final Integer idx : partitionToNodeMap.keySet()) {
			implicationGraph.addIntNode();
		}

		partitionLoop:
		for (final SplitTree<S, I, O> B : R) {

			// general validity
			final Map<I, Boolean> validInputMap = inputs.stream()
					.collect(Collectors.toMap(Function.identity(),
											  input -> isValidInput(automaton, input, B.getPartition())));

			// a valid
			for (final I i : inputs) {

				if (!validInputMap.get(i)) {
					continue;
				}

				final Set<O> outputs =
						B.getPartition().stream().map(s -> automaton.getOutput(s, i)).collect(Collectors.toSet());

				if (outputs.size() > 1) {
					result.get(Validity.A_VALID).add(new Pair<>(Word.fromSymbols(i), B));
					partitionToClassificationMap.put(stateToPartitionMap.get(B.getPartition().iterator().next()),
													 Validity.A_VALID);
					continue partitionLoop;
				}
			}

			// b valid
			for (final I i : inputs) {

				if (!validInputMap.get(i)) {
					continue;
				}

				final Set<Integer> successors = B.getPartition()
						.stream()
						.map(s -> stateToPartitionMap.get(automaton.getSuccessor(s, i)))
						.collect(Collectors.toSet());

				if (successors.size() > 1) {
					result.get(Validity.B_VALID).add(new Pair<>(Word.fromSymbols(i), B));
					partitionToClassificationMap.put(stateToPartitionMap.get(B.getPartition().iterator().next()),
													 Validity.B_VALID);
					continue partitionLoop;
				}
			}

			// c valid
			// we defer evaluation to later point in time, because we need to check if the target partitions are a- or b-valid
			for (final I i : inputs) {

				if (!validInputMap.get(i)) {
					continue;
				}

				final S nodeInPartition = B.getPartition().iterator().next();
				final S successor = automaton.getSuccessor(nodeInPartition, i);

				final Integer partition = stateToPartitionMap.get(nodeInPartition);
				final Integer successorPartition = stateToPartitionMap.get(successor);

				if (!partition.equals(successorPartition)) {
					implicationGraph.connect(partition, successorPartition, i);
					pendingCs.add(B);
				}
			}

			if (pendingCs.contains(B)) {
				continue partitionLoop;
			}

			//if we haven't continued the loop up until here, there is no valid input
			result.get(Validity.INVALID).add(new Pair<>(null, B));
		}

		//check remaining potential Cs
		pendingCLoop:
		for (final SplitTree<S, I, O> pendingC : pendingCs) {

			final Integer pendingPartition = partitionToNodeMap.inverse().get(pendingC);
			final Iterator<Integer> iter =
					GraphTraversal.bfIterator(implicationGraph, Collections.singleton(pendingPartition));

			while (iter.hasNext()) {

				final Integer successor = iter.next();
				final Validity successorValidity = partitionToClassificationMap.get(successor);
				if (successorValidity == Validity.A_VALID || successorValidity == Validity.B_VALID) {
					final Path<Integer, CompactEdge<I>> path = ShortestPaths.shortestPath(implicationGraph,
																						  pendingPartition,
																						  implicationGraph.size(),
																						  successor);
					final List<I> word =
							path.edgeList().stream().map(CompactEdge::getProperty).collect(Collectors.toList());

					result.get(Validity.C_VALID).add(new Pair<>(Word.fromList(word), pendingC));
					continue pendingCLoop;
				}
			}

			result.get(Validity.INVALID).add(new Pair<>(null, pendingC));
		}

		return result;
	}
}
