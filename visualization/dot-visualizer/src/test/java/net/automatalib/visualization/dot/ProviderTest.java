package net.automatalib.visualization.dot;

import net.automatalib.visualization.VPManager;
import net.automatalib.visualization.VisualizationProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProviderTest {

    @Test
    public void testProviderConfiguration() throws Exception {

        if (!DOT.checkUsable()) {
            // Do not fail on platforms, where DOT is not installed
            return;
        }

        final VPManager vpManager = new VPManager();

        vpManager.load();

        final VisualizationProvider swingProvider = vpManager.getProviderByName("graphviz-swing");
        final VisualizationProvider browserProvider = vpManager.getProviderByName("graphviz-browser");

        Assert.assertTrue(swingProvider instanceof GraphVizSwingVisualizationProvider);
        Assert.assertTrue(browserProvider instanceof GraphVizBrowserVisualizationProvider);
    }
}
