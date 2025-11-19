package service;

import model.Department;
import model.Doctor;
import model.Patient;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.sql.SQLException;

public class HospitalManagement {
    private final List<Department> departments;
    private final List<Doctor> doctors;
    private final List<Patient> patients;

    // Database helper (optional)
    private Database db;

    public HospitalManagement() {
        this.departments = new ArrayList<>();
        this.doctors = new ArrayList<>();
        this.patients = new ArrayList<>();

        // try to initialize DB and load saved data; if not present, populate demo data and save it
        try {
            db = new Database();
            if (db.hasAnyData()) {
                // load from DB
                List<Department> loadedDepts = db.loadDepartments();
                departments.addAll(loadedDepts);
                List<Doctor> loadedDocs = db.loadDoctors(departments);
                doctors.addAll(loadedDocs);
                List<Patient> loadedPats = db.loadPatients(departments, doctors);
                patients.addAll(loadedPats);
            } else {
                addInitialData();
                // persist initial data
                saveAllToDatabase();
            }
        } catch (SQLException ex) {
            // If DB initialization fails, fallback to in-memory demo data
            System.err.println("Warning: could not initialize database, running in-memory only: " + ex.getMessage());
            addInitialData();
        }
    }

    private void addInitialData() {
        // Departments
        Department cardio = new Department("Кардіологія", 30);
        Department surgery = new Department("Хірургія", 50);
        Department neuro = new Department("Неврологія", 25);
        Department pedi = new Department("Педіатрія", 40);
        Department onco = new Department("Онкологія", 20);
        Department ortho = new Department("Ортопедія", 30);

        departments.add(cardio);
        departments.add(surgery);
        departments.add(neuro);
        departments.add(pedi);
        departments.add(onco);
        departments.add(ortho);

        // Doctors
        Doctor ivanov = new Doctor("Іванов", "Петро", "Сергійович", 1975,
                "Завідувач", "Кардіолог", "050-111-22-33");
        ivanov.addDepartment(cardio);
        doctors.add(ivanov);

        Doctor petrenko = new Doctor("Петренко", "Олексій", "Іванович", 1980,
                "Лікар", "Невролог", "050-222-33-44");
        petrenko.addDepartment(neuro);
        doctors.add(petrenko);

        Doctor shevchenko = new Doctor("Шевченко", "Марія", "Петрівна", 1985,
                "Лікар", "Педіатр", "050-333-44-55");
        shevchenko.addDepartment(pedi);
        doctors.add(shevchenko);

        Doctor kovalenko = new Doctor("Коваленко", "Ігор", "Володимирович", 1978,
                "Лікар", "Хірург", "050-444-55-66");
        kovalenko.addDepartment(surgery);
        doctors.add(kovalenko);

        Doctor bondar = new Doctor("Бондар", "Тарас", "Олександрович", 1982,
                "Лікар", "Онколог", "050-555-66-77");
        bondar.addDepartment(onco);
        doctors.add(bondar);

        Doctor melnyk = new Doctor("Мельник", "Оксана", "Ігорівна", 1990,
                "Лікар", "Ортопед", "050-666-77-88");
        melnyk.addDepartment(ortho);
        doctors.add(melnyk);

        // Patients - admit several to populate tables
        Patient p1 = new Patient("Богданов", "Андрій", "Миколайович", 1990, "Інфаркт", cardio, ivanov);
        admitPatient(p1);

        Patient p2 = new Patient("Коваль", "Світлана", "Петрівна", 1988, "Кардіоміопатія", cardio, ivanov);
        admitPatient(p2);

        Patient p3 = new Patient("Гнатенко", "Олег", "Іванович", 1972, "Післяопераційний стан", surgery, kovalenko);
        admitPatient(p3);

        Patient p4 = new Patient("Лисенко", "Ольга", "Петрівна", 1992, "Пневмонія", pedi, shevchenko);
        admitPatient(p4);

        Patient p5 = new Patient("Кравчук", "Іван", "Петрович", 1965, "Інсульт", neuro, petrenko);
        admitPatient(p5);

        Patient p6 = new Patient("Ткач", "Наталя", "Василівна", 1983, "Остеоартрит", ortho, melnyk);
        admitPatient(p6);

        // Add a discharged demo patient to show discharged list
        Patient discharged = new Patient("Романенко", "Віктор", "Сергійович", 1970, "Гострий бронхіт", pedi, shevchenko);
        admitPatient(discharged);
        // Immediately discharge to appear in discharged reports
        dischargePatient(discharged);
    }

