package ui;

import dao.BorrowRecordDAO;
import model.BorrowRecord;
import util.InputValidator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * BorrowPanel – Swing GUI panel for managing borrowing records.
 * Includes overdue highlighting, date range filtering, and status filtering.
 */
public class BorrowPanel extends JPanel {

    private final BorrowRecordDAO dao = new BorrowRecordDAO();
    private DefaultTableModel tableModel;
    private JTable table;

    // Toolbar
    private JTextField tfMemberId, tfBookId, tfFromDate, tfToDate;
    private JComboBox<String> cbStatusFilter;

    // Form
    private JTextField tfFormBookId, tfFormMemberId, tfFormBorrowDate, tfFormDueDate;
    private JComboBox<String> cbReturnStatus;
    private JLabel lblRecordId;
    private int selectedRecordId = -1;

    public BorrowPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);
        loadAll();
    }

    // ── Toolbar ────────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));

        tfMemberId = new JTextField(5); tfBookId = new JTextField(5);
        tfFromDate = new JTextField("yyyy-MM-dd", 10); tfToDate = new JTextField("yyyy-MM-dd", 10);
        cbStatusFilter = new JComboBox<>(new String[]{"All", "Borrowed", "Returned", "Overdue"});

        JButton btnByMember = new JButton("By Member");
        JButton btnByBook   = new JButton("By Book");
        JButton btnOverdue  = new JButton("⚠ Show Overdue");
        JButton btnDateRange = new JButton("Date Range");
        JButton btnAll      = new JButton("↺ All Records");

        btnByMember.addActionListener(e -> filterByMember());
        btnByBook.addActionListener(e -> filterByBook());
        btnOverdue.addActionListener(e -> showOverdue());
        btnDateRange.addActionListener(e -> filterByDateRange());
        btnAll.addActionListener(e -> loadAll());
        cbStatusFilter.addActionListener(e -> filterByStatus());

        bar.add(new JLabel("Member ID:")); bar.add(tfMemberId); bar.add(btnByMember);
        bar.add(new JLabel("Book ID:")); bar.add(tfBookId); bar.add(btnByBook);
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(new JLabel("Status:")); bar.add(cbStatusFilter);
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(new JLabel("From:")); bar.add(tfFromDate);
        bar.add(new JLabel("To:")); bar.add(tfToDate); bar.add(btnDateRange);
        bar.add(btnOverdue); bar.add(btnAll);
        return bar;
    }

    // ── Table ──────────────────────────────────────────────────────────────────
    private JScrollPane buildTablePanel() {
        String[] cols = {"ID", "Book", "Member", "Borrow Date", "Due Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);

        int[] widths = {50, 220, 160, 100, 100, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Row colour: red = overdue, green = returned
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    String status = (String) t.getModel().getValueAt(row, 5);
                    String dueStr = (String) t.getModel().getValueAt(row, 4);
                    boolean overdue = !"Returned".equals(status) &&
                            LocalDate.now().isAfter(LocalDate.parse(dueStr));
                    if (overdue) c.setBackground(new Color(255, 180, 180));
                    else if ("Returned".equals(status)) c.setBackground(new Color(200, 240, 200));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });
        return new JScrollPane(table);
    }

    // ── Form ───────────────────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Borrow Record Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        lblRecordId = new JLabel("New record");
        tfFormBookId    = new JTextField(6);
        tfFormMemberId  = new JTextField(6);
        tfFormBorrowDate = new JTextField(LocalDate.now().toString(), 11);
        tfFormDueDate    = new JTextField(LocalDate.now().plusDays(14).toString(), 11);
        cbReturnStatus  = new JComboBox<>(new String[]{"Borrowed", "Returned", "Overdue"});

        Object[][] rows = {
            {"Book ID:", tfFormBookId, "Member ID:", tfFormMemberId},
            {"Borrow Date (yyyy-MM-dd):", tfFormBorrowDate, "Due Date (yyyy-MM-dd):", tfFormDueDate},
            {"Return Status:", cbReturnStatus, null, null}
        };

        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < 4; c++) {
                if (rows[r][c] == null) continue;
                gbc.gridx = c; gbc.gridy = r;
                form.add(rows[r][c] instanceof String ? new JLabel((String) rows[r][c]) : (Component) rows[r][c], gbc);
            }
        }

        JButton btnAdd    = new JButton("➕ Add Record");
        JButton btnUpdate = new JButton("✏ Update");
        JButton btnDelete = new JButton("🗑 Delete");
        JButton btnClear  = new JButton("Clear");

        btnAdd.addActionListener(e -> addRecord());
        btnUpdate.addActionListener(e -> updateRecord());
        btnDelete.addActionListener(e -> deleteRecord());
        btnClear.addActionListener(e -> clearForm());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(lblRecordId); btns.add(btnClear);
        btns.add(btnAdd); btns.add(btnUpdate); btns.add(btnDelete);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        form.add(btns, gbc);
        return form;
    }

    // ── Data loading ───────────────────────────────────────────────────────────
    private void loadAll() {
        cbStatusFilter.setSelectedItem("All");
        load(dao.findAll());
    }

    private void filterByMember() {
        String v = tfMemberId.getText().trim();
        if (!InputValidator.isPositiveInteger(v)) { showError("Enter a valid Member ID."); return; }
        load(dao.findByMember(Integer.parseInt(v)));
    }

    private void filterByBook() {
        String v = tfBookId.getText().trim();
        if (!InputValidator.isPositiveInteger(v)) { showError("Enter a valid Book ID."); return; }
        load(dao.findByBook(Integer.parseInt(v)));
    }

    private void showOverdue() {
        cbStatusFilter.setSelectedItem("All");
        load(dao.findOverdue());
    }

    private void filterByDateRange() {
        String from = tfFromDate.getText().trim();
        String to   = tfToDate.getText().trim();
        if (!InputValidator.isValidDate(from) || !InputValidator.isValidDate(to)) {
            showError("Enter valid dates in yyyy-MM-dd format."); return;
        }
        load(dao.findByDateRange(from, to));
    }

    private void filterByStatus() {
        String sel = (String) cbStatusFilter.getSelectedItem();
        if ("All".equals(sel)) { loadAll(); return; }
        load(dao.findByStatus(sel));
    }

    private void load(List<BorrowRecord> records) {
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override protected List<BorrowRecord> doInBackground() { return records; }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { showError("Error: " + e.getMessage()); }
            }
        }.execute();
    }

    private void populateTable(List<BorrowRecord> records) {
        tableModel.setRowCount(0);
        for (BorrowRecord r : records) {
            tableModel.addRow(new Object[]{
                r.getRecordId(), r.getBookTitle(), r.getMemberName(),
                r.getBorrowDate(), r.getDueDate(), r.getReturnStatus()
            });
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedRecordId = (int) tableModel.getValueAt(row, 0);
        // We need book_id and member_id – look up the full record
        BorrowRecord rec = dao.findById(selectedRecordId);
        if (rec == null) return;
        tfFormBookId.setText(String.valueOf(rec.getBookId()));
        tfFormMemberId.setText(String.valueOf(rec.getMemberId()));
        tfFormBorrowDate.setText(rec.getBorrowDate());
        tfFormDueDate.setText(rec.getDueDate());
        cbReturnStatus.setSelectedItem(rec.getReturnStatus());
        lblRecordId.setText("ID: " + selectedRecordId);
    }

    // ── CRUD ───────────────────────────────────────────────────────────────────
    private void addRecord() {
        if (!validateForm()) return;
        BorrowRecord rec = buildRecord(0);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return dao.create(rec); }
            @Override protected void done() {
                try {
                    if (get()) { info("Borrow record created successfully."); clearForm(); loadAll(); }
                    else showError("Failed to create record.");
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private void updateRecord() {
        if (selectedRecordId < 0) { info("Select a record to update."); return; }
        if (!validateForm()) return;
        BorrowRecord rec = buildRecord(selectedRecordId);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return dao.update(rec); }
            @Override protected void done() {
                try {
                    if (get()) { info("Borrow record updated successfully."); clearForm(); loadAll(); }
                    else showError("Update failed.");
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private void deleteRecord() {
        if (selectedRecordId < 0) { info("Select a record to delete."); return; }
        if (JOptionPane.showConfirmDialog(this,
                "Delete record ID " + selectedRecordId + "?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        final int id = selectedRecordId;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return dao.delete(id); }
            @Override protected void done() {
                try {
                    if (get()) { info("Record deleted."); clearForm(); loadAll(); }
                    else showError("Delete failed.");
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private BorrowRecord buildRecord(int id) {
        return new BorrowRecord(id,
            Integer.parseInt(tfFormBookId.getText().trim()),
            Integer.parseInt(tfFormMemberId.getText().trim()),
            tfFormBorrowDate.getText().trim(),
            tfFormDueDate.getText().trim(),
            (String) cbReturnStatus.getSelectedItem());
    }

    private boolean validateForm() {
        if (!InputValidator.isPositiveInteger(tfFormBookId.getText())) { showError("Book ID must be a positive integer."); return false; }
        if (!InputValidator.isPositiveInteger(tfFormMemberId.getText())) { showError("Member ID must be a positive integer."); return false; }
        if (!InputValidator.isValidDate(tfFormBorrowDate.getText())) { showError("Borrow date must be in yyyy-MM-dd format."); return false; }
        if (!InputValidator.isValidDate(tfFormDueDate.getText())) { showError("Due date must be in yyyy-MM-dd format."); return false; }
        if (!InputValidator.isDueDateAfterBorrowDate(tfFormBorrowDate.getText(), tfFormDueDate.getText())) {
            showError("Due date must be after the borrow date."); return false;
        }
        return true;
    }

    private void clearForm() {
        selectedRecordId = -1;
        tfFormBookId.setText(""); tfFormMemberId.setText("");
        tfFormBorrowDate.setText(LocalDate.now().toString());
        tfFormDueDate.setText(LocalDate.now().plusDays(14).toString());
        cbReturnStatus.setSelectedIndex(0);
        lblRecordId.setText("New record");
        table.clearSelection();
    }

    private void info(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}
