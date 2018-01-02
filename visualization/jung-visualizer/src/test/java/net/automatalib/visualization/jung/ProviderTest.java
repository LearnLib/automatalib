package net.automatalib.visualization.jung;

import net.automatalib.visualization.VPManager;
import net.automatalib.visualization.VisualizationProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProviderTest {

    @Test
    public void testProviderConfiguration() {
        final VPManager vpManager = new VPManager();

        vpManager.load();

        final VisualizationProvider provider = vpManager.getProviderByName("jung");

        Assert.assertTrue(provider instanceof JungGraphVisualizationProvider);
    }
}
