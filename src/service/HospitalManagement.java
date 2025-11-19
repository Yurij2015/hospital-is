package service;

import model.Department;
import model.Doctor;
import model.Patient;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class HospitalManagement {
    private final List<Department> departments;
    private final List<Doctor> doctors;
    private final List<Patient> patients;

    public HospitalManagement() {
        this.departments = new ArrayList<>();
        this.doctors = new ArrayList<>();
        this.patients = new ArrayList<>();
        addInitialData();
    }

    private void addInitialData() {
        Department cardio = new Department("Кардіологія", 30);
        Department surgery = new Department("Хірургія", 50);
        departments.add(cardio);
        departments.add(surgery);

        Doctor ivanov = new Doctor("Іванов", "Петро", "Сергійович", 1975,
                "Завідувач", "Кардіолог", "050-111-22-33");
        ivanov.addDepartment(cardio);
        doctors.add(ivanov);
    }

    public void addDepartment(Department dept) { departments.add(dept); }
    public List<Department> getDepartments() { return departments; }

    public void updateDepartment(Department dept, String newName, int newCapacity) {
        if (newCapacity < dept.getCurrentOccupancy()) {
            throw new IllegalArgumentException("Нова місткість (" + newCapacity +
                    ") менша за поточну зайнятість (" + dept.getCurrentOccupancy() + ").");
        }
        dept.setName(newName); // Потребує set-метод у Department
        dept.setMaxCapacity(newCapacity); // Потребує set-метод у Department
    }

    public void removeDepartment(Department dept) {
        if (dept.getCurrentOccupancy() > 0) {
            throw new IllegalStateException("Неможливо видалити відділок '" + dept.getName() +
                    "', оскільки в ньому є " + dept.getCurrentOccupancy() + " пацієнтів.");
        }
        departments.remove(dept);
    }

    // Методи для пацієнтів
    public void admitPatient(Patient patient) {
        if (patient.getDepartment().hasSpace()) {
            patients.add(patient);
            patient.getDepartment().addPatient(patient);
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
    public void addDoctor(Doctor doctor) { doctors.add(doctor); }
    public List<Doctor> getDoctors() { return doctors; }

    // Новий метод: оновлення даних лікаря
    public void updateDoctor(Doctor doctor, String newPosition, String newSpecialization, String newPhone) {
        if (doctor == null) return;
        doctor.setPosition(newPosition);
        doctor.setSpecialization(newSpecialization);
        doctor.setPhoneNumber(newPhone);
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
    }
}