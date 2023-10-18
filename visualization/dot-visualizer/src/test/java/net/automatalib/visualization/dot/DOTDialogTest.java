/* Copyright (C) 2013-2023 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.visualization.dot;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import com.google.common.io.CharStreams;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.system.JVMUtil;
import org.assertj.swing.awt.AWT;
import org.assertj.swing.core.ComponentDragAndDrop;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JFileChooserFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JFileChooserFixture;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.assertj.swing.testng.testcase.AssertJSwingTestngTestCase;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DOTDialogTest extends AssertJSwingTestngTestCase {

    private final String dot;
    private DialogFixture window;

    public DOTDialogTest() throws IOException {
        this.dot =
                CharStreams.toString(IOUtil.asBufferedUTF8Reader(DOTDialogTest.class.getResourceAsStream("/dfa.dot")));
    }

    @BeforeClass
    public void beforeClass() {
        if (!DOT.checkUsable()) {
            // Do not fail on platforms, where DOT is not installed
            throw new SkipException("DOT is not installed");
        }

        final int canonicalSpecVersion = JVMUtil.getCanonicalSpecVersion();
        if (!(canonicalSpecVersion <= 8 || canonicalSpecVersion == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }
    }

    @Override
    protected void onSetUp() {
        final int canonicalSpecVersion = JVMUtil.getCanonicalSpecVersion();
        if (DOT.checkUsable() && (canonicalSpecVersion <= 8 || canonicalSpecVersion == 11)) {
            final DOTDialog frame = GuiActionRunner.execute(() -> new DOTDialog(dot, false));
            window = new DialogFixture(robot(), frame);
            window.show(); // shows the frame to test
        }
    }

    @Test
    public void testDragNDrop() {
        final Robot robot = robot();
        final ComponentDragAndDrop dnd = new ComponentDragAndDrop(robot);

        final JScrollPane pane = window.scrollPane().target();
        final Point center = AWT.visibleCenterOf(pane);
        final Point topleft = new Point(-2000, -2000);
        final Point topright = new Point(2000, -2000);
        final Point bottomleft = new Point(-2000, 2000);
        final Point bottomright = new Point(2000, 2000);

        /* TODO: the dragOvers seem to be missed/skipped in the headless environment.
         *       However in a non-headless environment, they do exactly what we want.
         *       So this seems to be an issue with cacio-tta
         */
        dnd.drag(pane, center);
        dnd.dragOver(pane, topleft);
        dnd.dragOver(pane, bottomleft);
        dnd.dragOver(pane, topright);
        dnd.dragOver(pane, bottomright);
        dnd.drop(pane, center);

        window.close();
    }

    @Test
    public void testClickClose() {
        window.menuItem(findByText(JMenuItem.class, "Close")).click();
    }

    @Test
    public void testSaveDOT() throws IOException {
        final JMenuItemFixture saveDotItem = window.menuItem(findByText(JMenuItem.class, "Save DOT"));

        final File tmpFile = Files.createTempFile("save-dot", ".dot").toFile();
        tmpFile.deleteOnExit();

        // aborting
        saveDotItem.click();
        JFileChooserFixture fileChooser = JFileChooserFinder.findFileChooser().using(robot());
        fileChooser.selectFile(tmpFile).cancel();

        Assert.assertEquals(tmpFile.length(), 0);

        // saving
        saveDotItem.click();
        fileChooser = JFileChooserFinder.findFileChooser().using(robot());
        fileChooser.selectFile(tmpFile).approve();

        Assert.assertEquals(CharStreams.toString(IOUtil.asBufferedUTF8Reader(tmpFile)), dot);
    }

    @Test
    public void testSavePNG() throws IOException {
        final JMenuItemFixture savePngItem = window.menuItem(findByText(JMenuItem.class, "Save PNG"));

        final File tmpFile = Files.createTempFile("save-png", ".png").toFile();
        tmpFile.deleteOnExit();

        // aborting
        savePngItem.click();
        JFileChooserFixture fileChooser = JFileChooserFinder.findFileChooser().using(robot());
        fileChooser.selectFile(tmpFile).cancel();

        Assert.assertEquals(tmpFile.length(), 0);

        // saving
        savePngItem.click();
        fileChooser = JFileChooserFinder.findFileChooser().using(robot());
        fileChooser.selectFile(tmpFile).approve();

        String mimeType = Files.probeContentType(tmpFile.toPath());
        Assert.assertEquals(mimeType, "image/png");
    }

    private static <T extends AbstractButton> GenericTypeMatcher<T> findByText(Class<T> clazz, String name) {
        return new GenericTypeMatcher<T>(clazz) {

            @Override
            protected boolean isMatching(T item) {
                return name.equals(item.getText());
            }
        };
    }
}
