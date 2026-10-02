package testF;
import javax.swing.*;

import java.awt.*;

public class SeatAssignmentSystem extends JFrame {
    private SeatManager seatManager = new SeatManager();
    private JPanel seatGridPanel;
    private JTextField idInput;
    private Admin loggedInAdmin = null;

    public SeatAssignmentSystem() {
        setTitle("Automated Seat Assignment System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 🔹 Student seat assigner at the TOP
        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel idLabel = new JLabel("Enter Student ID to assign/free seat:");
        idInput = new JTextField(15);
        JButton submitBtn = new JButton("Submit");
        topPanel.add(idLabel);
        topPanel.add(idInput);
        topPanel.add(submitBtn);

        // 🔹 Seat grid in the CENTER
        seatGridPanel = new JPanel(new GridLayout(4, 10, 10, 10));
        refreshSeatMap();

        // 🔹 Admin control buttons at the BOTTOM
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton registerAdminBtn = new JButton("Register Admin");
        JButton loginAdminBtn = new JButton("Login Admin");
        JButton logoutAdminBtn = new JButton("Logout Admin");
        bottomPanel.add(registerAdminBtn);
        bottomPanel.add(loginAdminBtn);
        bottomPanel.add(logoutAdminBtn);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(seatGridPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // 🔹 Event listeners
        registerAdminBtn.addActionListener(e -> registerAdmin());
        loginAdminBtn.addActionListener(e -> loginAdmin());
        logoutAdminBtn.addActionListener(e -> logoutAdmin());
        submitBtn.addActionListener(e -> toggleSeat());

        setVisible(true);
    }

    private void refreshSeatMap() {
        seatGridPanel.removeAll();
        for (int i = 1; i <= SeatManager.TOTAL_SEATS; i++) {
            JButton seatBtn = new JButton(String.valueOf(i));
            seatBtn.setEnabled(false);
            seatBtn.setBackground(seatManager.isSeatOccupied(i) ? Color.RED : Color.GREEN);
            seatGridPanel.add(seatBtn);
        }
        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    // Admin Registration
    private void registerAdmin() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField fField = new JTextField();
        JTextField lField = new JTextField();
        JTextField idField = new JTextField();
        JPasswordField passField = new JPasswordField();

        panel.add(new JLabel("First Name:"));
        panel.add(fField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lField);
        panel.add(new JLabel("5-digit Admin ID:"));
        panel.add(idField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Register Admin", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String f = fField.getText().trim();
        String l = lField.getText().trim();
        String id = idField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        if (f.isEmpty() || l.isEmpty() || id.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (!id.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this, "Admin ID must be exactly 5 digits!");
            return;
        }

        String msg = seatManager.registerAdmin(f, l, id, p);
        JOptionPane.showMessageDialog(this, msg);
    }

    // Admin Login
    private void loginAdmin() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField idField = new JTextField();
        JPasswordField passField = new JPasswordField();

        panel.add(new JLabel("5-digit Admin ID:"));
        panel.add(idField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Admin Login", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String id = idField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        if (!id.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this, "Admin ID must be exactly 5 digits!");
            return;
        }

        if (!seatManager.verifyAdminLogin(id, p)) {
            JOptionPane.showMessageDialog(this, "❌ Invalid credentials. Access denied!");
            return;
        }

        loggedInAdmin = seatManager.getAdmin(id);
        JOptionPane.showMessageDialog(this, "✅ Welcome, " + loggedInAdmin.firstName + "!");
        showAdminDashboard(loggedInAdmin);
    }

    private void logoutAdmin() {
        if (loggedInAdmin != null) {
            JOptionPane.showMessageDialog(this, "Admin " + loggedInAdmin.firstName + " logged out.");
            loggedInAdmin = null;
        } else {
            JOptionPane.showMessageDialog(this, "No admin is currently logged in.");
        }
    }

    private void toggleSeat() {
        String id = idInput.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter your Student ID!");
            return;
        }

        String msg = seatManager.toggleSeatForStudent(id);
        JOptionPane.showMessageDialog(this, msg);
        refreshSeatMap();
    }

    // 🔹 Updated Dashboard with Manual Seat Assignment
    private void showAdminDashboard(Admin admin) {
        JFrame dash = new JFrame("Admin Dashboard - " + admin.firstName);
        dash.setSize(500, 450);
        dash.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 1, 10, 10));
        JButton registerStudentBtn = new JButton("Register Student");
        JButton assignSeatBtn = new JButton("Assign Seat Manually");
        JButton totalBtn = new JButton("View total occupied seats");
        JButton occupiedBtn = new JButton("View occupied seat numbers");
        JButton refreshBtn = new JButton("Refresh seat map");
        JButton closeBtn = new JButton("Close");

