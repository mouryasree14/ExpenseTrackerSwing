import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ExpenseTrackerSwing extends JFrame {

    // Database details
    static final String URL = "jdbc:mysql://localhost:3306/personal_tracker?useSSL=false&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASSWORD = "root1014";

    // UI Components
    private JTextField txtId, txtAmount, txtCategory, txtDate, txtDesc;
    private JTable table;
    private DefaultTableModel model;

    public ExpenseTrackerSwing() {
        setTitle("Personal Expense Tracker (Swing + JDBC)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Panel: Form
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Expense ID (for Update/Delete):"));
        txtId = new JTextField();
        formPanel.add(txtId);

        formPanel.add(new JLabel("Amount:"));
        txtAmount = new JTextField();
        formPanel.add(txtAmount);

        formPanel.add(new JLabel("Category:"));
        txtCategory = new JTextField();
        formPanel.add(txtCategory);

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        txtDate = new JTextField();
        formPanel.add(txtDate);

        formPanel.add(new JLabel("Description:"));
        txtDesc = new JTextField();
        formPanel.add(txtDesc);

        add(formPanel, BorderLayout.NORTH);

        // Center Panel: Table
        model = new DefaultTableModel(new String[]{"ID", "Amount", "Category", "Date", "Description"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Panel: Buttons
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Add Expense");
        JButton btnView = new JButton("View Expenses");
        JButton btnUpdate = new JButton("Update Expense");
        JButton btnDelete = new JButton("Delete Expense");
        JButton btnClear = new JButton("Clear Fields");

        btnPanel.add(btnAdd);
        btnPanel.add(btnView);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        add(btnPanel, BorderLayout.SOUTH);

        // Button Actions
        btnAdd.addActionListener(e -> insertExpense());
        btnView.addActionListener(e -> loadExpenses());
        btnUpdate.addActionListener(e -> updateExpense());
        btnDelete.addActionListener(e -> deleteExpense());
        btnClear.addActionListener(e -> clearFields());

        setVisible(true);
    }

    // DB Connection
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database Connection Error: " + e.getMessage());
            return null;
        }
    }

    // Insert Expense
    private void insertExpense() {
        try (Connection con = getConnection()) {
            String sql = "INSERT INTO expenses (amount, category, expense_date, description) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, Double.parseDouble(txtAmount.getText()));
            ps.setString(2, txtCategory.getText());
            ps.setDate(3, Date.valueOf(txtDate.getText()));
            ps.setString(4, txtDesc.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Expense Added Successfully!");
            loadExpenses();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Load Expenses
    private void loadExpenses() {
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM expenses")) {

            model.setRowCount(0); // clear table
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getDate("expense_date"),
                        rs.getString("description")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Loading Data: " + e.getMessage());
        }
    }

    // Update Expense
    private void updateExpense() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Enter Expense ID to Update");
            return;
        }
        try (Connection con = getConnection()) {
            String sql = "UPDATE expenses SET amount=?, category=?, expense_date=?, description=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, Double.parseDouble(txtAmount.getText()));
            ps.setString(2, txtCategory.getText());
            ps.setDate(3, Date.valueOf(txtDate.getText()));
            ps.setString(4, txtDesc.getText());
            ps.setInt(5, Integer.parseInt(txtId.getText()));
            int rows = ps.executeUpdate();
            if (rows > 0)
                JOptionPane.showMessageDialog(this, "✅ Expense Updated Successfully!");
            else
                JOptionPane.showMessageDialog(this, "⚠️ Expense ID Not Found!");
            loadExpenses();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Delete Expense
    private void deleteExpense() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Enter Expense ID to Delete");
            return;
        }
        try (Connection con = getConnection()) {
            String sql = "DELETE FROM expenses WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(txtId.getText()));
            int rows = ps.executeUpdate();
            if (rows > 0)
                JOptionPane.showMessageDialog(this, "🗑️ Expense Deleted Successfully!");
            else
                JOptionPane.showMessageDialog(this, "⚠️ Expense Not Found!");
            loadExpenses();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Clear Fields
    private void clearFields() {
        txtId.setText("");
        txtAmount.setText("");
        txtCategory.setText("");
        txtDate.setText("");
        txtDesc.setText("");
    }

    // Main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerSwing::new);
    }
}
