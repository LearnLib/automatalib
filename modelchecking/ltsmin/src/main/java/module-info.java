open module net.automatalib.modelchecker.ltsmin {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.serialization.fsm;
    requires net.automatalib.serialization.etf;
    requires net.automatalib.util;

    requires com.google.common;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static de.learnlib.tooling.annotation.processor;

    exports net.automatalib.modelchecker.ltsmin;
    exports net.automatalib.modelchecker.ltsmin.ltl;
    exports net.automatalib.modelchecker.ltsmin.monitor;
}