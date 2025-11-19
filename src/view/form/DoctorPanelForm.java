package view.form;

import service.HospitalManagement;
import view.table.DoctorTableModel;
import model.Doctor;
import model.Department; // Потрібен для роботи з відділками

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
import java.util.stream.Collectors;

public class DoctorPanelForm extends JPanel {

    private JPanel panel1;
    private JTable table1;

    // --- Поля контролера та моделі ---
    private final HospitalManagement manager;
    private final DoctorTableModel tableModel;

    public DoctorPanelForm(HospitalManagement manager) {
        this.manager = manager;

        // 1. Ініціалізація компонентів
        // --- Компоненти (Створюємо вручну) ---
        JTable doctorsTable = new JTable();
        // Обов'язково обгортаємо JTable в JScrollPane
        JScrollPane tableScrollPane = new JScrollPane(doctorsTable);

        JButton addButton = new JButton("Додати Лікаря");
        JButton editButton = new JButton("Редагувати");
        JButton deleteButton = new JButton("Видалити");
        // Кнопка для призначення відділків
        JButton assignDepartmentButton = new JButton("Призначити Відділок");
        JButton importButton = new JButton("Імпорт з CSV");
        JButton exportButton = new JButton("Експорт в CSV");

        // 2. Налаштування компонування
        this.setLayout(new BorderLayout(5, 5)); // Встановлення BorderLayout для JPanel

        // Панель для кнопок (SOUTH)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(assignDepartmentButton);
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);

        // Додавання компонентів до головної панелі (this)
        this.add(tableScrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // 3. Підключення даних
        tableModel = new DoctorTableModel(manager.getDoctors());
        doctorsTable.setModel(tableModel);

        // Додамо сортер
        TableRowSorter<DoctorTableModel> sorter = new TableRowSorter<>(tableModel);
        doctorsTable.setRowSorter(sorter);

        // 4. Додавання обробників подій
        addButton.addActionListener(e -> addNewDoctor());
        editButton.addActionListener(e -> editSelectedDoctor(doctorsTable));
        deleteButton.addActionListener(e -> deleteSelectedDoctor(doctorsTable));
        assignDepartmentButton.addActionListener(e -> assignDepartment(doctorsTable));
        importButton.addActionListener(e -> importDoctorsFromCsv());
        exportButton.addActionListener(e -> exportDoctorsToCsv());

        refreshTableData();
    }

    public void refreshTableData() {
        tableModel.setData(manager.getDoctors());
    }

    // =========================================================
    // ЛОГІКА КНОПОК
    // =========================================================

