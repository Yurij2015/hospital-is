package view.form;

import service.HospitalManagement;
import model.Department;
import model.Doctor;
import model.Patient;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsPanelForm extends JPanel {

    private final HospitalManagement manager;
    private final JComboBox<String> reportTypeCombo;
    private final JTextArea outputArea;

    public ReportsPanelForm(HospitalManagement manager) {
        this.manager = manager;

        setLayout(new BorderLayout(8, 8));

        // Top controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypeCombo = new JComboBox<>(new String[]{"Завантаженість відділків", "Кількість пацієнтів на лікаря", "Список виписаних пацієнтів"});
        JButton generateButton = new JButton("Генерувати");
        JButton exportButton = new JButton("Експорт в CSV");
        top.add(new JLabel("Тип звіту:"));
        top.add(reportTypeCombo);
        top.add(generateButton);
        top.add(exportButton);

        add(top, BorderLayout.NORTH);

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(outputArea);
        add(scroll, BorderLayout.CENTER);

        // Handlers
        generateButton.addActionListener(e -> generateSelectedReport());
        exportButton.addActionListener(e -> exportCurrentReport());
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
        List<Patient> list = manager.getPatients().stream().filter(Patient::isDischarged).toList();
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

    // Export currently selected report to CSV
    private void exportCurrentReport() {
        String selection = (String) reportTypeCombo.getSelectedItem();
        if (selection == null) {
            JOptionPane.showMessageDialog(this, "Виберіть тип звіту перед експортом.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String[]> csvRows = new ArrayList<>();
        switch (selection) {
            case "Завантаженість відділків" -> csvRows = buildDepartmentOccupancyCsv();
            case "Кількість пацієнтів на лікаря" -> csvRows = buildPatientsPerDoctorCsv();
            case "Список виписаних пацієнтів" -> csvRows = buildDischargedPatientsCsv();
            default -> {
                JOptionPane.showMessageDialog(this, "Невідомий тип звіту.", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (csvRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Немає даних для експорту.", "Інформація", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Зберегти CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        int userSelection = chooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        if (file.exists()) {
            int resp = JOptionPane.showConfirmDialog(this, "Файл вже існує. Перезаписати?", "Підтвердження", JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) return;
        }

        try (FileWriter fw = new FileWriter(file)) {
            for (String[] row : csvRows) {
                fw.write(escapeCsvRow(row));
                fw.write(System.lineSeparator());
            }
            fw.flush();
            JOptionPane.showMessageDialog(this, "Експорт завершено: " + file.getAbsolutePath(), "Успіх", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Помилка при записі файлу: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String[]> buildDepartmentOccupancyCsv() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Відділок", "Зайнято", "Максимум"});
        for (Department d : manager.getDepartments()) {
            rows.add(new String[]{d.getName(), String.valueOf(d.getCurrentOccupancy()), String.valueOf(d.getMaxCapacity())});
        }
        return rows;
    }

    private List<String[]> buildPatientsPerDoctorCsv() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Лікар", "Пацієнтів"});
        List<Doctor> docs = manager.getDoctors();
        for (Doctor d : docs) {
            long count = manager.getPatients().stream().filter(p -> p.getAttendingDoctor() == d).count();
            rows.add(new String[]{d.getFullName(), String.valueOf(count)});
        }
        return rows;
    }

    private List<String[]> buildDischargedPatientsCsv() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Пацієнт", "Відділок", "Дата виписки"});
        List<Patient> list = manager.getPatients().stream().filter(Patient::isDischarged).toList();
        for (Patient p : list) {
            rows.add(new String[]{p.getFullName(), (p.getDepartment() != null ? p.getDepartment().getName() : "Н/Д"), (p.getDischargeDate() != null ? p.getDischargeDate().toString() : "-")});
        }
        return rows;
    }

    private String escapeCsvRow(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsvField(row[i]));
        }
        return sb.toString();
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        boolean mustQuote = field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r");
        String escaped = field.replace("\"", "\"\"");
        if (mustQuote) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
}
