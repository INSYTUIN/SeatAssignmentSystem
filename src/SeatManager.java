package src;

import java.sql.*;
import java.util.*;

class Admin {
    String firstName, lastName, adminId, password;
    Admin(String f, String l, String id, String p) {
        firstName = f; lastName = l; adminId = id; password = p;
    }
}

class Student {
    String firstName, lastName, studentId;
    int seat;
    Student(String f, String l, String id, int s) {
        firstName = f; lastName = l; studentId = id; seat = s;
    }
}

class SeatManager {
    public static final int TOTAL_SEATS = 40;
    private String studentTable = "students"; // default table

    // change table manually
    public void setStudentTable(String tableName) {
        if (tableName != null && tableName.matches("[a-zA-Z0-9_]+")) {
            this.studentTable = tableName;
        }
    }

    // retrieve current table
    public String getStudentTable() {
        return studentTable;
}
    // ✅ Register new student
    public String registerStudent(String first, String last, String id) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + studentTable + " WHERE student_id=?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return "❌ Student already registered.";

            ps = con.prepareStatement( "INSERT INTO " + studentTable + "(first_name,last_name,student_id) VALUES(?,?,?)");
            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, id);
            ps.executeUpdate();
            return "✅ Student registered successfully!";
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Database error: " + e.getMessage();
        }
    }

    // ✅ Toggle seat: assign or free
    public String toggleSeatForStudent(String id) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT seat_number FROM " + studentTable + " WHERE student_id=?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return "❌ Student not found.";

            int seat = rs.getInt("seat_number");
            if (rs.wasNull()) {
                int next = getNextAvailableSeat(con);
                if (next == -1) return "❌ No seats available!";
                ps = con.prepareStatement("UPDATE " + studentTable + " SET seat_number=? WHERE student_id=?");
                ps.setInt(1, next);
                ps.setString(2, id);
                ps.executeUpdate();
                return "✅ Assigned seat #" + next;
            } else {
                ps = con.prepareStatement("UPDATE " + studentTable + " SET seat_number=NULL WHERE student_id=?");
                ps.setString(1, id);
                ps.executeUpdate();
                return "✅ Seat #" + seat + " freed.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Database error: " + e.getMessage();
        }
    }

    private int getNextAvailableSeat(Connection con) throws SQLException {
        Set<Integer> occupied = new HashSet<>();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT seat_number FROM " + studentTable + " WHERE seat_number IS NOT NULL");
        while (rs.next()) occupied.add(rs.getInt(1));

        for (int i = 1; i <= TOTAL_SEATS; i++)
            if (!occupied.contains(i)) return i;
        return -1;
    }

    // ✅ Admin Registration
    public String registerAdmin(String f, String l, String id, String p) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM admins WHERE admin_id=?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return "❌ Admin already exists.";

            ps = con.prepareStatement("INSERT INTO admins(first_name,last_name,admin_id,password) VALUES(?,?,?,?)");
            ps.setString(1, f);
            ps.setString(2, l);
            ps.setString(3, id);
            ps.setString(4, p);
            ps.executeUpdate();
            return "✅ Admin registered successfully!";
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Database error: " + e.getMessage();
        }
    }

    // ✅ Verify admin login
    public boolean verifyAdminLogin(String id, String pass) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM admins WHERE admin_id=? AND password=?");
            ps.setString(1, id);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Get total occupied seats
    public int getOccupiedCount() {
        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + studentTable + " WHERE seat_number IS NOT NULL");
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ✅ Get list of occupied seats
    public String getOccupiedSeats() {
        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT seat_number FROM " + studentTable + " WHERE seat_number IS NOT NULL ORDER BY seat_number");
            StringBuilder sb = new StringBuilder();
            while (rs.next()) sb.append(rs.getInt(1)).append(" ");
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Error fetching seats.";
        }
    }

    // ✅ Assign seat manually (Admin)
    public String assignSeatManually(String studentId, int seat) {
        try (Connection con = DBConnection.getConnection()) {
            if (seat < 1 || seat > TOTAL_SEATS) return "❌ Invalid seat number.";
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + studentTable + " WHERE student_id=?");
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return "❌ Student not found.";

            ps = con.prepareStatement("SELECT * FROM " + studentTable + " WHERE seat_number=?");
            ps.setInt(1, seat);
            rs = ps.executeQuery();
            if (rs.next()) return "❌ Seat already occupied.";

            ps = con.prepareStatement("UPDATE " + studentTable + " SET seat_number=? WHERE student_id=?");
            ps.setInt(1, seat);
            ps.setString(2, studentId);
            ps.executeUpdate();
            return "✅ Seat #" + seat + " assigned successfully.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Database error: " + e.getMessage();
        }
    }

    // ✅ Reset all seats (Admin only)
    public String resetAllSeats() {
        try (Connection con = DBConnection.getConnection()) {
        Statement st = con.createStatement();
        int count = st.executeUpdate("UPDATE " + studentTable + " SET seat_number=NULL");
        return "✅ " + count + " seats have been reset and are now available.";
        } catch (SQLException e) {
        e.printStackTrace();
        return "❌ Database error while resetting seats: " + e.getMessage();
         }
    }   

    // ✅ Remove a student from the database
    public String removeStudent(String studentId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + studentTable + " WHERE student_id=?");
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return "❌ Student not found.";
            }

            ps = con.prepareStatement("DELETE FROM " + studentTable + " WHERE student_id=?");
            ps.setString(1, studentId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                return "✅ Student with ID " + studentId + " has been removed successfully.";
            }
            return "❌ Error: Could not remove student.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Database error while removing student: " + e.getMessage();
        }
    }

    // ✅ Check seat occupancy
    public boolean isSeatOccupied(int seat) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT seat_number FROM " + studentTable + " WHERE seat_number=?");
            ps.setInt(1, seat);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Return dummy admin object (for GUI)
    public Admin getAdmin(String id) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM admins WHERE admin_id=?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Admin(rs.getString("first_name"), rs.getString("last_name"),
                                 rs.getString("admin_id"), rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;}
    // ✅ Random seat assignment for all students without seats
    public String assignRandomSeats() {
    try (Connection con = DBConnection.getConnection()) {

        List<Integer> availableSeats = new ArrayList<>();
        List<String> studentsWithoutSeat = new ArrayList<>();

        // Get occupied seats
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT seat_number FROM " + studentTable + " WHERE seat_number IS NOT NULL");

        Set<Integer> occupied = new HashSet<>();
        while (rs.next()) {
            occupied.add(rs.getInt(1));
        }

        // Determine available seats
        for (int i = 1; i <= TOTAL_SEATS; i++) {
            if (!occupied.contains(i)) {
                availableSeats.add(i);
            }
        }

        // Get students without seats
        rs = st.executeQuery("SELECT student_id FROM " + studentTable + " WHERE seat_number IS NULL");
        while (rs.next()) {
            studentsWithoutSeat.add(rs.getString(1));
        }

        if (studentsWithoutSeat.isEmpty()) {
            return "⚠️ All students already have seats.";
        }

        if (availableSeats.isEmpty()) {
            return "❌ No available seats.";
        }

        // Shuffle seats randomly
        Collections.shuffle(availableSeats);

        PreparedStatement ps = con.prepareStatement(
                "UPDATE " + studentTable + " SET seat_number=? WHERE student_id=?");

        int assignments = Math.min(studentsWithoutSeat.size(), availableSeats.size());

        for (int i = 0; i < assignments; i++) {
            ps.setInt(1, availableSeats.get(i));
            ps.setString(2, studentsWithoutSeat.get(i));
            ps.executeUpdate();
        }

        return "✅ Random seats assigned to " + assignments + " students.";

        } catch (SQLException e) {
        e.printStackTrace();
        return "❌ Database error: " + e.getMessage();
        }
    }

    // ✅ Get student ID assigned to a seat
    public String getStudentInSeat(int seat) {
    try (Connection con = DBConnection.getConnection()) {
        PreparedStatement ps = con.prepareStatement(
            "SELECT student_id FROM " + studentTable + " WHERE seat_number=?");
        ps.setInt(1, seat);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("student_id");
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
}
