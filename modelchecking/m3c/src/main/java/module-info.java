open module net.automatalib.modelchecker.m3c {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;

    requires com.google.common;
    requires java.xml;
    requires org.checkerframework.checker.qual;

    requires addlib.core;

    exports net.automatalib.modelchecker.m3c.formula;
    exports net.automatalib.modelchecker.m3c.formula.ctl;
    exports net.automatalib.modelchecker.m3c.formula.modalmu;
    exports net.automatalib.modelchecker.m3c.formula.parser;
    exports net.automatalib.modelchecker.m3c.formula.visitor;
    exports net.automatalib.modelchecker.m3c.solver;
    exports net.automatalib.modelchecker.m3c.transformer;
    exports net.automatalib.modelchecker.m3c.visualization;
}