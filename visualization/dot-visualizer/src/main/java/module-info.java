import net.automatalib.visualization.VisualizationProvider;
import net.automatalib.visualization.dot.GraphVizBrowserVisualizationProvider;
import net.automatalib.visualization.dot.GraphVizSwingVisualizationProvider;

open module net.automatalib.visualization.dot {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.serialization.dot;

    requires java.desktop;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static org.kohsuke.metainf_services;

    provides VisualizationProvider with GraphVizSwingVisualizationProvider, GraphVizBrowserVisualizationProvider;

    exports net.automatalib.visualization.dot;
}