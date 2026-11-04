package net.diaithir.crashtools.crashwindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.minecraft.CrashReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clean-room standalone crash window. Minecraft's own GUI is already gone when a crash is saved,
 * so this is a small Swing dialog (no GL needed) with the headline, the report path and three
 * buttons: open the folder, copy the summary, and close. It is skipped on headless machines and
 * can never throw into the crash path.
 */
public final class CrashWindow {
    private static final Logger LOG = LoggerFactory.getLogger("diaithir-crashwindow");
    private static boolean shown;

    private CrashWindow() {}

    public static void show(CrashReport report) {
        if (shown || Boolean.getBoolean("diaithir.crashwindow.disable") || GraphicsEnvironment.isHeadless()) return;
        shown = true;
        try {
            Path file = report.getSaveFile();
            String summary = summary(report, file);
            Runnable build = () -> build(summary, file);
            if (SwingUtilities.isEventDispatchThread()) build.run();
            else SwingUtilities.invokeAndWait(build);
        } catch (Throwable t) {
            LOG.warn("Could not show crash window", t);
        }
    }

    static String summary(CrashReport report, Path file) {
        Throwable e = report.getException();
        StringBuilder sb = new StringBuilder();
        sb.append("Minecraft crashed: ").append(report.getTitle()).append('\n');
        sb.append(e.getClass().getName());
        if (e.getMessage() != null) sb.append(": ").append(e.getMessage());
        sb.append("\n\n");
        StackTraceElement[] st = e.getStackTrace();
        for (int i = 0; i < Math.min(12, st.length); i++) sb.append("  at ").append(st[i]).append('\n');
        sb.append("\nReport: ").append(file == null ? "(not saved)" : file.toAbsolutePath()).append('\n');
        return sb.toString();
    }

    private static void build(String summary, Path file) {
        JDialog d = new JDialog((java.awt.Frame) null, "Diaithir - the game crashed", true);
        JTextArea text = new JTextArea(summary, 18, 80);
        text.setEditable(false);
        text.setCaretPosition(0);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton open = new JButton("Open crash folder");
        open.setEnabled(file != null && Desktop.isDesktopSupported());
        open.addActionListener(a -> {
            try {
                File dir = file.getParent().toFile();
                Desktop.getDesktop().open(dir);
            } catch (Throwable t) {
                LOG.warn("open folder failed", t);
            }
        });
        JButton copy = new JButton("Copy summary");
        copy.addActionListener(a ->
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(summary), null));
        JButton close = new JButton("Close");
        close.addActionListener(a -> d.dispose());
        buttons.add(open);
        buttons.add(copy);
        buttons.add(close);
        d.setLayout(new BorderLayout(8, 8));
        d.add(new JLabel("  Send the crash report file when asking for help."), BorderLayout.NORTH);
        d.add(new JScrollPane(text), BorderLayout.CENTER);
        d.add(buttons, BorderLayout.SOUTH);
        d.setMinimumSize(new Dimension(640, 360));
        d.pack();
        d.setLocationRelativeTo(null);
        d.setAlwaysOnTop(true);
        d.setVisible(true);
    }
}
