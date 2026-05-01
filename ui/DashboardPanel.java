package ui;

import dao.BookDAO;
import dao.BorrowRecordDAO;
import dao.MemberDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * DashboardPanel – summary view showing library statistics.
 * Loads data asynchronously using SwingWorker (Advanced: multi-threading).
 */
public class DashboardPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final BorrowRecordDAO borrowDAO = new BorrowRecordDAO();

    private JLabel lblTotalBooks, lblAvailable, lblBorrowed;
    private JLabel lblTotalMembers, lblStudents, lblStaff;
    private JLabel lblTotalRecords, lblOverdue, lblReturned;
    private JTextArea txtOverdueList;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("📚 St Mary's Digital Library – Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.add(buildBookCard());
        cards.add(buildMemberCard());
        cards.add(buildBorrowCard());
        add(cards, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setBorder(BorderFactory.createTitledBorder("⚠ Overdue Books"));
        txtOverdueList = new JTextArea(6, 0);
        txtOverdueList.setEditable(false);
        txtOverdueList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        south.add(new JScrollPane(txtOverdueList), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("↺ Refresh Dashboard");
        btnRefresh.addActionListener(e -> refreshData());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnRefresh);
        south.add(btnPanel, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel buildBookCard() {
        JPanel p = card("📖 Books");
        lblTotalBooks = stat(p, "Total Books:");
        lblAvailable  = stat(p, "Available:");
        lblBorrowed   = stat(p, "Borrowed:");
        return p;
    }

    private JPanel buildMemberCard() {
        JPanel p = card("👤 Members");
        lblTotalMembers = stat(p, "Total Members:");
        lblStudents     = stat(p, "Students:");
        lblStaff        = stat(p, "Staff:");
        return p;
    }

    private JPanel buildBorrowCard() {
        JPanel p = card("📋 Transactions");
        lblTotalRecords = stat(p, "Total Records:");
        lblOverdue      = stat(p, "Overdue:");
        lblReturned     = stat(p, "Returned:");
        return p;
    }

    private JPanel card(String heading) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 200), 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        p.setBackground(new Color(245, 247, 255));
        JLabel h = new JLabel(heading);
        h.setFont(new Font("SansSerif", Font.BOLD, 14));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(8));
        return p;
    }

    private JLabel stat(JPanel card, String label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JLabel val = new JLabel("...");
        val.setFont(new Font("SansSerif", Font.BOLD, 13));
        val.setForeground(new Color(30, 80, 180));
        row.add(lbl); row.add(val);
        card.add(row);
        return val;
    }

    public void refreshData() {
        new SwingWorker<int[], Void>() {
            private String overdueText = "";

            @Override
            protected int[] doInBackground() {
                int total = bookDAO.findAll().size();
                int avail = bookDAO.filterByStatus("Available").size();
                int borrow = bookDAO.filterByStatus("Borrowed").size();
                int members = memberDAO.findAll().size();
                int students = memberDAO.filterByType("Student").size();
                int staff = memberDAO.filterByType("Staff").size();
                int records = borrowDAO.findAll().size();
                var overdueList = borrowDAO.findOverdue();
                int overdue = overdueList.size();
                int returned = borrowDAO.findByStatus("Returned").size();

                StringBuilder sb = new StringBuilder();
                if (overdueList.isEmpty()) {
                    sb.append("No overdue records. ✓");
                } else {
                    for (var r : overdueList) {
                        sb.append(String.format("  Record #%d | Book: %-25s | Member: %-20s | Due: %s (%d days overdue)%n",
                            r.getRecordId(), r.getBookTitle(), r.getMemberName(), r.getDueDate(), r.daysOverdue()));
                    }
                }
                overdueText = sb.toString();
                return new int[]{total, avail, borrow, members, students, staff, records, overdue, returned};
            }

            @Override
            protected void done() {
                try {
                    int[] d = get();
                    lblTotalBooks.setText(String.valueOf(d[0]));
                    lblAvailable.setText(String.valueOf(d[1]));
                    lblBorrowed.setText(String.valueOf(d[2]));
                    lblTotalMembers.setText(String.valueOf(d[3]));
                    lblStudents.setText(String.valueOf(d[4]));
                    lblStaff.setText(String.valueOf(d[5]));
                    lblTotalRecords.setText(String.valueOf(d[6]));
                    lblOverdue.setText(String.valueOf(d[7]));
                    if (d[7] > 0) lblOverdue.setForeground(Color.RED);
                    lblReturned.setText(String.valueOf(d[8]));
                    txtOverdueList.setText(overdueText);
                } catch (Exception e) {
                    txtOverdueList.setText("Error loading data: " + e.getMessage());
                }
            }
        }.execute();
    }
}
