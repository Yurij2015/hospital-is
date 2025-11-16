package view.form;

import service.HospitalManagement;
import view.table.DepartmentTableModel;
import model.Department;

import javax.swing.*;
import java.awt.*;

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

        this.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        this.add(tableScrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        tableModel = new DepartmentTableModel(manager.getDepartments());
        departmentsTable.setModel(tableModel);

        addButton.addActionListener(e -> addNewDepartment());
        editButton.addActionListener(e -> editSelectedDepartment());
        deleteButton.addActionListener(e -> deleteSelectedDepartment());

        refreshTableData();
    }

    public void refreshTableData() {
        tableModel.setData(manager.getDepartments());
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
}