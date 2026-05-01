package ui;

import db.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * MainWindow – the primary application frame.
 * Hosts a JTabbedPane with Dashboard, Books, Members, and Borrowing tabs.
 *
 * OOP concepts: Encapsulation, Composition (contains child panels).
 */
public class MainWindow extends JFrame {

    private DashboardPanel dashboardPanel;

    public MainWindow() {
        setTitle("St Mary's Digital Library Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(MainWindow.this,
                    "Exit the Library System?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    DatabaseManager.getInstance().closeConnection();
                    System.exit(0);
                }
            }
        });

        setJMenuBar(buildMenuBar());
        setContentPane(buildContent());
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "St Mary's Digital Library Management System\n" +
            "CPS4005 – Object-Oriented Programming\n\n" +
            "Built with Java Swing + SQLite JDBC\n" +
            "© St Mary's University, Twickenham",
            "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        bar.add(fileMenu);
        bar.add(helpMenu);
        return bar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());

        // Header banner
        JLabel header = new JLabel("  📚 St Mary's University – Digital Library System", SwingConstants.LEFT);
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setOpaque(true);
        header.setBackground(new Color(30, 60, 120));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        content.add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        dashboardPanel = new DashboardPanel();
        tabs.addTab("🏠 Dashboard",  dashboardPanel);
        tabs.addTab("📖 Books",      new BookPanel());
        tabs.addTab("👤 Members",    new MemberPanel());
        tabs.addTab("📋 Borrowing",  new BorrowPanel());

        // Refresh dashboard stats whenever the user returns to it
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) dashboardPanel.refreshData();
        });

        content.add(tabs, BorderLayout.CENTER);

        // Status bar
        JLabel status = new JLabel("  Connected to library.db   |   Ready");
        status.setFont(new Font("Monospaced", Font.PLAIN, 11));
        status.setOpaque(true);
        status.setBackground(new Color(240, 240, 240));
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        status.setPreferredSize(new Dimension(0, 22));
        content.add(status, BorderLayout.SOUTH);

        return content;
    }
}
