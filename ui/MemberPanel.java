package ui;

import dao.MemberDAO;
import model.Member;
import util.InputValidator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * MemberPanel – Swing GUI panel for managing library members.
 */
public class MemberPanel extends JPanel {

    private final MemberDAO memberDAO = new MemberDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JComboBox<String> sortColumn;
    private JToggleButton sortDir;

    // Form fields
    private JTextField tfName, tfEmail;
    private JComboBox<String> cbType;
    private JLabel lblId;
    private int selectedId = -1;

    public MemberPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);
        loadData();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchField = new JTextField(18);
        JButton btnSearch = new JButton("🔍 Search");
        JButton btnClear = new JButton("Clear");
        typeFilter = new JComboBox<>(new String[]{"All", "Student", "Staff"});
        sortColumn = new JComboBox<>(new String[]{"member_id", "member_name", "email", "membership_type"});
        sortDir = new JToggleButton("▲ ASC");
        sortDir.addActionListener(e -> sortDir.setText(sortDir.isSelected() ? "▼ DESC" : "▲ ASC"));
        JButton btnRefresh = new JButton("↺ Refresh");

        btnSearch.addActionListener(e -> loadData());
        btnClear.addActionListener(e -> { searchField.setText(""); loadData(); });
        typeFilter.addActionListener(e -> loadData());
        sortColumn.addActionListener(e -> loadData());
        sortDir.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> loadData());

        bar.add(new JLabel("Search:")); bar.add(searchField);
        bar.add(btnSearch); bar.add(btnClear);
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(new JLabel("Type:")); bar.add(typeFilter);
        bar.add(new JLabel("Sort:")); bar.add(sortColumn); bar.add(sortDir);
        bar.add(btnRefresh);
        return bar;
    }

    private JScrollPane buildTablePanel() {
        String[] cols = {"ID", "Name", "Email", "Membership Type"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);

        int[] widths = {50, 200, 280, 130};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });
        return new JScrollPane(table);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Member Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        lblId  = new JLabel("New member");
        tfName = new JTextField(22);
        tfEmail = new JTextField(25);
        cbType = new JComboBox<>(new String[]{"Student", "Staff"});

        int col = 0;
        gbc.gridx = col; gbc.gridy = 0; form.add(new JLabel("Name:"), gbc);
        gbc.gridx = col + 1; form.add(tfName, gbc);
        gbc.gridx = col + 2; form.add(new JLabel("Email:"), gbc);
        gbc.gridx = col + 3; form.add(tfEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; form.add(cbType, gbc);

        JButton btnAdd    = new JButton("➕ Add Member");
        JButton btnUpdate = new JButton("✏ Update");
        JButton btnDelete = new JButton("🗑 Delete");
        JButton btnClearF = new JButton("Clear Form");

        btnAdd.addActionListener(e -> addMember());
        btnUpdate.addActionListener(e -> updateMember());
        btnDelete.addActionListener(e -> deleteMember());
        btnClearF.addActionListener(e -> clearForm());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(lblId); btns.add(btnClearF);
        btns.add(btnAdd); btns.add(btnUpdate); btns.add(btnDelete);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        form.add(btns, gbc);
        return form;
    }

    private void loadData() {
        new SwingWorker<List<Member>, Void>() {
            @Override protected List<Member> doInBackground() {
                String s = searchField.getText().trim();
                String type = (String) typeFilter.getSelectedItem();
                String sort = (String) sortColumn.getSelectedItem();
                boolean asc = !sortDir.isSelected();
                if (!s.isEmpty()) return memberDAO.search(s);
                if (!"All".equals(type)) return memberDAO.filterByType(type);
                return memberDAO.findAll(sort, asc);
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { showError("Error loading members: " + e.getMessage()); }
            }
        }.execute();
    }

    private void populateTable(List<Member> members) {
        tableModel.setRowCount(0);
        for (Member m : members)
            tableModel.addRow(new Object[]{m.getMemberId(), m.getMemberName(), m.getEmail(), m.getMembershipType()});
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        tfName.setText((String) tableModel.getValueAt(row, 1));
        tfEmail.setText((String) tableModel.getValueAt(row, 2));
        cbType.setSelectedItem(tableModel.getValueAt(row, 3));
        lblId.setText("ID: " + selectedId);
    }

    private void addMember() {
        if (!validateForm()) return;
        Member m = new Member(0, tfName.getText().trim(), tfEmail.getText().trim(), (String) cbType.getSelectedItem());
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return memberDAO.create(m); }
            @Override protected void done() {
                try {
                    if (get()) { info("Member added successfully."); clearForm(); loadData(); }
                    else showError("Failed to add member.");
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private void updateMember() {
        if (selectedId < 0) { info("Select a member to update."); return; }
        if (!validateForm()) return;
        Member m = new Member(selectedId, tfName.getText().trim(), tfEmail.getText().trim(), (String) cbType.getSelectedItem());
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return memberDAO.update(m); }
            @Override protected void done() {
                try {
                    if (get()) { info("Member updated successfully."); clearForm(); loadData(); }
                    else showError("Update failed.");
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private void deleteMember() {
        if (selectedId < 0) { info("Select a member to delete."); return; }
        if (JOptionPane.showConfirmDialog(this,
                "Delete member ID " + selectedId + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        final int id = selectedId;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return memberDAO.delete(id); }
            @Override protected void done() {
                try {
                    if (get()) { info("Member deleted."); clearForm(); loadData(); }
                    else showError("Delete failed.");
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private boolean validateForm() {
        if (!InputValidator.isNotBlank(tfName.getText())) { showError("Name must not be empty."); return false; }
        if (!InputValidator.isValidEmail(tfEmail.getText())) { showError("Please enter a valid email address."); return false; }
        return true;
    }

    private void clearForm() {
        selectedId = -1; tfName.setText(""); tfEmail.setText("");
        cbType.setSelectedIndex(0); lblId.setText("New member"); table.clearSelection();
    }

    private void info(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}
