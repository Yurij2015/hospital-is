package view.form;

import service.HospitalManagement;
import view.table.DoctorTableModel;
import model.Doctor;
import model.Department; // Потрібен для роботи з відділками

import javax.swing.*;
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

        // 4. Додавання обробників подій (поки що заглушки)
        addButton.addActionListener(e -> addNewDoctor());
        editButton.addActionListener(e -> editSelectedDoctor());
        deleteButton.addActionListener(e -> deleteSelectedDoctor());
        assignDepartmentButton.addActionListener(e -> assignDepartment());

        refreshTableData();
    }


    public void refreshTableData() {
        tableModel.setData(manager.getDoctors());
    }

    // =========================================================
    // ЛОГІКА КНОПОК
    // =========================================================

    private void addNewDoctor() {
        // TODO: Тут буде виклик AddDoctorDialog.showDialog()
        JOptionPane.showMessageDialog(this, "Функціонал: Додати Лікаря");
    }

    private void editSelectedDoctor() {
        // TODO: Тут буде логіка редагування лікаря
        JOptionPane.showMessageDialog(this, "Функціонал: Редагувати Лікаря");
    }

    private void deleteSelectedDoctor() {
        // TODO: Тут буде логіка видалення лікаря (з перевіркою на закріплених пацієнтів)
        JOptionPane.showMessageDialog(this, "Функціонал: Видалити Лікаря");
    }

    private void assignDepartment() {
        // TODO: Тут буде логіка для призначення відділку (п. 2.5)
        JOptionPane.showMessageDialog(this, "Функціонал: Призначити Відділок");
    }
}