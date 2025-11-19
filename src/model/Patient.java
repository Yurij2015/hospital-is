package model;

import java.time.LocalDate;

public class Patient {
    private final String lastName;
    private final String firstName;
    private final String patronymic;
    private final String diagnosis;
    private final LocalDate admissionDate;
    private Department department;
    private Doctor attendingDoctor;
    private LocalDate dischargeDate;

    public Patient(String lastName, String firstName, String patronymic, int birthYear,
                   String diagnosis, Department department, Doctor attendingDoctor) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.diagnosis = diagnosis;
        this.admissionDate = LocalDate.now();
        this.department = department;
        this.attendingDoctor = attendingDoctor;
        this.dischargeDate = null;
    }

    public Department getDepartment() {
        return department;
    }

    public Doctor getAttendingDoctor() {
        return attendingDoctor;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(lastName != null ? lastName : "");
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(' ').append(firstName.charAt(0)).append('.');
        }
        if (patronymic != null && !patronymic.isEmpty()) {
            sb.append(patronymic.charAt(0)).append('.');
        }
        return sb.toString();
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

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public void setAttendingDoctor(Doctor attendingDoctor) {
        this.attendingDoctor = attendingDoctor;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}