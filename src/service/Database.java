package service;

import model.Department;
import model.Doctor;
import model.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;

public class Database implements AutoCloseable {
    private static final String DB_URL = "jdbc:sqlite:hospital.db";
    private final Connection conn;

    public Database() throws SQLException {
        Path dbPath = Path.of("hospital.db");
        if (Files.exists(dbPath)) {
            boolean valid = false;
            try (FileInputStream fis = new FileInputStream(dbPath.toFile())) {
                byte[] header = new byte[16];
                int n = fis.read(header);
                if (n == 16) {
                    String hdr = new String(header, StandardCharsets.US_ASCII);
                    valid = "SQLite format 3\u0000".equals(hdr);
                }
            } catch (IOException e) {
                System.err.println("Could not read hospital.db to validate header: " + e.getMessage());
            }
            if (!valid) {
                try {
                    Path backup = Path.of("hospital.db.broken." + System.currentTimeMillis());
                    Files.move(dbPath, backup, StandardCopyOption.REPLACE_EXISTING);
                    System.err.println("Existing hospital.db is not a valid SQLite DB; moved to " + backup);
                } catch (IOException ex) {
                    System.err.println("Failed to move invalid hospital.db: " + ex.getMessage());
                }
            }
        }

        Connection tmpConn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            try {
                Class<?> drvClass = Class.forName("org.sqlite.JDBC");
                java.sql.Driver drv = (java.sql.Driver) drvClass.getDeclaredConstructor().newInstance();
                try {
                    DriverManager.registerDriver(drv);
                } catch (SQLException ignore) {
                }
            } catch (ReflectiveOperationException roe) {
                System.err.println("Could not instantiate SQLite driver class: " + roe.getMessage());
            }
        } catch (ClassNotFoundException cnfe) {
            System.err.println("SQLite JDBC driver class not found: " + cnfe.getMessage());
        }
        try {
            tmpConn = DriverManager.getConnection(DB_URL);
        } catch (SQLException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("file is not a database") || msg.contains("not a database") || msg.contains("sqlite_notadb")) {
                try {
                    Path dbPath2 = dbPath;
                    if (Files.exists(dbPath2)) {
                        Path backup = Path.of("hospital.db.broken." + System.currentTimeMillis());
                        Files.move(dbPath2, backup, StandardCopyOption.REPLACE_EXISTING);
                        System.err.println("Moved invalid hospital.db to " + backup + " and retrying DB creation.");
                    }
                } catch (IOException io) {
                    System.err.println("Failed to move invalid hospital.db: " + io.getMessage());
                }
                tmpConn = DriverManager.getConnection(DB_URL);
            } else {
                throw ex;
            }
        }
        this.conn = tmpConn;

        initSchema();
    }

    private void initSchema() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
            st.execute("CREATE TABLE IF NOT EXISTS departments (\n" +
                    "  name TEXT PRIMARY KEY,\n" +
                    "  max_capacity INTEGER NOT NULL\n" +
                    ");");

            st.execute("CREATE TABLE IF NOT EXISTS doctors (\n" +
                    "  lastName TEXT NOT NULL,\n" +
                    "  firstName TEXT NOT NULL,\n" +
                    "  patronymic TEXT,\n" +
                    "  birthYear INTEGER,\n" +
                    "  position TEXT,\n" +
                    "  specialization TEXT,\n" +
                    "  phone TEXT,\n" +
                    "  primary_dept TEXT,\n" +
                    "  UNIQUE(lastName, firstName, patronymic),\n" +
                    "  FOREIGN KEY(primary_dept) REFERENCES departments(name)\n" +
                    ");");

            st.execute("CREATE TABLE IF NOT EXISTS patients (\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "  lastName TEXT NOT NULL,\n" +
                    "  firstName TEXT NOT NULL,\n" +
                    "  patronymic TEXT,\n" +
                    "  diagnosis TEXT,\n" +
                    "  admission_date TEXT,\n" +
                    "  discharge_date TEXT,\n" +
                    "  dept_name TEXT,\n" +
                    "  doctor_last TEXT,\n" +
                    "  doctor_first TEXT,\n" +
                    "  FOREIGN KEY(dept_name) REFERENCES departments(name)\n" +
                    ");");
        }
    }

    public boolean hasAnyData() throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM departments")) {
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public List<Department> loadDepartments() throws SQLException {
        List<Department> out = new ArrayList<>();
        String sql = "SELECT name, max_capacity FROM departments";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                int max = rs.getInt("max_capacity");
                out.add(new Department(name, max));
            }
        }
        return out;
    }

    public List<Doctor> loadDoctors(List<Department> departments) throws SQLException {
        List<Doctor> out = new ArrayList<>();
        String sql = "SELECT lastName, firstName, patronymic, birthYear, position, specialization, phone, primary_dept FROM doctors";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ln = rs.getString("lastName");
                String fn = rs.getString("firstName");
                String pat = rs.getString("patronymic");
                int year = rs.getInt("birthYear");
                String pos = rs.getString("position");
                String spec = rs.getString("specialization");
                String phone = rs.getString("phone");
                String primaryDept = rs.getString("primary_dept");
                Doctor d = new Doctor(ln, fn, pat, year, pos, spec, phone);
                if (primaryDept != null) {
                    for (Department dp : departments) {
                        if (dp.getName().equalsIgnoreCase(primaryDept)) {
                            d.addDepartment(dp);
                            break;
                        }
                    }
                }
                out.add(d);
            }
        }
        return out;
    }

    public List<Patient> loadPatients(List<Department> departments, List<Doctor> doctors) throws SQLException {
        List<Patient> out = new ArrayList<>();
        String sql = "SELECT lastName, firstName, patronymic, diagnosis, admission_date, discharge_date, dept_name, doctor_last, doctor_first FROM patients";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ln = rs.getString("lastName");
                String fn = rs.getString("firstName");
                String pat = rs.getString("patronymic");
                String diag = rs.getString("diagnosis");
                String dis = rs.getString("discharge_date");
                String deptName = rs.getString("dept_name");
                String docLast = rs.getString("doctor_last");

                Department dept = null;
                for (Department d : departments) if (d.getName().equalsIgnoreCase(deptName)) { dept = d; break; }

                Doctor doc = null;
                for (Doctor d : doctors) if (d.getFullName().toLowerCase().startsWith(docLast.toLowerCase())) { doc = d; break; }
                Patient p = new Patient(ln, fn, pat, 0, diag, dept, doc);
                if (dis != null && !dis.isEmpty()) {
                    try {
                        p.setDischargeDate(LocalDate.parse(dis));
                    } catch (Exception ignored) {}
                }
                out.add(p);
            }
        }
        return out;
    }

    public void saveDepartment(Department d) throws SQLException {
        String sql = "INSERT OR REPLACE INTO departments(name, max_capacity) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setInt(2, d.getMaxCapacity());
            ps.executeUpdate();
        }
    }

    public void saveDoctor(Doctor d) throws SQLException {
        String sql = "INSERT OR REPLACE INTO doctors(lastName, firstName, patronymic, birthYear, position, specialization, phone, primary_dept) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String full = d.getFullName();
            ps.setString(1, full);
            ps.setString(2, "");
            ps.setString(3, "");
            ps.setInt(4, d.getBirthYear());
            ps.setString(5, d.getPosition());
            ps.setString(6, d.getSpecialization());
            ps.setString(7, d.getPhoneNumber());
            String primary = d.getAffiliatedDepartments().isEmpty() ? null : d.getAffiliatedDepartments().get(0).getName();
            ps.setString(8, primary);
            ps.executeUpdate();
        }
    }

    public void savePatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patients(lastName, firstName, patronymic, diagnosis, admission_date, discharge_date, dept_name, doctor_last, doctor_first) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getFullName().split(" ")[0]);
            ps.setString(2, "");
            ps.setString(3, "");
            ps.setString(4, p.getDiagnosis());
            ps.setString(5, LocalDate.now().toString());
            ps.setString(6, p.getDischargeDate() != null ? p.getDischargeDate().toString() : null);
            ps.setString(7, p.getDepartment() != null ? p.getDepartment().getName() : null);
            ps.setString(8, p.getAttendingDoctor() != null ? p.getAttendingDoctor().getFullName().split(" ")[0] : null);
            ps.setString(9, p.getAttendingDoctor() != null ? "" : null);
            ps.executeUpdate();
        }
    }

    public void updatePatientDischarge(Patient p) throws SQLException {
        String sql = "UPDATE patients SET discharge_date = ? WHERE lastName = ? AND firstName = ? AND discharge_date IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getDischargeDate() != null ? p.getDischargeDate().toString() : null);
            ps.setString(2, p.getFullName().split(" ")[0]);
            ps.setString(3, "");
            ps.executeUpdate();
        }
    }

    @Override
    public void close() throws Exception {
        if (conn != null && !conn.isClosed()) conn.close();
    }
}
