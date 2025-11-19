package model;

import java.util.ArrayList;
import java.util.List;

public class Doctor {
    private final String lastName;
    private final String firstName;
    private final String patronymic;
    private final int birthYear;
    private String position;
    private String specialization;
    private String phoneNumber;
    // Відділки, за якими закріплений лікар
    private final List<Department> affiliatedDepartments;

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

    public int getBirthYear() {
        return birthYear;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Department> getAffiliatedDepartments() {
        return affiliatedDepartments;
    }

    public void addDepartment(Department department) {
        if (!affiliatedDepartments.contains(department)) {
            affiliatedDepartments.add(department);
        }
    }

    public void removeDepartment(Department department) {
        affiliatedDepartments.remove(department);
    }

    // ... інші геттери та сеттери (для року народження, посади, фаху тощо)

    @Override
    public String toString() {
        return getFullName() + " (" + specialization + ")";
    }
}