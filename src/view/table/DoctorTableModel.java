package view.table;

import model.Doctor;
import model.Department; // Потрібен для безпечного відображення назв відділків

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorTableModel extends AbstractTableModel {

    private List<Doctor> doctors;
    // ПІБ, Рік нар., Посада, Фах, Телефон, Відділки
    private final String[] columnNames = {"ПІБ", "Рік нар.", "Посада", "Фах", "Телефон", "Відділки"};

    public DoctorTableModel(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    // Метод, необхідний формі для отримання об'єкта Doctor при натисканні (редагування/видалення)
    public List<Doctor> getDoctors() {
        return doctors;
    }

    // Метод для оновлення даних у таблиці (викликається після додавання/редагування)
    public void setData(List<Doctor> newDoctors) {
        this.doctors = newDoctors;
        fireTableDataChanged(); // Повідомляє JTable, що дані змінилися
    }

    @Override
    public int getRowCount() {
        return doctors.size();
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
        Doctor doc = doctors.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> doc.getFullName();
            case 1 -> doc.getBirthYear();
            case 2 -> doc.getPosition();
            case 3 -> doc.getSpecialization();
            case 4 -> doc.getPhoneNumber();
            case 5 ->
                // Перетворюємо список відділків, за якими закріплений лікар, на рядок, розділений комою
                    doc.getAffiliatedDepartments().stream()
                            .map(Department::getName)
                            .collect(Collectors.joining(", "));
            default -> null;
        };
    }
}