    // Persist initial demo data to DB (if db is available)
    private void saveAllToDatabase() {
        if (db == null) return;
        try {
            for (Department d : departments) db.saveDepartment(d);
            for (Doctor d : doctors) db.saveDoctor(d);
            for (Patient p : patients) db.savePatient(p);
        } catch (SQLException ex) {
            System.err.println("Failed to save initial data to DB: " + ex.getMessage());
        }
    }

    public void addDepartment(Department dept) {
        departments.add(dept);
        if (db != null) {
            try { db.saveDepartment(dept); } catch (SQLException ex) { System.err.println("DB save dept failed: " + ex.getMessage()); }
        }
    }
    public List<Department> getDepartments() { return departments; }

    public void updateDepartment(Department dept, String newName, int newCapacity) {
        if (newCapacity < dept.getCurrentOccupancy()) {
            throw new IllegalArgumentException("Нова місткість (" + newCapacity +
                    ") менша за поточну зайнятість (" + dept.getCurrentOccupancy() + ").");
        }
        dept.setName(newName); // Потребує set-метод у Department
        dept.setMaxCapacity(newCapacity); // Потребує set-метод у Department
        if (db != null) {
            try { db.saveDepartment(dept); } catch (SQLException ex) { System.err.println("DB update dept failed: " + ex.getMessage()); }
        }
    }

    public void removeDepartment(Department dept) {
        if (dept.getCurrentOccupancy() > 0) {
            throw new IllegalStateException("Неможливо видалити відділок '" + dept.getName() +
                    "', оскільки в ньому є " + dept.getCurrentOccupancy() + " пацієнтів.");
        }
        departments.remove(dept);
        // Note: DB delete not implemented; could be added if desired
    }

    // Методи для пацієнтів
    public void admitPatient(Patient patient) {
        if (patient.getDepartment().hasSpace()) {
            patients.add(patient);
            patient.getDepartment().addPatient(patient);
            if (db != null) {
                try { db.savePatient(patient); } catch (SQLException ex) { System.err.println("DB save patient failed: " + ex.getMessage()); }
            }
        } else {
            System.err.println("Помилка: Відділок заповнений.");
        }
    }

    public List<Patient> getPatients() { return patients; }

    // Новий метод: виписка пацієнта
    public void dischargePatient(Patient patient) {
        if (patient == null) return;
        if (patient.isDischarged()) {
            throw new IllegalStateException("Пацієнт вже виписаний.");
        }
        // Встановлюємо дату виписки та прибираємо з відділку
        patient.setDischargeDate(LocalDate.now());
        Department dept = patient.getDepartment();
        if (dept != null) {
            dept.removePatient(patient);
        }
        if (db != null) {
            try { db.updatePatientDischarge(patient); } catch (SQLException ex) { System.err.println("DB update discharge failed: " + ex.getMessage()); }
        }
    }

    // Новий метод: пошук пацієнтів за рядком (ПІБ або діагноз)
    public List<Patient> searchPatients(String query) {
        List<Patient> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            result.addAll(patients);
            return result;
        }
        String q = query.toLowerCase();
        for (Patient p : patients) {
            if (p.getFullName().toLowerCase().contains(q) || p.getDiagnosis().toLowerCase().contains(q)) {
                result.add(p);
            }
        }
        return result;
    }

    // Методи для лікарів
    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
        if (db != null) { try { db.saveDoctor(doctor); } catch (SQLException ex) { System.err.println("DB save doctor failed: " + ex.getMessage()); } }
    }
    public List<Doctor> getDoctors() { return doctors; }

    // Новий метод: оновлення даних лікаря
    public void updateDoctor(Doctor doctor, String newPosition, String newSpecialization, String newPhone) {
        if (doctor == null) return;
        doctor.setPosition(newPosition);
        doctor.setSpecialization(newSpecialization);
        doctor.setPhoneNumber(newPhone);
        if (db != null) { try { db.saveDoctor(doctor); } catch (SQLException ex) { System.err.println("DB update doctor failed: " + ex.getMessage()); } }
    }

    // Новий метод: видалення лікаря (перевіряємо, чи немає закріплених пацієнтів)
    public void removeDoctor(Doctor doctor) {
        if (doctor == null) return;
        // Перевірка: чи є пацієнти, які мають цього лікаря
        for (Patient p : patients) {
            if (p.getAttendingDoctor() == doctor) {
                throw new IllegalStateException("Не можна видалити лікаря, поки є пацієнти, закріплені за ним.");
            }
        }
        // Якщо все ок — видаляємо
        doctors.remove(doctor);
        // Note: DB delete not implemented here
    }
}