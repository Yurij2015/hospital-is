package model;

import java.util.ArrayList;
import java.util.List;

public class Department {
    private String name;
    private int maxCapacity;
    // Пацієнти, що зараз перебувають у цьому відділку
    private List<Patient> currentPatients;

    public Department(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentPatients = new ArrayList<>();
    }

    // --- Getters and Setters ---
    public String getName() {
        return name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentOccupancy() {
        return currentPatients.size();
    }

    // Перевірка, чи є місце
    public boolean hasSpace() {
        return currentPatients.size() < maxCapacity;
    }

    // Метод для додавання/видалення пацієнтів
    public void addPatient(Patient patient) {
        if (hasSpace()) {
            currentPatients.add(patient);
        } else {
            throw new IllegalStateException("Відділок " + name + " заповнений.");
        }
    }

    public void removePatient(Patient patient) {
        currentPatients.remove(patient);
    }

    @Override
    public String toString() {
        return name + " (" + getCurrentOccupancy() + "/" + maxCapacity + ")";
    }
}