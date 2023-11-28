import net.automatalib.common.util.setting.SettingsSource;

open module net.automatalib.common.util {

    requires net.automatalib.common.smartcollection;

    requires com.google.common;
    requires java.management;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    uses SettingsSource;

    exports net.automatalib.common.util;
    exports net.automatalib.common.util.array;
    exports net.automatalib.common.util.collection;
    exports net.automatalib.common.util.comparison;
    exports net.automatalib.common.util.concurrent;
    exports net.automatalib.common.util.fixpoint;
    exports net.automatalib.common.util.function;
    exports net.automatalib.common.util.io;
    exports net.automatalib.common.util.lib;
    exports net.automatalib.common.util.mapping;
    exports net.automatalib.common.util.nid;
    exports net.automatalib.common.util.process;
    exports net.automatalib.common.util.random;
    exports net.automatalib.common.util.ref;
    exports net.automatalib.common.util.setting;
    exports net.automatalib.common.util.string;
    exports net.automatalib.common.util.system;
}
