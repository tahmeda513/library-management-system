package ui;

import dao.BookDAO;
import model.Book;
import util.InputValidator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * BookPanel – Swing GUI panel for managing books.
 *
 * OOP concepts: Encapsulation (private fields), SwingWorker for multi-threading.
 * Follows single-responsibility: only handles the Book UI concern.
 */
public class BookPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> sortColumn;
    private JToggleButton sortDirection;

    // Form fields
    private JTextField tfTitle, tfAuthor, tfCategory;
    private JComboBox<String> cbStatus;
    private JLabel lblSelectedId;
    private int selectedBookId = -1;

    public BookPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);
        loadData();
    }

    // ── Toolbar (search + filter + sort) ──────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));

        searchField = new JTextField(18);
        searchField.setToolTipText("Search by title, author or category");
        JButton btnSearch = new JButton("🔍 Search");
        JButton btnClear = new JButton("Clear");

        statusFilter = new JComboBox<>(new String[]{"All", "Available", "Borrowed"});
        sortColumn = new JComboBox<>(new String[]{"book_id", "title", "author", "category", "availability_status"});
        sortDirection = new JToggleButton("▲ ASC");
        sortDirection.addActionListener(e -> sortDirection.setText(sortDirection.isSelected() ? "▼ DESC" : "▲ ASC"));

        JButton btnRefresh = new JButton("↺ Refresh");

        btnSearch.addActionListener(e -> performSearch());
        btnClear.addActionListener(e -> { searchField.setText(""); loadData(); });
        statusFilter.addActionListener(e -> loadData());
        sortColumn.addActionListener(e -> loadData());
        sortDirection.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> loadData());

        bar.add(new JLabel("Search:"));
        bar.add(searchField);
        bar.add(btnSearch);
        bar.add(btnClear);
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(new JLabel("Status:"));
        bar.add(statusFilter);
        bar.add(new JLabel("Sort:"));
        bar.add(sortColumn);
        bar.add(sortDirection);
        bar.add(btnRefresh);
        return bar;
    }

    // ── Table ──────────────────────────────────────────────────────────────────
    private JScrollPane buildTablePanel() {
        String[] cols = {"ID", "Title", "Author", "Category", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);

        // Column widths
        int[] widths = {50, 250, 160, 140, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Highlight Borrowed rows in light red
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    String status = (String) t.getModel().getValueAt(row, 4);
                    c.setBackground("Borrowed".equals(status)
                            ? new Color(255, 220, 220) : Color.WHITE);
                }
                return c;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromSelection();
        });

        return new JScrollPane(table);
    }

    // ── Form panel (Add / Update / Delete) ────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Book Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        lblSelectedId = new JLabel("New book");
        tfTitle  = new JTextField(22);
        tfAuthor = new JTextField(18);
        tfCategory = new JTextField(14);
        cbStatus = new JComboBox<>(new String[]{"Available", "Borrowed"});

        addRow(form, gbc, 0, "Title:", tfTitle);
        addRow(form, gbc, 1, "Author:", tfAuthor);
        addRow(form, gbc, 2, "Category:", tfCategory);
        addRow(form, gbc, 3, "Status:", cbStatus);

        // Buttons
        JButton btnAdd    = new JButton("➕ Add Book");
        JButton btnUpdate = new JButton("✏ Update");
        JButton btnDelete = new JButton("🗑 Delete");
        JButton btnClear  = new JButton("Clear Form");

        btnAdd.addActionListener(e -> addBook());
        btnUpdate.addActionListener(e -> updateBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnClear.addActionListener(e -> clearForm());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(lblSelectedId);
        btnPanel.add(btnClear);
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        form.add(btnPanel, gbc);
        return form;
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = (row % 2) * 2; gbc.gridy = row / 2;
        p.add(new JLabel(label), gbc);
        gbc.gridx++;
        p.add(field, gbc);
    }

    // ── Data loading (SwingWorker for multi-threading) ─────────────────────────
    private void loadData() {
        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() {
                String search = searchField.getText().trim();
                String status = (String) statusFilter.getSelectedItem();
                String sort = (String) sortColumn.getSelectedItem();
                boolean asc = !sortDirection.isSelected();

                if (!search.isEmpty()) return bookDAO.search(search);
                if (!"All".equals(status)) return bookDAO.filterByStatus(status);
                return bookDAO.findAll(sort, asc);
            }
            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BookPanel.this,
                        "Error loading books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void performSearch() {
        loadData();
    }

    private void populateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                b.getBookId(), b.getTitle(), b.getAuthor(), b.getCategory(), b.getAvailabilityStatus()
            });
        }
    }

    private void populateFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedBookId = (int) tableModel.getValueAt(row, 0);
        tfTitle.setText((String) tableModel.getValueAt(row, 1));
        tfAuthor.setText((String) tableModel.getValueAt(row, 2));
        tfCategory.setText((String) tableModel.getValueAt(row, 3));
        cbStatus.setSelectedItem(tableModel.getValueAt(row, 4));
        lblSelectedId.setText("ID: " + selectedBookId);
    }

    // ── CRUD actions ───────────────────────────────────────────────────────────
    private void addBook() {
        if (!validateForm()) return;
        Book book = new Book(0, tfTitle.getText().trim(), tfAuthor.getText().trim(),
                tfCategory.getText().trim(), (String) cbStatus.getSelectedItem());
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return bookDAO.create(book); }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BookPanel.this, "Book added successfully.");
                        clearForm(); loadData();
                    } else {
                        JOptionPane.showMessageDialog(BookPanel.this, "Failed to add book.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) { handleException(ex); }
            }
        }.execute();
    }

    private void updateBook() {
        if (selectedBookId < 0) { JOptionPane.showMessageDialog(this, "Select a book to update."); return; }
        if (!validateForm()) return;
        Book book = new Book(selectedBookId, tfTitle.getText().trim(), tfAuthor.getText().trim(),
                tfCategory.getText().trim(), (String) cbStatus.getSelectedItem());
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return bookDAO.update(book); }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BookPanel.this, "Book updated successfully.");
                        clearForm(); loadData();
                    } else {
                        JOptionPane.showMessageDialog(BookPanel.this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) { handleException(ex); }
            }
        }.execute();
    }

    private void deleteBook() {
        if (selectedBookId < 0) { JOptionPane.showMessageDialog(this, "Select a book to delete."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete book ID " + selectedBookId + "?\nThis cannot be undone.", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        final int id = selectedBookId;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return bookDAO.delete(id); }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BookPanel.this, "Book deleted successfully.");
                        clearForm(); loadData();
                    } else {
                        JOptionPane.showMessageDialog(BookPanel.this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) { handleException(ex); }
            }
        }.execute();
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private boolean validateForm() {
        if (!InputValidator.isNotBlank(tfTitle.getText())) {
            error("Book title must not be empty."); return false;
        }
        if (!InputValidator.isNotBlank(tfAuthor.getText())) {
            error("Author name must not be empty."); return false;
        }
        if (!InputValidator.isNotBlank(tfCategory.getText())) {
            error("Category must not be empty."); return false;
        }
        return true;
    }

    private void clearForm() {
        selectedBookId = -1;
        tfTitle.setText(""); tfAuthor.setText(""); tfCategory.setText("");
        cbStatus.setSelectedIndex(0);
        lblSelectedId.setText("New book");
        table.clearSelection();
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void handleException(Exception e) {
        JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