    private void addNewDoctor() {
        // Простий діалог для введення лікаря (прізвище/ім'я/по-батькові/рік/посада/фах/телефон)
        JTextField lastNameField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField patronymicField = new JTextField();
        JTextField birthYearField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField specializationField = new JTextField();
        JTextField phoneField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Прізвище:")); panel.add(lastNameField);
        panel.add(new JLabel("Ім'я:")); panel.add(firstNameField);
        panel.add(new JLabel("По батькові:")); panel.add(patronymicField);
        panel.add(new JLabel("Рік народження:")); panel.add(birthYearField);
        panel.add(new JLabel("Посада:")); panel.add(positionField);
        panel.add(new JLabel("Фах:")); panel.add(specializationField);
        panel.add(new JLabel("Телефон:")); panel.add(phoneField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Додати Лікаря", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int year = Integer.parseInt(birthYearField.getText().trim());
                Doctor d = new Doctor(lastNameField.getText().trim(), firstNameField.getText().trim(), patronymicField.getText().trim(),
                        year, positionField.getText().trim(), specializationField.getText().trim(), phoneField.getText().trim());
                manager.addDoctor(d);
                JOptionPane.showMessageDialog(this, "Лікаря додано.");
                refreshTableData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Невірний рік народження.", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedDoctor(JTable doctorsTable) {
        int viewRow = doctorsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Оберіть лікаря для редагування.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = doctorsTable.convertRowIndexToModel(viewRow);
        Doctor doc = tableModel.getDoctors().get(modelRow);

        JTextField positionField = new JTextField(doc.getPosition());
        JTextField specializationField = new JTextField(doc.getSpecialization());
        JTextField phoneField = new JTextField(doc.getPhoneNumber());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Посада:")); panel.add(positionField);
        panel.add(new JLabel("Фах:")); panel.add(specializationField);
        panel.add(new JLabel("Телефон:")); panel.add(phoneField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Редагувати Лікаря", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            manager.updateDoctor(doc, positionField.getText().trim(), specializationField.getText().trim(), phoneField.getText().trim());
            JOptionPane.showMessageDialog(this, "Дані лікаря оновлено.");
            refreshTableData();
        }
    }

    private void deleteSelectedDoctor(JTable doctorsTable) {
        int viewRow = doctorsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Оберіть лікаря для видалення.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = doctorsTable.convertRowIndexToModel(viewRow);
        Doctor doc = tableModel.getDoctors().get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this, "Видалити лікаря " + doc.getFullName() + "?", "Підтвердження", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.removeDoctor(doc);
                JOptionPane.showMessageDialog(this, "Лікаря видалено.");
                refreshTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Не вдалося видалити лікаря: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void assignDepartment(JTable doctorsTable) {
        int viewRow = doctorsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Оберіть лікаря для призначення відділку.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = doctorsTable.convertRowIndexToModel(viewRow);
        Doctor doc = tableModel.getDoctors().get(modelRow);

        List<Department> depts = manager.getDepartments();
        String[] names = depts.stream().map(Department::getName).toArray(String[]::new);
        JComboBox<String> combo = new JComboBox<>(names);
        int res = JOptionPane.showConfirmDialog(this, combo, "Призначити відділок", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            int idx = combo.getSelectedIndex();
            if (idx >= 0) {
                Department d = depts.get(idx);
                // Toggle assignment: якщо вже є - зняти, якщо немає - додати
                if (doc.getAffiliatedDepartments().contains(d)) {
                    doc.removeDepartment(d);
                    JOptionPane.showMessageDialog(this, "Відділок знято.");
                } else {
                    doc.addDepartment(d);
                    JOptionPane.showMessageDialog(this, "Відділок призначено.");
                }
                refreshTableData();
            }
        }
    }

    // Import doctors from CSV. Expected columns: lastName,firstName,patronymic,birthYear,position,specialization,phone
    private void importDoctorsFromCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Імпорт лікарів з CSV");
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
                if (fields.size() < 7) {
                    errors.add("Неправильний рядок: " + line);
                    continue;
                }
                String ln = fields.get(0).trim();
                String fn = fields.get(1).trim();
                String pat = fields.get(2).trim();
                String yearStr = fields.get(3).trim();
                String pos = fields.get(4).trim();
                String spec = fields.get(5).trim();
                String phone = fields.get(6).trim();
                try {
                    int year = Integer.parseInt(yearStr);
                    Doctor d = new Doctor(ln, fn, pat, year, pos, spec, phone);
                    manager.addDoctor(d);
                    added++;
                } catch (NumberFormatException ex) {
                    errors.add("Невірний рік для: " + ln + " " + fn + " -> " + yearStr);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Помилка читання файлу: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshTableData();
        StringBuilder msg = new StringBuilder();
        msg.append("Додано лікарів: ").append(added).append("\n");
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

    // Export doctors to CSV
    private void exportDoctorsToCsv() {
        List<Doctor> docs = tableModel.getDoctors();
        if (docs == null || docs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Немає даних для експорту.", "Інформація", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Зберегти лікарів як CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        int rc = chooser.showSaveDialog(this);
        if (rc != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
        if (file.exists()) {
            int resp = JOptionPane.showConfirmDialog(this, "Файл вже існує. Перезаписати?", "Підтвердження", JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) return;
        }

        try (FileWriter fw = new FileWriter(file)) {
            fw.write(escapeCsvRow(new String[]{"ПІБ", "Рік нар.", "Посада", "Фах", "Телефон", "Відділки"}));
            fw.write(System.lineSeparator());
            for (Doctor d : docs) {
                String aff = d.getAffiliatedDepartments().stream().map(Department::getName).collect(Collectors.joining(", "));
                fw.write(escapeCsvRow(new String[]{d.getFullName(), String.valueOf(d.getBirthYear()), d.getPosition(), d.getSpecialization(), d.getPhoneNumber(), aff}));
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
}