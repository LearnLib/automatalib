package net.automatalib.automaton.time.mmlt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Combines multiple String outputs by concatenating them and using a pipe as separator.
 */
public class StringSymbolCombiner extends AbstractSymbolCombiner<String> {

    private static final StringSymbolCombiner combiner = new StringSymbolCombiner();

    public static StringSymbolCombiner getInstance() {
        return combiner;
    }

    private StringSymbolCombiner() {

    }


    @Override
    public boolean isCombinedSymbol(String symbol) {
        return symbol.contains("|") && symbol.length() > 1;
    }

    @Override
    public String combineSymbols(List<String> symbols) {

        // Break all inputs (if needed) + put the results in a set:
        Set<String> expandedSymbols = new HashSet<>();
        for (var sym : symbols) {
            if (sym.equals("|")) {
                throw new IllegalArgumentException("The output | is reserved as delimiter.");
            }

            if (this.isCombinedSymbol(sym)) {
                expandedSymbols.addAll(this.separateSymbols(sym));
            } else {
                expandedSymbols.add(sym);
            }
        }

        // Sort the symbols + separate with pipe:
        return expandedSymbols.stream()
                .sorted()
                .collect(Collectors.joining("|"));
    }

    @Override
    public List<String> separateSymbols(String symbol) {
        if (!this.isCombinedSymbol(symbol)) {
            return List.of(symbol);
        }

        return Arrays.stream(symbol.split("\\|"))
                .distinct()
                .sorted()
                .toList();
    }
}
