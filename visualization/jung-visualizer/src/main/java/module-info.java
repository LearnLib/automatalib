import net.automatalib.visualization.VisualizationProvider;
import net.automatalib.visualization.jung.JungGraphVisualizationProvider;

open module net.automatalib.visualization.jung {
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;

    requires com.google.common;
    requires org.checkerframework.checker.qual;
    requires org.kohsuke.metainf_services;
    requires org.slf4j;

    requires graphviz.awt.shapes;
    requires jung.algorithms;
    requires jung.api;
    // patched in the compiler config because it contains split packages with jung.api
    //requires jung.graph.impl;
    requires jung.visualization;

    requires java.desktop;

    provides VisualizationProvider with JungGraphVisualizationProvider;

    exports net.automatalib.visualization.jung;
}