package service;

import java.util.ArrayList;
import java.util.List;

import model.Department;
import model.Doctor;
import model.Patient;

public class HospitalManagement {
    private List<Department> departments;
    private List<Doctor> doctors;
    private List<Patient> patients;

    public HospitalManagement() {
        this.departments = new ArrayList<>();
        this.doctors = new ArrayList<>();
        this.patients = new ArrayList<>();
        // Тут можна додати тестові дані для початку
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

        // ... додавання інших тестових даних
    }

    // --- Методи для керування даними ---
    public void addDepartment(Department dept) { departments.add(dept); }
    public List<Department> getDepartments() { return departments; }

    public void addDoctor(Doctor doctor) { doctors.add(doctor); }
    public List<Doctor> getDoctors() { return doctors; }

    public void admitPatient(Patient patient) {
        if (patient.getDepartment().hasSpace()) {
            patients.add(patient);
            patient.getDepartment().addPatient(patient);
        } else {
            // Обробка помилки або виключення
            System.err.println("Помилка: Відділок заповнений.");
        }
    }
    public List<Patient> getPatients() { return patients; }

    // ... методи для виписки пацієнта, пошуку, редагування
}