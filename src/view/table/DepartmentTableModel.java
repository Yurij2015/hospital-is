package view.table;

import model.Department;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class DepartmentTableModel extends AbstractTableModel {

    private List<Department> departments;
    private final String[] columnNames = {"Назва", "Макс. Ліжок", "Зайнято", "% Зайнятості"};

    public DepartmentTableModel(List<Department> departments) {
        this.departments = departments;
    }

    // Метод для оновлення даних, викликається після змін у service.HospitalManagement
    public void setData(List<Department> newDepartments) {
        this.departments = newDepartments;
        fireTableDataChanged(); // Повідомляє JTable про необхідність перемалюватись
    }

    @Override
    public int getRowCount() {
        return departments.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Department dept = departments.get(rowIndex);

        // Визначаємо, які дані повернути для конкретної колонки
        return switch (columnIndex) {
            case 0 -> dept.getName();
            case 1 -> dept.getMaxCapacity();
            case 2 -> dept.getCurrentOccupancy();
            case 3 -> {
                double occupancy = (double) dept.getCurrentOccupancy() / dept.getMaxCapacity();
                yield String.format("%.1f%%", occupancy * 100);
            }
            default -> null;
        };
    }
}