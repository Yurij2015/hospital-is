package view.form;

import service.HospitalManagement;
import view.table.PatientTableModel;
import model.Patient;
import model.Department;
import model.Doctor;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PatientPanelForm extends JPanel {

    // --- Компоненти ---
    private final JTable patientsTable;
    private final JTextField searchField;

    // --- Поля даних ---
    private final HospitalManagement manager;
    private final PatientTableModel tableModel;

    public PatientPanelForm(HospitalManagement manager) {
        this.manager = manager;

        // 1. Ініціалізація компонентів
        patientsTable = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(patientsTable);

        // Додати/Прийняти пацієнта
        JButton admitButton = new JButton("Прийняти Пацієнта");
        // Виписати пацієнта
        JButton dischargeButton = new JButton("Виписати");
        JButton editButton = new JButton("Редагувати");
        JButton importButton = new JButton("Імпорт з CSV");
        JButton exportButton = new JButton("Експорт в CSV");
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Пошук");

        // 2. Налаштування компонування
        this.setLayout(new BorderLayout(5, 5));

        // Панель для пошуку (NORTH)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Пошук (ПІБ, Діагноз):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Панель для основних кнопок (SOUTH)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(admitButton);
        buttonPanel.add(dischargeButton);
        buttonPanel.add(editButton);
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);

        // Розміщення на головній панелі
        this.add(searchPanel, BorderLayout.NORTH); // Пошук зверху
        this.add(tableScrollPane, BorderLayout.CENTER); // Таблиця по центру
        this.add(buttonPanel, BorderLayout.SOUTH);      // Кнопки знизу

        // 3. Підключення даних та логіки
        tableModel = new PatientTableModel(manager.getPatients());
        patientsTable.setModel(tableModel);

        // Додамо сортер, щоб можна було сортувати/фільтрувати в майбутньому
        TableRowSorter<PatientTableModel> sorter = new TableRowSorter<>(tableModel);
        patientsTable.setRowSorter(sorter);

        // Додавання обробників
        admitButton.addActionListener(e -> admitNewPatient());
        dischargeButton.addActionListener(e -> dischargePatient());
        searchButton.addActionListener(e -> searchPatients());
        editButton.addActionListener(e -> editPatient());
        importButton.addActionListener(e -> importPatientsFromCsv());
        exportButton.addActionListener(e -> exportPatientsToCsv());

        refreshTableData();
    }

    public void refreshTableData() {
        tableModel.setData(manager.getPatients());
    }

    // Export visible patients (from tableModel) to CSV
    private void exportPatientsToCsv() {
        List<Patient> rows = tableModel.getPatients();
        if (rows == null || rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Немає даних для експорту.", "Інформація", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Зберегти пацієнтів як CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        int rc = chooser.showSaveDialog(this);
        if (rc != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
        if (file.exists()) {
            int resp = JOptionPane.showConfirmDialog(this, "Файл уже існує. Перезаписати?", "Підтвердження", JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) return;
        }

        try (FileWriter fw = new FileWriter(file)) {
            // header
            fw.write(escapeCsvRow(new String[]{"Пацієнт", "Діагноз", "Відділок", "Лікар", "Дата прийому", "Дата виписки"}));
            fw.write(System.lineSeparator());
            for (Patient p : rows) {
                String name = p.getFullName();
                String diag = p.getDiagnosis();
                String dept = p.getDepartment() != null ? p.getDepartment().getName() : "Н/Д";
                String doc = p.getAttendingDoctor() != null ? p.getAttendingDoctor().getFullName() : "Н/Д";
                String adm = p.getAdmissionDate() != null ? p.getAdmissionDate().toString() : "";
                String dis = p.getDischargeDate() != null ? p.getDischargeDate().toString() : "";
                fw.write(escapeCsvRow(new String[]{name, diag, dept, doc, adm, dis}));
                fw.write(System.lineSeparator());
            }
            fw.flush();
            JOptionPane.showMessageDialog(this, "Експорт завершено: " + file.getAbsolutePath(), "Успіх", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Помилка запису файлу: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
        }
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
        if (mustQuote) return '"' + escaped + '"';
        return escaped;
    }

    private void admitNewPatient() {
        // Простий діалог для додавання пацієнта: ПІБ і діагноз, обираємо відділок і лікаря за індексом
        JTextField lastNameField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField patronymicField = new JTextField();
        JTextField diagnosisField = new JTextField();

        List<Department> depts = manager.getDepartments();
        String[] deptNames = depts.stream().map(Department::getName).toArray(String[]::new);

        List<Doctor> docs = manager.getDoctors();
        String[] docNames = docs.stream().map(Doctor::getFullName).toArray(String[]::new);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Прізвище:")); panel.add(lastNameField);
        panel.add(new JLabel("Ім'я:")); panel.add(firstNameField);
        panel.add(new JLabel("По батькові:")); panel.add(patronymicField);
        panel.add(new JLabel("Діагноз:")); panel.add(diagnosisField);
        panel.add(new JLabel("Відділок:"));
        JComboBox<String> deptCombo = new JComboBox<>(deptNames);
        panel.add(deptCombo);
        panel.add(new JLabel("Лікар:"));
        JComboBox<String> docCombo = new JComboBox<>(docNames);
        panel.add(docCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Прийом нового пацієнта", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int deptIndex = deptCombo.getSelectedIndex();
            int docIndex = docCombo.getSelectedIndex();
            if (deptIndex < 0 || docIndex < 0) {
                JOptionPane.showMessageDialog(this, "Оберіть відділок та лікаря.", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Department dept = depts.get(deptIndex);
            Doctor doc = docs.get(docIndex);
            Patient p = new Patient(lastNameField.getText().trim(), firstNameField.getText().trim(), patronymicField.getText().trim(), 0,
                    diagnosisField.getText().trim(), dept, doc);
            try {
                manager.admitPatient(p);
                JOptionPane.showMessageDialog(this, "Пацієнта додано.");
                refreshTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Не вдалося додати пацієнта: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void dischargePatient() {
        int viewRow = patientsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Оберіть пацієнта для виписки.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = patientsTable.convertRowIndexToModel(viewRow);

        Patient pat = tableModel.getPatients().get(modelRow);
        if (pat.isDischarged()) {
            JOptionPane.showMessageDialog(this, "Пацієнт вже виписаний.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Виписати пацієнта " + pat.getFullName() + "?",
                "Підтвердження виписки", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.dischargePatient(pat);
                JOptionPane.showMessageDialog(this, "Пацієнт виписаний.");
                refreshTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Не вдалося виписати пацієнта: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchPatients() {
        String query = searchField.getText().trim();
        List<Patient> result = manager.searchPatients(query);
        tableModel.setData(result);
    }

    private void editPatient() {
        int viewRow = patientsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Оберіть пацієнта для редагування.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = patientsTable.convertRowIndexToModel(viewRow);
        Patient pat = tableModel.getPatients().get(modelRow);

        // Дозволимо змінити лише лікуючого лікаря для простоти
        List<Doctor> docs = manager.getDoctors();
        String[] docNames = docs.stream().map(Doctor::getFullName).toArray(String[]::new);
        JComboBox<String> docCombo = new JComboBox<>(docNames);
        int current = docs.indexOf(pat.getAttendingDoctor());
        if (current >= 0) docCombo.setSelectedIndex(current);

        int res = JOptionPane.showConfirmDialog(this, docCombo, "Змінити лікуючого лікаря", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            int idx = docCombo.getSelectedIndex();
            if (idx >= 0) {
                pat.setAttendingDoctor(docs.get(idx));
                JOptionPane.showMessageDialog(this, "Лікаря змінено.");
                refreshTableData();
            }
        }
    }

    // Import patients from CSV. Expected columns: lastName,firstName,patronymic,diagnosis,departmentName,doctorFullName
    private void importPatientsFromCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Імпорт пацієнтів з CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        List<String> errors = new ArrayList<>();
        int added = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (first) { first = false; if (line.toLowerCase().contains("прізвище") || line.toLowerCase().contains("last")) continue; }
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 6) { errors.add("Неправильний рядок: " + line); continue; }
                String ln = fields.get(0).trim();
                String fn = fields.get(1).trim();
                String pat = fields.get(2).trim();
                String diag = fields.get(3).trim();
                String deptName = fields.get(4).trim();
                String docName = fields.get(5).trim();

                Department dept = manager.getDepartments().stream().filter(d -> d.getName().equalsIgnoreCase(deptName)).findFirst().orElse(null);
                Doctor doc = manager.getDoctors().stream().filter(dd -> dd.getFullName().equalsIgnoreCase(docName)).findFirst().orElse(null);
                if (dept == null) { errors.add("Відділок не знайдено: " + deptName + " для рядка: " + line); continue; }
                if (doc == null) { errors.add("Лікаря не знайдено: " + docName + " для рядка: " + line); continue; }
                Patient p = new Patient(ln, fn, pat, 0, diag, dept, doc);
                try {
                    manager.admitPatient(p);
                    added++;
                } catch (Exception ex) {
                    errors.add("Не вдалося додати: " + ln + " - " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Помилка читання файлу: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshTableData();
        StringBuilder msg = new StringBuilder();
        msg.append("Додано пацієнтів: ").append(added).append("\n");
        if (!errors.isEmpty()) { msg.append("Помилки:\n"); for (String er : errors) msg.append(er).append("\n"); }
        JOptionPane.showMessageDialog(this, msg.toString(), "Імпорт завершено", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; } else { inQuotes = false; }
                } else { cur.append(c); }
            } else {
                if (c == '"') { inQuotes = true; } else if (c == ',') { out.add(cur.toString()); cur.setLength(0); } else { cur.append(c); }
            }
        }
        out.add(cur.toString());
        return out;
    }
}