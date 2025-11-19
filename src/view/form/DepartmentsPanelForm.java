package view.form;

import service.HospitalManagement;
import view.table.DepartmentTableModel;
import model.Department;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DepartmentsPanelForm extends JPanel {

    private final JTable departmentsTable;

    private final HospitalManagement manager;
    private final DepartmentTableModel tableModel;

    public DepartmentsPanelForm(HospitalManagement manager) {
        this.manager = manager;

        departmentsTable = new JTable();

        JScrollPane tableScrollPane = new JScrollPane(departmentsTable);

        JButton addButton = new JButton("Додати");
        JButton editButton = new JButton("Редагувати");
        JButton deleteButton = new JButton("Видалити");
        JButton importButton = new JButton("Імпорт з CSV");
        JButton exportButton = new JButton("Експорт в CSV");

        this.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);

        this.add(tableScrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        tableModel = new DepartmentTableModel(manager.getDepartments());
        departmentsTable.setModel(tableModel);

        addButton.addActionListener(e -> addNewDepartment());
        editButton.addActionListener(e -> editSelectedDepartment());
        deleteButton.addActionListener(e -> deleteSelectedDepartment());
        importButton.addActionListener(e -> importDepartmentsFromCsv());
        exportButton.addActionListener(e -> exportDepartmentsToCsv());

        refreshTableData();
    }

    public void refreshTableData() {
        tableModel.setData(manager.getDepartments());
    }

    private void exportDepartmentsToCsv() {
        List<Department> depts = tableModel.getDepartments();
        if (depts == null || depts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Немає даних для експорту.", "Інформація", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Зберегти відділки як CSV");
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
            fw.write(escapeCsvRow(new String[]{"Назва", "Макс. Ліжок", "Зайнято", "% Зайнятості"}));
            fw.write(System.lineSeparator());
            for (Department d : depts) {
                String pct = d.getMaxCapacity() > 0 ? String.format("%.1f%%", (double) d.getCurrentOccupancy() / d.getMaxCapacity() * 100) : "0.0%";
                fw.write(escapeCsvRow(new String[]{d.getName(), String.valueOf(d.getMaxCapacity()), String.valueOf(d.getCurrentOccupancy()), pct}));
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

    private void addNewDepartment() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);

        String name = JOptionPane.showInputDialog(owner, "Введіть назву відділку:", "Додати відділок", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        String capacityStr = JOptionPane.showInputDialog(owner, "Введіть макс. кількість ліжок:", "Додати відділок", JOptionPane.PLAIN_MESSAGE);
        try {
            int capacity = Integer.parseInt(capacityStr);
            Department newDept = new Department(name, capacity);
            manager.addDepartment(newDept);
            refreshTableData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(owner, "Кількість ліжок має бути числом.", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedDepartment() {
        int selectedRow = departmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Оберіть відділок для редагування.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Department selectedDept = tableModel.getDepartments().get(selectedRow);
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);

        String newName = JOptionPane.showInputDialog(owner, "Нова назва:", selectedDept.getName());
        if (newName == null || newName.trim().isEmpty()) return;

        String capacityStr = JOptionPane.showInputDialog(owner, "Нова макс. кількість ліжок:", String.valueOf(selectedDept.getMaxCapacity()));
        try {
            int newCapacity = Integer.parseInt(capacityStr);

            manager.updateDepartment(selectedDept, newName, newCapacity);

            refreshTableData();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(owner, "Кількість ліжок має бути числом.", "Помилка", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Помилка оновлення", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedDepartment() {
        int selectedRow = departmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Оберіть відділок для видалення.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Department selectedDept = tableModel.getDepartments().get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити відділок '" + selectedDept.getName() + "'?",
                "Підтвердження видалення",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.removeDepartment(selectedDept);
                refreshTableData();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importDepartmentsFromCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Імпорт відділок з CSV");
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
                if (first) {
                    first = false;
                    if (line.toLowerCase().contains("відділок") || line.toLowerCase().contains("назва") || line.contains("Зайнято")) {
                        continue;
                    }
                }
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 2) {
                    errors.add("Неправильний рядок: " + line);
                    continue;
                }
                String name = fields.get(0).trim();
                String capStr = fields.get(1).trim();
                try {
                    int cap = Integer.parseInt(capStr);
                    boolean exists = manager.getDepartments().stream().anyMatch(d -> d.getName().equalsIgnoreCase(name));
                    if (exists) {
                        errors.add("Відділок вже існує: " + name);
                        continue;
                    }
                    Department d = new Department(name, cap);
                    manager.addDepartment(d);
                    added++;
                } catch (NumberFormatException ex) {
                    errors.add("Невірна місткість для: " + name + " -> " + capStr);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Помилка читання файлу: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshTableData();
        StringBuilder msg = new StringBuilder();
        msg.append("Додано відділоків: ").append(added).append("\n");
        if (!errors.isEmpty()) {
            msg.append("Помилки:\n");
            for (String er : errors) msg.append(er).append("\n");
        }
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
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out;
    }
}