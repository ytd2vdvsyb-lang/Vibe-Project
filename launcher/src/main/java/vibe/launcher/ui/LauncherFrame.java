package vibe.launcher.ui;

import vibe.launcher.core.Account;
import vibe.launcher.core.GameDirs;
import vibe.launcher.core.GameLaunch;
import vibe.launcher.core.MicrosoftAuth;
import vibe.launcher.core.Progress;
import vibe.launcher.core.Settings;
import vibe.launcher.core.Versions;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public final class LauncherFrame extends JFrame {
    private static final int LOG_LIMIT = 200_000;

    private final GameDirs dirs;
    private final Settings settings;

    private final JTextField nameField = Theme.style(new JTextField());
    private final JComboBox<VersionItem> versionBox = Theme.style(new JComboBox<>());
    private final JSlider memorySlider;
    private final JLabel memoryLabel = label("", Theme.MUTED, 13);
    private final JCheckBox closeOnLaunch = new JCheckBox("Закрыть лаунчер после запуска");
    private final GradientButton playButton = new GradientButton("ИГРАТЬ");
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel statusLabel = label(" ", Theme.MUTED, 13);
    private final JTextArea logArea = new JTextArea();
    private final JButton microsoftButton = linkButton("");

    public LauncherFrame(GameDirs dirs) {
        super("Vibe Client");
        this.dirs = dirs;
        this.settings = Settings.load(dirs);

        int maxGb = Math.max(2, (int) (totalMemoryMb() / 1024) - 1);
        memorySlider = new JSlider(1, Math.min(32, maxGb), Math.max(1, Math.min(maxGb, settings.memoryMb / 1024)));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(880, 540));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);
        HeroPanel hero = new HeroPanel();
        hero.setPreferredSize(new Dimension(440, 540));
        root.add(hero, BorderLayout.CENTER);
        root.add(buildForm(), BorderLayout.EAST);
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);

        loadVersions();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE);
        form.setPreferredSize(new Dimension(420, 540));
        form.setBorder(BorderFactory.createEmptyBorder(40, 40, 32, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.insets = new Insets(0, 0, 24, 0);
        form.add(label("Вход", Theme.TEXT, 22, Font.BOLD), c);

        c.insets = new Insets(0, 0, 6, 0);
        form.add(label("Ник", Theme.MUTED, 13), c);
        nameField.setText(settings.username);
        nameField.setBorder(Theme.fieldBorder());
        nameField.setCaretColor(Theme.TEXT);
        c.insets = new Insets(0, 0, 2, 0);
        form.add(nameField, c);
        microsoftButton.addActionListener(e -> {
            if (settings.microsoftRefreshToken.isBlank()) loginMicrosoft(); else logoutMicrosoft();
        });
        c.insets = new Insets(0, -14, 10, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        form.add(microsoftButton, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        updateAccountUi();

        c.insets = new Insets(0, 0, 6, 0);
        form.add(label("Версия Minecraft", Theme.MUTED, 13), c);
        versionBox.addItem(new VersionItem(Settings.LATEST, "Загружаю список..."));
        versionBox.setPreferredSize(new Dimension(0, nameField.getPreferredSize().height));
        c.insets = new Insets(0, 0, 18, 0);
        form.add(versionBox, c);

        JPanel memoryRow = new JPanel(new BorderLayout());
        memoryRow.setOpaque(false);
        memoryRow.add(label("Память", Theme.MUTED, 13), BorderLayout.WEST);
        memoryRow.add(memoryLabel, BorderLayout.EAST);
        c.insets = new Insets(0, 0, 6, 0);
        form.add(memoryRow, c);
        memorySlider.setOpaque(false);
        memorySlider.addChangeListener(e -> updateMemoryLabel());
        updateMemoryLabel();
        c.insets = new Insets(0, 0, 12, 0);
        form.add(memorySlider, c);

        closeOnLaunch.setSelected(settings.closeOnLaunch);
        closeOnLaunch.setOpaque(false);
        closeOnLaunch.setForeground(Theme.MUTED);
        closeOnLaunch.setFont(Theme.font(Font.PLAIN, 13));
        c.insets = new Insets(0, 0, 22, 0);
        form.add(closeOnLaunch, c);

        playButton.setPreferredSize(new Dimension(0, 52));
        playButton.addActionListener(e -> play());
        c.insets = new Insets(0, 0, 14, 0);
        form.add(playButton, c);

        progressBar.setPreferredSize(new Dimension(0, 4));
        progressBar.setBorderPainted(false);
        progressBar.setBackground(Theme.FIELD);
        progressBar.setForeground(Theme.ACCENT);
        progressBar.setVisible(false);
        c.insets = new Insets(0, 0, 8, 0);
        form.add(progressBar, c);
        form.add(statusLabel, c);

        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        form.add(new JPanel() {{ setOpaque(false); }}, c);
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        JButton logButton = linkButton("Открыть лог");
        logButton.addActionListener(e -> showLog());
        c.insets = new Insets(0, -14, 0, 0);
        form.add(logButton, c);
        return form;
    }

    private void loadVersions() {
        new SwingWorker<List<VersionItem>, Void>() {
            @Override
            protected List<VersionItem> doInBackground() throws Exception {
                Versions versions = new Versions(dirs);
                List<VersionItem> items = new ArrayList<>();
                items.add(new VersionItem(Settings.LATEST, "Последняя (" + versions.latestRelease() + ")"));
                for (Versions.Release release : versions.releases()) items.add(new VersionItem(release.id(), release.id()));
                return items;
            }

            @Override
            protected void done() {
                List<VersionItem> items;
                try {
                    items = get();
                } catch (Exception e) {
                    items = List.of(new VersionItem(Settings.LATEST, "Последняя"));
                    setStatus("Нет связи с серверами Mojang: " + rootMessage(e), true);
                }
                versionBox.setModel(new DefaultComboBoxModel<>(items.toArray(new VersionItem[0])));
                for (VersionItem item : items) {
                    if (item.id.equals(settings.minecraftVersion)) versionBox.setSelectedItem(item);
                }
            }
        }.execute();
    }

    private void play() {
        boolean microsoft = !settings.microsoftRefreshToken.isBlank();
        String name = nameField.getText().trim();
        if (!microsoft && !Account.isValidName(name)) {
            setStatus("Ник: 3-16 символов, латиница, цифры и _", true);
            return;
        }
        if (!microsoft) settings.username = name;
        settings.memoryMb = memorySlider.getValue() * 1024;
        settings.closeOnLaunch = closeOnLaunch.isSelected();
        VersionItem selected = (VersionItem) versionBox.getSelectedItem();
        settings.minecraftVersion = selected == null ? Settings.LATEST : selected.id;
        try {
            settings.save(dirs);
        } catch (Exception e) {
            appendLog("Не удалось сохранить настройки: " + e);
        }

        playButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        boolean detach = settings.closeOnLaunch;

        new SwingWorker<Process, Void>() {
            @Override
            protected Process doInBackground() throws Exception {
                Progress progress = uiProgress();
                GameLaunch launch = new GameLaunch(dirs, settings, progress);
                if (microsoft) {
                    progress.status("Вход в аккаунт Microsoft");
                    MicrosoftAuth.Session session = new MicrosoftAuth(settings.effectiveMsClientId())
                            .refresh(settings.microsoftRefreshToken);
                    settings.microsoftRefreshToken = session.refreshToken();
                    settings.microsoftName = session.account().name();
                    settings.save(dirs);
                    launch.account(session.account());
                }
                return launch.start(detach);
            }

            @Override
            protected void done() {
                try {
                    Process process = get();
                    if (detach) {
                        dispose();
                        System.exit(0);
                    }
                    setStatus("Игра запущена", false);
                    progressBar.setVisible(false);
                    process.onExit().thenRun(() -> SwingUtilities.invokeLater(() -> {
                        playButton.setEnabled(true);
                        int code = process.exitValue();
                        setStatus(code == 0 ? "Игра закрыта" : "Игра упала (код " + code + "), см. лог", code != 0);
                    }));
                } catch (Exception e) {
                    playButton.setEnabled(true);
                    progressBar.setVisible(false);
                    setStatus("Ошибка: " + rootMessage(e), true);
                    appendLog("ОШИБКА: " + e);
                }
            }
        }.execute();
    }

    private void loginMicrosoft() {
        String clientId = settings.effectiveMsClientId();
        if (clientId.isBlank()) {
            setStatus("Вход через Microsoft ещё не настроен (нет client id)", true);
            return;
        }
        microsoftButton.setEnabled(false);
        JDialog dialog = new JDialog(this, "Вход через Microsoft", false);
        JLabel text = label("Получаю код...", Theme.TEXT, 14);
        text.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        dialog.setContentPane(text);
        dialog.setSize(460, 170);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        new SwingWorker<MicrosoftAuth.Session, Void>() {
            @Override
            protected MicrosoftAuth.Session doInBackground() throws Exception {
                return new MicrosoftAuth(clientId).login(code -> SwingUtilities.invokeLater(() -> {
                    text.setText("<html>Открой <b>" + code.verificationUri() + "</b><br>и введи код <b style='font-size:16px'>"
                            + code.userCode() + "</b><br><br>Код уже скопирован, страница откроется сама.</html>");
                    copyToClipboard(code.userCode());
                    openBrowser(code.verificationUri());
                }));
            }

            @Override
            protected void done() {
                dialog.dispose();
                microsoftButton.setEnabled(true);
                try {
                    MicrosoftAuth.Session session = get();
                    settings.microsoftRefreshToken = session.refreshToken();
                    settings.microsoftName = session.account().name();
                    settings.save(dirs);
                    setStatus("Вошёл как " + session.account().name(), false);
                } catch (Exception e) {
                    setStatus("Не удалось войти: " + rootMessage(e), true);
                    appendLog("ОШИБКА входа: " + e);
                }
                updateAccountUi();
            }
        }.execute();
    }

    private void logoutMicrosoft() {
        settings.microsoftRefreshToken = "";
        settings.microsoftName = "";
        try {
            settings.save(dirs);
        } catch (Exception e) {
            appendLog("Не удалось сохранить настройки: " + e);
        }
        updateAccountUi();
    }

    private void updateAccountUi() {
        boolean microsoft = !settings.microsoftRefreshToken.isBlank();
        nameField.setEnabled(!microsoft);
        nameField.setText(microsoft ? settings.microsoftName : settings.username);
        microsoftButton.setText(microsoft ? "Выйти из аккаунта Microsoft" : "Войти через Microsoft (лицензия)");
    }

    private static void copyToClipboard(String text) {
        try {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(text), null);
        } catch (RuntimeException ignored) {
            // Буфер обмена недоступен, код всё равно виден в окне.
        }
    }

    private static void openBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception ignored) {
            // Ссылка видна в окне, игрок откроет её сам.
        }
    }

    private Progress uiProgress() {
        return new Progress() {
            @Override
            public void status(String text) {
                SwingUtilities.invokeLater(() -> {
                    setStatus(text, false);
                    progressBar.setIndeterminate(true);
                });
                appendLog("== " + text);
            }

            @Override
            public void progress(long done, long total) {
                SwingUtilities.invokeLater(() -> {
                    if (total <= 0) return;
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum((int) total);
                    progressBar.setValue((int) done);
                });
            }

            @Override
            public void log(String line) {
                appendLog(line);
            }
        };
    }

    private void appendLog(String line) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(line + "\n");
            int extra = logArea.getDocument().getLength() - LOG_LIMIT;
            if (extra > 0) logArea.replaceRange("", 0, extra);
        });
    }

    private void showLog() {
        JDialog dialog = new JDialog(this, "Лог Vibe Client");
        // Лог самой игры пишется в файл, даже если лаунчер закрывался: показываем его, а под ним вывод лаунчера.
        JTextArea area = new JTextArea(gameLog() + "\n== Лаунчер ==\n" + logArea.getText());
        area.setEditable(false);
        area.setBackground(Theme.BACKGROUND);
        area.setForeground(Theme.TEXT);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setCaretPosition(area.getDocument().getLength());
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dialog.setContentPane(scroll);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Конец logs/latest.log игры или пояснение, если игру ещё не запускали. */
    private String gameLog() {
        java.nio.file.Path file = dirs.game.resolve("logs").resolve("latest.log");
        try {
            String text = java.nio.file.Files.readString(file, java.nio.charset.StandardCharsets.UTF_8);
            return "== Игра: " + file + " ==\n" + (text.length() > LOG_LIMIT ? text.substring(text.length() - LOG_LIMIT) : text);
        } catch (java.io.IOException | java.io.UncheckedIOException e) {
            return "== Игра ещё не запускалась (нет " + file + ") ==";
        }
    }

    private void setStatus(String text, boolean error) {
        statusLabel.setText(text);
        statusLabel.setForeground(error ? Theme.ERROR : Theme.MUTED);
    }

    private void updateMemoryLabel() {
        memoryLabel.setText(memorySlider.getValue() + " ГБ");
    }

    private static long totalMemoryMb() {
        try {
            var os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return os.getTotalMemorySize() / (1024 * 1024);
        } catch (RuntimeException | LinkageError e) {
            return 8192;
        }
    }

    private static String rootMessage(Throwable e) {
        while (e.getCause() != null) e = e.getCause();
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }

    private static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(Theme.MUTED);
        button.setFont(Theme.font(Font.PLAIN, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JLabel label(String text, java.awt.Color color, int size) {
        return label(text, color, size, Font.PLAIN);
    }

    private static JLabel label(String text, java.awt.Color color, int size, int style) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(Theme.font(style, size));
        return label;
    }

    private record VersionItem(String id, String title) {
        @Override
        public String toString() {
            return title;
        }
    }
}
