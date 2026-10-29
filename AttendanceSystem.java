import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

// ---------------- Student class ----------------
class Student {
    String roll, name, section;
    int[] conducted = new int[3];   // classes conducted in each subject
    int[] attended = new int[3];    // classes attended in each subject

    Student(String roll, String name, String section, int[] conducted, int[] attended) {
        this.roll = roll;
        this.name = name;
        this.section = section;
        this.conducted = conducted;
        this.attended = attended;
    }

    // Attendance % = attended / conducted * 100
    double percentage(int sub) {
        if (conducted[sub] == 0) return 0;
        return attended[sub] * 100.0 / conducted[sub];
    }

    double overall() {
        int c = 0, a = 0;
        for (int i = 0; i < 3; i++) {
            c += conducted[i];
            a += attended[i];
        }
        if (c == 0) return 0;
        return a * 100.0 / c;
    }
}

// ---------------- One attendance record ----------------
class Record {
    String roll, name, subject, sessionId, date, status;

    Record(String roll, String name, String subject, String sessionId, String date, String status) {
        this.roll = roll;
        this.name = name;
        this.subject = subject;
        this.sessionId = sessionId;
        this.date = date;
        this.status = status;
    }
}

// ---------------- Attendance session ----------------
class Session {
    String id, subject, section, code;
    int subjectIndex;
    long expiryTime;                                   // time when the code expires
    ArrayList<String> presentRolls = new ArrayList<>(); // students who marked attendance
}

