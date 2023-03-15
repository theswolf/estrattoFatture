package core.september.menu;

import javax.swing.*;
import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MenuHandler<T extends JFrame> {

    JMenuBar menuBar = new JMenuBar();
    JMenu file = new JMenu("File");
    JMenuItem item = new JMenuItem("Open folder");


    public MenuHandler(T frame, BiConsumer<Integer,JFileChooser> callback) {
        file.add(item);
        menuBar.add(file);

        item.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser("/Users/christiangeymonat/MEGAsync/FATTURE");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(frame);
            callback.accept(option,fileChooser);
        });

        frame.setJMenuBar(menuBar);
    }
}
