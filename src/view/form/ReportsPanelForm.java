package view.form;

import service.HospitalManagement;
import model.Department;
import model.Doctor;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsPanelForm extends JPanel {

    private final HospitalManagement manager;
    private final JComboBox<String> reportTypeCombo;
    private final JButton generateButton;
    private final JTextArea outputArea;

    public ReportsPanelForm(HospitalManagement manager) {
        this.manager = manager;

        setLayout(new BorderLayout(8, 8));

        // Top controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypeCombo = new JComboBox<>(new String[]{"Завантаженість відділків", "Кількість пацієнтів на лікаря", "Список виписаних пацієнтів"});
        generateButton = new JButton("Генерувати");
        top.add(new JLabel("Тип звіту:"));
        top.add(reportTypeCombo);
        top.add(generateButton);

        add(top, BorderLayout.NORTH);

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(outputArea);
        add(scroll, BorderLayout.CENTER);

        // Handler
        generateButton.addActionListener(e -> generateSelectedReport());
    }

    private void generateSelectedReport() {
        String selection = (String) reportTypeCombo.getSelectedItem();
        if (selection == null) return;

        switch (selection) {
            case "Завантаженість відділків" -> showDepartmentOccupancy();
            case "Кількість пацієнтів на лікаря" -> showPatientsPerDoctor();
            case "Список виписаних пацієнтів" -> showDischargedPatients();
            default -> outputArea.setText("Невідомий тип звіту.");
        }
    }

    private void showDepartmentOccupancy() {
        List<Department> depts = manager.getDepartments();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %10s\n", "Відділок", "Зайнято/Макс"));
        sb.append("------------------------------------------------\n");
        for (Department d : depts) {
            sb.append(String.format("%-30s %3d/%-3d\n", d.getName(), d.getCurrentOccupancy(), d.getMaxCapacity()));
        }
        outputArea.setText(sb.toString());
    }

    private void showPatientsPerDoctor() {
        List<Doctor> docs = manager.getDoctors();
        Map<Doctor, Long> counts = docs.stream()
                .collect(Collectors.toMap(d -> d,
                        d -> manager.getPatients().stream().filter(p -> p.getAttendingDoctor() == d).count()));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %10s\n", "Лікар", "Пацієнтів"));
        sb.append("------------------------------------------------\n");
        for (Map.Entry<Doctor, Long> e : counts.entrySet()) {
            sb.append(String.format("%-30s %10d\n", e.getKey().getFullName(), e.getValue()));
        }
        outputArea.setText(sb.toString());
    }

    private void showDischargedPatients() {
        List<Patient> list = manager.getPatients().stream().filter(Patient::isDischarged).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %-20s %-12s\n", "Пацієнт", "Відділок", "Дата виписки"));
        sb.append("---------------------------------------------------------------\n");
        for (Patient p : list) {
            sb.append(String.format("%-30s %-20s %-12s\n",
                    p.getFullName(), (p.getDepartment() != null ? p.getDepartment().getName() : "Н/Д"),
                    (p.getDischargeDate() != null ? p.getDischargeDate().toString() : "-")));
        }
        outputArea.setText(sb.toString());
    }
}