        registerStudentBtn.addActionListener(e -> {
            JPanel regPanel = new JPanel(new GridLayout(3, 2, 10, 10));
            JTextField f = new JTextField();
            JTextField l = new JTextField();
            JTextField id = new JTextField();

            regPanel.add(new JLabel("Student First Name:"));
            regPanel.add(f);
            regPanel.add(new JLabel("Student Last Name:"));
            regPanel.add(l);
            regPanel.add(new JLabel("5-digit Student ID:"));
            regPanel.add(id);

            int result = JOptionPane.showConfirmDialog(dash, regPanel, "Register Student", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String fn = f.getText().trim();
            String ln = l.getText().trim();
            String sid = id.getText().trim();

            if (fn.isEmpty() || ln.isEmpty() || sid.isEmpty()) {
                JOptionPane.showMessageDialog(dash, "All fields are required!");
                return;
            }

            if (!sid.matches("\\d{5}")) {
                JOptionPane.showMessageDialog(dash, "Student ID must be exactly 5 digits!");
                return;
            }

            String msg = seatManager.registerStudent(fn, ln, sid);
            JOptionPane.showMessageDialog(dash, msg);
        });

        assignSeatBtn.addActionListener(e -> {
            JPanel assignPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            JTextField studentIdField = new JTextField();
            JTextField seatNumField = new JTextField();

            assignPanel.add(new JLabel("Student ID:"));
            assignPanel.add(studentIdField);
            assignPanel.add(new JLabel("Seat Number (1–" + SeatManager.TOTAL_SEATS + "):"));
            assignPanel.add(seatNumField);

            int result = JOptionPane.showConfirmDialog(dash, assignPanel, "Assign Seat Manually", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String sid = studentIdField.getText().trim();
            String seatStr = seatNumField.getText().trim();

            if (!sid.matches("\\d{5}")) {
                JOptionPane.showMessageDialog(dash, "Invalid student ID format.");
                return;
            }
            if (!seatStr.matches("\\d+")) {
                JOptionPane.showMessageDialog(dash, "Seat number must be a number!");
                return;
            }

            int seat = Integer.parseInt(seatStr);
            String msg = seatManager.assignSeatManually(sid, seat);
            JOptionPane.showMessageDialog(dash, msg);
            refreshSeatMap();
        });

        totalBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(dash, "Total occupied seats: " + seatManager.getOccupiedCount()));

        occupiedBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(dash, "Occupied seat numbers: " + seatManager.getOccupiedSeats()));

        refreshBtn.addActionListener(e -> {
            refreshSeatMap();
            JOptionPane.showMessageDialog(dash, "Seat map refreshed!");
        });

        closeBtn.addActionListener(e -> dash.dispose());

        panel.add(registerStudentBtn);
        panel.add(assignSeatBtn);
        panel.add(totalBtn);
        panel.add(occupiedBtn);
        panel.add(refreshBtn);
        panel.add(closeBtn);
        dash.add(panel);
        dash.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SeatAssignmentSystem::new);
    }
}
