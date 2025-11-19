package view.form;

import service.HospitalManagement;
import view.table.DoctorTableModel;
import model.Doctor;
import model.Department; // Потрібен для роботи з відділками

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

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

        // 2. Налаштування компонування
        this.setLayout(new BorderLayout(5, 5)); // Встановлення BorderLayout для JPanel

        // Панель для кнопок (SOUTH)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(assignDepartmentButton);

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
}