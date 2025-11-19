package view.form;

import service.HospitalManagement;
import view.table.PatientTableModel;
import model.Patient;
import model.Department;
import model.Doctor;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
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

        refreshTableData();
    }

    public void refreshTableData() {
        tableModel.setData(manager.getPatients());
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
}