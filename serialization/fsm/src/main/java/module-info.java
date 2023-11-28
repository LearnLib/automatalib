open module net.automatalib.serialization.fsm {
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.serialization.core;

    requires org.checkerframework.checker.qual;

    exports net.automatalib.serialization.fsm.parser;
}