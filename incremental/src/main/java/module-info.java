open module net.automatalib.incremental {
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.core;
    requires net.automatalib.util;

    requires org.checkerframework.checker.qual;

    exports net.automatalib.incremental;
    exports net.automatalib.incremental.dfa;
    exports net.automatalib.incremental.dfa.dag;
    exports net.automatalib.incremental.dfa.tree;
    exports net.automatalib.incremental.mealy;
    exports net.automatalib.incremental.mealy.dag;
    exports net.automatalib.incremental.mealy.tree;
    exports net.automatalib.incremental.moore;
    exports net.automatalib.incremental.moore.dag;
    exports net.automatalib.incremental.moore.tree;
}