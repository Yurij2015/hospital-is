package app;

import service.HospitalManagement;
import view.form.DepartmentsPanelForm;
import view.form.PatientPanelForm;
import view.form.DoctorPanelForm;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HospitalApp extends JFrame {

    public HospitalApp() {
        super("Система Управління Лікарнею");
        HospitalManagement manager = new HospitalManagement();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        DepartmentsPanelForm departmentsPanel = new DepartmentsPanelForm(manager);
        tabbedPane.addTab("Відділки", departmentsPanel);

        DoctorPanelForm doctorPanelForm = new DoctorPanelForm(manager);
        tabbedPane.addTab("Лікарі", doctorPanelForm);

        PatientPanelForm patientPanel = new PatientPanelForm(manager);
        tabbedPane.addTab("Пацієнти", patientPanel);

        tabbedPane.addTab("Звіти", createReportsPanel());

        add(tabbedPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Збереження даних...");
            }
        });
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Панель Звітів (Тут будуть звіти про завантаженість)", SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HospitalApp().setVisible(true);
        });
    }
}