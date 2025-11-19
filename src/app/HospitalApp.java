package app;

import service.HospitalManagement;
import view.form.DepartmentsPanelForm;
import view.form.PatientPanelForm;
import view.form.DoctorPanelForm;
import view.form.ReportsPanelForm;
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

        JTabbedPane tabbedPane = getJTabbedPane(manager);

        add(tabbedPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Збереження даних...");
            }
        });
    }

    private static JTabbedPane getJTabbedPane(HospitalManagement manager) {
        JTabbedPane tabbedPane = new JTabbedPane();

        DepartmentsPanelForm departmentsPanel = new DepartmentsPanelForm(manager);
        tabbedPane.addTab("Відділки", departmentsPanel);

        DoctorPanelForm doctorPanelForm = new DoctorPanelForm(manager);
        tabbedPane.addTab("Лікарі", doctorPanelForm);

        PatientPanelForm patientPanel = new PatientPanelForm(manager);
        tabbedPane.addTab("Пацієнти", patientPanel);

        ReportsPanelForm reportsPanel = new ReportsPanelForm(manager);
        tabbedPane.addTab("Звіти", reportsPanel);
        return tabbedPane;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HospitalApp().setVisible(true);
        });
    }
}