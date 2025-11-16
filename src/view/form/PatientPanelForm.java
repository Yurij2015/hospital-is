package view.form;

import service.HospitalManagement;
import view.table.PatientTableModel;
import model.Patient;

import javax.swing.*;
import java.awt.*;

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

        // Додавання обробників
        admitButton.addActionListener(e -> admitNewPatient());
        dischargeButton.addActionListener(e -> dischargePatient());
        searchButton.addActionListener(e -> searchPatients());
        // TODO: Додати обробник для editButton

        refreshTableData();
    }

    public void refreshTableData() {
        // У майбутньому тут може бути: tableModel.setData(manager.searchPatients(null));
        tableModel.setData(manager.getPatients());
    }

    private void admitNewPatient() {
        // TODO: Виклик AdmitPatientDialog
        JOptionPane.showMessageDialog(this, "Функціонал прийому нового пацієнта");
    }

    private void dischargePatient() {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Оберіть пацієнта для виписки.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Patient pat = tableModel.getPatients().get(selectedRow);
        if (pat.isDischarged()) {
            JOptionPane.showMessageDialog(this, "Пацієнт вже виписаний.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Виписати пацієнта " + pat.getFullName() + "?",
                "Підтвердження виписки", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // TODO: Реалізувати метод manager.dischargePatient(pat);
            JOptionPane.showMessageDialog(this, "Пацієнт виписаний. Оновіть дані.");
            refreshTableData();
        }
    }

    private void searchPatients() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshTableData(); // Якщо порожньо, показуємо всіх
        } else {
            // TODO: Виклик manager.searchPatients(query);
            JOptionPane.showMessageDialog(this, "Пошук за запитом: " + query);
        }
    }
}