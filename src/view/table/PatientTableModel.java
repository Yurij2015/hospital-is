package view.table;

import model.Patient;
import model.Department; // Потрібен для безпечного доступу до назви відділка
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.Objects; // Потрібен для безпечної перевірки на null

public class PatientTableModel extends AbstractTableModel {

    private List<Patient> patients;
    // Формат для відображення дати
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // ПІБ, Діагноз, Відділок, Лікар, Дата Прийому, Дата Виписки
    private final String[] columnNames = {"ПІБ", "Діагноз", "Відділок", "Лікар", "Прийом", "Виписка"};

    public PatientTableModel(List<Patient> patients) {
        this.patients = patients;
    }

    public void setData(List<Patient> newPatients) {
        this.patients = newPatients;
        fireTableDataChanged();
    }

    public List<Patient> getPatients() {
        return patients;
    }

    @Override
    public int getRowCount() {
        return patients.size();
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
        Patient pat = patients.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> pat.getFullName();
            case 1 -> pat.getDiagnosis();
            case 2 -> {
                // Безпечний доступ до назви відділка
                Department dept = pat.getDepartment();
                yield (dept != null) ? dept.getName() : "Н/Д";
            }
            case 3 ->
                // Безпечний доступ до імені лікаря
                    (pat.getAttendingDoctor() != null) ? pat.getAttendingDoctor().getFullName() : "Н/Д";
            case 4 ->
                // Форматування дати прийому
                    pat.getAdmissionDate().format(DATE_FORMAT);
            case 5 ->
                // Форматування дати виписки (якщо вона існує)
                    (pat.getDischargeDate() != null) ? pat.getDischargeDate().format(DATE_FORMAT) : "На лікуванні";
            default -> null;
        };
    }
}