package model;

import java.time.LocalDate;

public class Patient {
    private String lastName;
    private String firstName;
    private String patronymic;
    private int birthYear;
    private String diagnosis;
    private LocalDate admissionDate;
    private Department department;
    private Doctor attendingDoctor;
    private LocalDate dischargeDate; // Null, якщо пацієнт ще не виписаний

    public Patient(String lastName, String firstName, String patronymic, int birthYear,
                   String diagnosis, Department department, Doctor attendingDoctor) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.birthYear = birthYear;
        this.diagnosis = diagnosis;
        this.admissionDate = LocalDate.now(); // Дата прийому - сьогодні
        this.department = department;
        this.attendingDoctor = attendingDoctor;
        this.dischargeDate = null;
    }

    // --- Getters and Setters ---

    // Геттер, необхідний для service.HospitalManagement (для перевірки місць у відділку)
    public Department getDepartment() {
        return department;
    }

    // Геттер для лікаря (потрібен для відображення в таблиці та звітів)
    public Doctor getAttendingDoctor() {
        return attendingDoctor;
    }

    public String getFullName() {
        return lastName + " " + firstName.charAt(0) + "." + patronymic.charAt(0) + ".";
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public LocalDate getDischargeDate() {
        return dischargeDate;
    }

    public boolean isDischarged() {
        return dischargeDate != null;
    }

    // Сеттер для дати виписки (використовується при виписці пацієнта)
    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    // Опціонально: Сеттер для зміни лікаря
    public void setAttendingDoctor(Doctor attendingDoctor) {
        this.attendingDoctor = attendingDoctor;
    }

    // Опціонально: Сеттер для переведення пацієнта (потрібно також оновити відділок у service.HospitalManagement!)
    public void setDepartment(Department department) {
        this.department = department;
    }
}