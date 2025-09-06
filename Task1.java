import java.sql.*;
import java.util.*;

// Class 1: User
class User {
    private String username;
    private String password;
    private Scanner sc = new Scanner(System.in);

    public String getUsername() {
        System.out.print("Enter DB Username: ");
        username = sc.nextLine();
        return username;
    }

    public String getPassword() {
        System.out.print("Enter DB Password: ");
        password = sc.nextLine();
        return password;
    }
}

// Class 2: PnrRecord
class PnrRecord {
    private int pnrNumber;
    private String passengerName;
    private String trainNumber;
    private String classType;
    private String journeyDate;
    private String from;
    private String to;

    private static final int MIN = 1000;
    private static final int MAX = 9999;
    private Scanner sc = new Scanner(System.in);

    public int generatePnrNumber() {
        Random random = new Random();
        pnrNumber = random.nextInt(MAX - MIN + 1) + MIN;
        return pnrNumber;
    }

    public void collectPassengerDetails() {
        System.out.print("Enter Passenger Name: ");
        passengerName = sc.nextLine();
        System.out.print("Enter Train Number: ");
        trainNumber = sc.nextLine();
        System.out.print("Enter Class Type: ");
        classType = sc.nextLine();
        System.out.print("Enter Journey Date (YYYY-MM-DD): ");
        journeyDate = sc.nextLine();
        System.out.print("Enter Starting Place: ");
        from = sc.nextLine();
        System.out.print("Enter Destination Place: ");
        to = sc.nextLine();
    }

    // getters for DB insert
    public int getPnrNumber() { return pnrNumber; }
    public String getPassengerName() { return passengerName; }
    public String getTrainNumber() { return trainNumber; }
    public String getClassType() { return classType; }
    public String getJourneyDate() { return journeyDate; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
}

// Class 3: ReservationSystem
class ReservationSystem {
    private Connection connection;

    public ReservationSystem(Connection connection) {
        this.connection = connection;
    }

    // Insert new reservation
    public void insertReservation(PnrRecord record) {
        String query = "INSERT INTO reservations VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, record.getPnrNumber());
            ps.setString(2, record.getPassengerName());
            ps.setString(3, record.getTrainNumber());
            ps.setString(4, record.getClassType());
            ps.setString(5, record.getJourneyDate());
            ps.setString(6, record.getFrom());
            ps.setString(7, record.getTo());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Reservation added successfully with PNR: " + record.getPnrNumber());
            } else {
                System.out.println("❌ Failed to add reservation.");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting record: " + e.getMessage());
        }
    }

    // Delete reservation
    public void deleteReservation(int pnr) {
        String query = "DELETE FROM reservations WHERE pnr_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, pnr);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Reservation deleted successfully.");
            } else {
                System.out.println("❌ No reservation found with that PNR.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting record: " + e.getMessage());
        }
    }

    // Show all reservations
    public void showAllReservations() {
        String query = "SELECT * FROM reservations";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n--- All Reservations ---");
            while (rs.next()) {
                System.out.println("PNR Number: " + rs.getInt("pnr_number"));
                System.out.println("Passenger Name: " + rs.getString("passenger_name"));
                System.out.println("Train Number: " + rs.getString("train_number"));
                System.out.println("Class Type: " + rs.getString("class_type"));
                System.out.println("Journey Date: " + rs.getString("journey_date"));
                System.out.println("From: " + rs.getString("from_location"));
                System.out.println("To: " + rs.getString("to_location"));
                System.out.println("-------------------------");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching records: " + e.getMessage());
        }
    }
}

// Class 4: Menu
class Menu {
    private Scanner sc = new Scanner(System.in);
    private ReservationSystem system;

    public Menu(ReservationSystem system) {
        this.system = system;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n******************** ONLINE RESERVATION SYSTEM ********************");
            System.out.println("1. Insert Reservation");
            System.out.println("2. Delete Reservation");
            System.out.println("3. Show All Reservations");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume leftover newline

            switch (choice) {
                case 1:
                    PnrRecord record = new PnrRecord();
                    record.generatePnrNumber();
                    record.collectPassengerDetails();
                    system.insertReservation(record);
                    break;
                case 2:
                    System.out.print("Enter PNR Number to delete: ");
                    int pnr = sc.nextInt();
                    system.deleteReservation(pnr);
                    break;
                case 3:
                    system.showAllReservations();
                    break;
                case 4:
                    System.out.println("👋 Exiting system. Goodbye!");
                    return;
                default:
                    System.out.println("❌ Invalid choice. Try again.");
            }
        }
    }
}

// Class 5: Main
public class Task1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        User user = new User();
        String username = user.getUsername();
        String password = user.getPassword();

        String url = "jdbc:mysql://localhost:3306/vasu"; // change DB name if needed

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("✅ Database Connected Successfully!");
                ReservationSystem system = new ReservationSystem(connection);
                Menu menu = new Menu(system);
                menu.showMenu();
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }
}
