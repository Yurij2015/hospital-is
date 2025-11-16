package app;

import service.HospitalManagement;
import view.form.DepartmentsPanelForm; // Необхідний імпорт
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HospitalApp extends JFrame {

    private HospitalManagement manager;
    private JTabbedPane tabbedPane;
    private DepartmentsPanelForm departmentsPanel; // <<< ВИПРАВЛЕННЯ

    public HospitalApp() {
        super("Система Управління Лікарнею");
        manager = new HospitalManagement(); // Ініціалізація контролера

        // Налаштування основного вікна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600); // Збільшений розмір для таблиць
        setLocationRelativeTo(null); // Вікно по центру екрана

        // Створення панелі з вкладками
        tabbedPane = new JTabbedPane();

        // Додавання вкладок

        tabbedPane = new JTabbedPane();

        // РЯДОК 28, де виникає помилка: тут змінна використовується, але не оголошена
        departmentsPanel = new DepartmentsPanelForm(manager);
        tabbedPane.addTab("Відділки", departmentsPanel);

        tabbedPane.addTab("Лікарі", createDoctorsPanel());
        tabbedPane.addTab("Пацієнти", createPatientsPanel());
        tabbedPane.addTab("Звіти", createReportsPanel());

        // Додавання панелі вкладок до вікна
        add(tabbedPane);

        // Додаємо обробник для збереження даних при закритті (для майбутньої серіалізації)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Тут має бути виклик методу manager.saveData()
                System.out.println("Збереження даних...");
            }
        });
    }

    /**
     * Методи-заглушки для створення вмісту вкладок
     */

    private JPanel createDoctorsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Панель Лікарів (Тут буде JTable)", SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Панель Пацієнтів (Тут буде JTable та кнопки Прийняти/Виписати)", SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Панель Звітів (Тут будуть звіти про завантаженість)", SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        // Виконання коду Swing в Event Dispatch Thread (рекомендовано)
        SwingUtilities.invokeLater(() -> {
            new HospitalApp().setVisible(true);
        });
    }
}