package testF;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import src.DBConnection;

public class SeatManager {
    public static final int TOTAL_SEATS = 40;
    private Map<String, Student> students = new HashMap<>();
    private Map<String, Admin> admins = new HashMap<>();
    private Set<Integer> occupiedSeats = new HashSet<>();

    public String registerStudent(String f, String l, String id) {
        if (students.containsKey(id)) return "Student ID already exists!";
        students.put(id, new Student(f, l, id));
        return "Student registered successfully! ID: " + id;
    }

    public String registerAdmin(String f, String l, String id, String password) {
        if (admins.containsKey(id)) return "Admin ID already registered!";
        admins.put(id, new Admin(f, l, id, password));
        return "Admin registered successfully!";
    }

    public boolean verifyAdminLogin(String id, String password) {
        Admin admin = admins.get(id);
        if (admin == null) return false;
        return admin.password.equals(password);
    }

    public Admin getAdmin(String id) {
        return admins.get(id);
    }

    public boolean isSeatOccupied(int seat) {
        return occupiedSeats.contains(seat);
    }

    public int getOccupiedCount() {
        return occupiedSeats.size();
    }

    public Set<Integer> getOccupiedSeats() {
        return occupiedSeats;
    }

    public String toggleSeatForStudent(String id) {
        Student s = students.get(id);
        if (s == null) return "Student not found! Please ask an admin to register you first.";

        if (s.seat == -1) {
            int seat = findFreeSeat();
            if (seat == -1) return "No available seats!";
            s.seat = seat;
            occupiedSeats.add(seat);
            return "Seat #" + seat + " assigned to " + s.firstName + ".";
        } else {
            occupiedSeats.remove(s.seat);
            int freed = s.seat;
            s.seat = -1;
            return "Seat #" + freed + " freed for " + s.firstName + ".";
        }
    }

    private int findFreeSeat() {
        for (int i = 1; i <= TOTAL_SEATS; i++) {
            if (!occupiedSeats.contains(i)) return i;
        }
        return -1;
    }
    public String assignSeatManually(String studentId, int seat) {
    if (seat < 1 || seat > TOTAL_SEATS)
        return "❌ Invalid seat number. Must be between 1 and " + TOTAL_SEATS + ".";

    Student s = students.get(studentId);
    if (s == null)
        return "❌ Student not found. Register first.";

    if (occupiedSeats.contains(seat))
        return "❌ Seat #" + seat + " is already occupied.";

    // Free old seat if exists
    if (s.seat != -1)
        occupiedSeats.remove(s.seat);

    s.seat = seat;
    occupiedSeats.add(seat);
    return "✅ Seat #" + seat + " successfully assigned to " + s.firstName + ".";
    }

    // ✅ Get student ID assigned to a seat
    public String getStudentInSeat(int seat) {
    try (Connection con = DBConnection.getConnection()) {
        PreparedStatement ps = con.prepareStatement(
            "SELECT student_id FROM students WHERE seat_number=?");
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
