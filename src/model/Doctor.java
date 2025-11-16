package model;

import java.util.ArrayList;
import java.util.List;

public class Doctor {
    private String lastName;
    private String firstName;
    private String patronymic;
    private int birthYear;
    private String position;
    private String specialization;
    private String phoneNumber;
    // Відділки, за якими закріплений лікар
    private List<Department> affiliatedDepartments;

    public Doctor(String lastName, String firstName, String patronymic, int birthYear,
                  String position, String specialization, String phoneNumber) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.birthYear = birthYear;
        this.position = position;
        this.specialization = specialization;
        this.phoneNumber = phoneNumber;
        this.affiliatedDepartments = new ArrayList<>();
    }

    // --- Getters and Setters ---
    public String getFullName() {
        return lastName + " " + firstName.charAt(0) + "." + patronymic.charAt(0) + ".";
    }

    public List<Department> getAffiliatedDepartments() {
        return affiliatedDepartments;
    }

    public void addDepartment(Department department) {
        if (!affiliatedDepartments.contains(department)) {
            affiliatedDepartments.add(department);
        }
    }

    // ... інші геттери та сеттери (для року народження, посади, фаху тощо)

    @Override
    public String toString() {
        return getFullName() + " (" + specialization + ")";
    }
}