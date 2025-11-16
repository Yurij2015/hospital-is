package model;

import java.util.ArrayList;
import java.util.List;

public class Department {
    private String name;
    private int maxCapacity;
    private final List<Patient> currentPatients;

    public Department(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentPatients = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public int getCurrentOccupancy() { return currentPatients.size(); }
    public boolean hasSpace() { return currentPatients.size() < maxCapacity; }

    public void addPatient(Patient patient) {
        if (hasSpace()) {
            currentPatients.add(patient);
        } else {
            throw new IllegalStateException("Відділок " + name + " заповнений.");
        }
    }

    public void removePatient(Patient patient) { currentPatients.remove(patient); }

    @Override
    public String toString() { return name + " (" + getCurrentOccupancy() + "/" + maxCapacity + ")"; }
}