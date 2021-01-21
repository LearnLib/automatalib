package net.automatalib.modelcheckers.m3c;

import java.io.StringReader;
import java.util.Collection;

import net.automatalib.ts.modal.ModalTransitionSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws ParseException {
        LOGGER.info("Hello World!");
        new InternalM3CParser(new StringReader("qwe")).formula(); // passes
        new InternalM3CParser(new StringReader("asd")).formula(); // throws error
    }

    public static <S, I, T> void modelcheck(ModalTransitionSystem<S, I, T, ?> mts, Collection<? extends I> inputs) {

        for (S s : mts) {
            for (I i : inputs) {
                for (T t : mts.getTransitions(s, i)) {
                    LOGGER.info("Transition '{}' is a '{}' transition", t, mts.getTransitionProperty(t).getType());
                }
            }
        }
    }

}