// ---------------- Main class ----------------
public class AttendanceSystem {
    static String[] subjects = {"Data Structures", "Java", "Mathematics"};
    static ArrayList<Student> students = new ArrayList<>();
    static ArrayList<Record> records = new ArrayList<>();
    static Session session = null;       // null means no active session
    static int sessionCount = 100;
    static final int EXPIRY_SECONDS = 90; // code is valid for 90 seconds
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadDemoData();
        while (true) {
            System.out.println("\n===== AUTOMATED STUDENT ATTENDANCE SYSTEM =====");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Enter choice: ");
            int ch = readInt();
            if (ch == 1) {
                login();
            } else if (ch == 2) {
                System.out.println("Thank you!");
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
    }

    // ---------- demo data ----------
    static void loadDemoData() {
        students.add(new Student("2620080001", "Aarav Sharma", "AI&DS-10", new int[]{30, 28, 32}, new int[]{26, 25, 29}));
        students.add(new Student("2620080002", "Ananya Reddy", "AI&DS-10", new int[]{30, 28, 32}, new int[]{28, 24, 30}));
        students.add(new Student("2620080003", "Vivaan Gupta", "AI&DS-10", new int[]{30, 28, 32}, new int[]{22, 20, 27}));
        students.add(new Student("2620080004", "Diya Patel", "AI&DS-10", new int[]{30, 28, 32}, new int[]{29, 27, 31}));
        students.add(new Student("2620080015", "Rahul Kumar", "AI&DS-10", new int[]{30, 28, 32}, new int[]{26, 19, 29}));
        students.add(new Student("2620080006", "Sneha Iyer", "AI&DS-10", new int[]{30, 28, 32}, new int[]{24, 22, 26}));
    }

    // ---------- small helper methods ----------
    static int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    static Student findStudent(String roll) {
        for (Student s : students) {
            if (s.roll.equals(roll)) return s;
        }
        return null;
    }

    static String status(double p) {
        if (p >= 85) return "Good";
        else if (p >= 75) return "Monitor";
        else return "Warning";
    }

    static int countSection(String sec) {
        int n = 0;
        for (Student s : students) {
            if (s.section.equals(sec)) n++;
        }
        return n;
    }

    // ---------- login ----------
    static void login() {
        System.out.println("Select role: 1.Admin  2.Faculty  3.Student");
        int role = readInt();
        System.out.print("Username (student: roll number): ");
        String user = sc.nextLine().trim();
        System.out.print("Password: ");
        String pass = sc.nextLine().trim();

        if (role == 1 && user.equals("admin") && pass.equals("admin123")) {
            adminMenu();
        } else if (role == 2 && user.equals("faculty") && pass.equals("faculty123")) {
            facultyMenu();
        } else if (role == 3) {
            Student s = findStudent(user);
            if (s != null && pass.equals("student123")) studentMenu(s);
            else System.out.println("Invalid login!");
        } else {
            System.out.println("Invalid login!");
        }
    }

    // ================= FACULTY =================
    static void facultyMenu() {
        System.out.println("\nWelcome, Dr. Ramesh Kumar");
        while (true) {
            System.out.println("\n--- FACULTY MENU ---");
            System.out.println("1. Start Attendance Session");
            System.out.println("2. Refresh QR Code");
            System.out.println("3. Show Live Attendance");
            System.out.println("4. End Session");
            System.out.println("5. Attendance Records");
            System.out.println("6. Low Attendance Alerts");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");
            int ch = readInt();
            switch (ch) {
                case 1: startSession(); break;
                case 2: refreshCode(); break;
                case 3: liveAttendance(); break;
                case 4: endSession(); break;
                case 5: showRecords(); break;
                case 6: lowAlerts(); break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    static void startSession() {
        if (session != null) {
            System.out.println("A session is already running. End it first.");
            return;
        }
        for (int i = 0; i < 3; i++) {
            System.out.println((i + 1) + ". " + subjects[i]);
        }
        System.out.print("Select subject: ");
        int sub = readInt() - 1;
        if (sub < 0 || sub > 2) {
            System.out.println("Invalid subject!");
            return;
        }
        System.out.print("Enter section (example AI&DS-10): ");
        String sec = sc.nextLine().trim();
        if (countSection(sec) == 0) {
            System.out.println("No students found in this section!");
            return;
        }
        session = new Session();
        sessionCount++;
        session.id = subjects[sub].substring(0, 2).toUpperCase() + sessionCount;
        session.subject = subjects[sub];
        session.subjectIndex = sub;
        session.section = sec;
        generateCode();
        showQR();
    }

    // new random 4 digit code + new expiry time
    static void generateCode() {
        Random r = new Random();
        session.code = String.valueOf(1000 + r.nextInt(9000));
        session.expiryTime = System.currentTimeMillis() + EXPIRY_SECONDS * 1000L;
    }

    static void showQR() {
        long remaining = (session.expiryTime - System.currentTimeMillis()) / 1000;
        System.out.println("\n+--------------------------------------+");
        System.out.println("|          ATTENDANCE SESSION          |");
        System.out.println("+--------------------------------------+");
        System.out.println("Subject    : " + session.subject);
        System.out.println("Faculty    : Dr. Ramesh Kumar");
        System.out.println("Section    : " + session.section);
        System.out.println("Date       : " + new Date());
        System.out.println("Session ID : " + session.id);
        System.out.println("QR CODE    : [ " + session.id + "-" + session.code + " ]");
        System.out.println("Expires in : " + remaining + " seconds");
        System.out.println("(Logout and let the student login to enter this code)");
    }

    static void refreshCode() {
        if (session == null) {
            System.out.println("No active session!");
            return;
        }
        generateCode();
        showQR();
    }

    static void liveAttendance() {
        if (session == null) {
            System.out.println("No active session!");
            return;
        }
        System.out.println("Present: " + session.presentRolls.size() + " / " + countSection(session.section));
        for (String roll : session.presentRolls) {
            Student s = findStudent(roll);
            System.out.println(" - " + s.roll + "  " + s.name);
        }
    }

    // End session: update attendance of every student in that section
    static void endSession() {
        if (session == null) {
            System.out.println("No active session!");
            return;
        }
        String date = new Date().toString();
        for (Student s : students) {
            if (s.section.equals(session.section)) {
                s.conducted[session.subjectIndex]++;
                String stat = "Absent";
                if (session.presentRolls.contains(s.roll)) {
                    s.attended[session.subjectIndex]++;
                    stat = "Present";
                }
                records.add(new Record(s.roll, s.name, session.subject, session.id, date, stat));
                if (s.percentage(session.subjectIndex) < 75) {
                    System.out.println("ALERT: LOW ATTENDANCE - " + s.name + " in " + session.subject
                            + " = " + String.format("%.1f", s.percentage(session.subjectIndex)) + "%");
                }
            }
        }
        System.out.println("Session " + session.id + " completed. Present: " + session.presentRolls.size());
        session = null;
    }

    static void showRecords() {
        if (records.isEmpty()) {
            System.out.println("No records yet. Run a session first.");
            return;
        }
        System.out.printf("%-12s %-16s %-16s %-8s %-8s%n", "Roll", "Name", "Subject", "Session", "Status");
        for (Record r : records) {
            System.out.printf("%-12s %-16s %-16s %-8s %-8s%n", r.roll, r.name, r.subject, r.sessionId, r.status);
        }
    }

    static void lowAlerts() {
        boolean found = false;
        System.out.println("\n--- LOW ATTENDANCE ALERTS (below 75%) ---");
        for (Student s : students) {
            for (int i = 0; i < 3; i++) {
                if (s.conducted[i] > 0 && s.percentage(i) < 75) {
                    found = true;
                    System.out.println("LOW ATTENDANCE | " + s.name + " | " + s.roll + " | " + subjects[i]
                            + " | " + String.format("%.1f", s.percentage(i)) + "% | Attended: " + s.attended[i]
                            + " | Missed: " + (s.conducted[i] - s.attended[i]) + " | Status: Warning");
                }
            }
        }
        if (!found) System.out.println("No student is below 75%.");
    }

    // ================= STUDENT =================
    static void studentMenu(Student st) {
        System.out.println("\nWelcome, " + st.name + " (" + st.roll + ", " + st.section + ")");
        while (true) {
            System.out.println("\n--- STUDENT MENU ---");
            System.out.println("1. Scan QR / Enter Session Code");
            System.out.println("2. My Attendance");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");
            int ch = readInt();
            if (ch == 1) markAttendance(st);
            else if (ch == 2) myAttendance(st);
            else if (ch == 0) return;
            else System.out.println("Invalid choice!");
        }
    }

    static void markAttendance(Student st) {
        if (session == null) {
            System.out.println("No active session right now.");
            return;
        }
        if (!session.section.equals(st.section)) {
            System.out.println("This session is not for your section.");
            return;
        }
        System.out.print("Enter code (only the 4 digits): ");
        String code = sc.nextLine().trim();

        if (System.currentTimeMillis() > session.expiryTime) {
            System.out.println("QR code expired! Ask faculty to refresh it.");
            return;
        }
        if (!code.equals(session.code)) {
            System.out.println("Wrong code! Attendance not marked.");
            return;
        }
        if (session.presentRolls.contains(st.roll)) {
            System.out.println("Attendance already marked for this session!");
            return;
        }
        session.presentRolls.add(st.roll);
        System.out.println("\n*** Attendance Marked Successfully ***");
        System.out.println("Subject : " + session.subject);
        System.out.println("Date    : " + new Date());
        System.out.println("Status  : Present");
    }

    static void myAttendance(Student st) {
        System.out.println("\nOverall Attendance: " + String.format("%.1f", st.overall()) + "% (" + status(st.overall()) + ")");
        System.out.printf("%-18s %-10s %-9s %-7s %-8s %-8s%n", "Subject", "Conducted", "Attended", "Absent", "Percent", "Status");
        for (int i = 0; i < 3; i++) {
            System.out.printf("%-18s %-10d %-9d %-7d %-8.1f %-8s%n", subjects[i], st.conducted[i], st.attended[i],
                    st.conducted[i] - st.attended[i], st.percentage(i), status(st.percentage(i)));
            if (st.conducted[i] > 0 && st.percentage(i) < 75) {
                System.out.println("   ** WARNING: Low attendance in " + subjects[i] + " **");
            }
        }
    }

    // ================= ADMIN =================
    static void adminMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. View All Students Attendance");
            System.out.println("2. Add Student");
            System.out.println("3. Delete Student");
            System.out.println("4. Low Attendance Alerts");
            System.out.println("5. Attendance Records");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");
            int ch = readInt();
            if (ch == 1) {
                System.out.printf("%-12s %-16s %-10s %-9s %-8s%n", "Roll", "Name", "Section", "Overall%", "Status");
                for (Student s : students) {
                    System.out.printf("%-12s %-16s %-10s %-9.1f %-8s%n", s.roll, s.name, s.section, s.overall(), status(s.overall()));
                }
                System.out.println("Total students: " + students.size());
            } else if (ch == 2) {
                System.out.print("Roll number: ");
                String roll = sc.nextLine().trim();
                if (findStudent(roll) != null) {
                    System.out.println("Roll number already exists!");
                    continue;
                }
                System.out.print("Name: ");
                String name = sc.nextLine().trim();
                System.out.print("Section: ");
                String sec = sc.nextLine().trim();
                students.add(new Student(roll, name, sec, new int[3], new int[3]));
                System.out.println("Student added.");
            } else if (ch == 3) {
                System.out.print("Roll number to delete: ");
                Student s = findStudent(sc.nextLine().trim());
                if (s == null) System.out.println("Student not found!");
                else {
                    students.remove(s);
                    System.out.println("Student deleted.");
                }
            } else if (ch == 4) lowAlerts();
            else if (ch == 5) showRecords();
            else if (ch == 0) return;
            else System.out.println("Invalid choice!");
        }
    }
}