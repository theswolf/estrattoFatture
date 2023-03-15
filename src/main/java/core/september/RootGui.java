package core.september;

import com.formdev.flatlaf.FlatLightLaf;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QPDecoderStream;
import core.september.menu.MenuHandler;
import core.september.util.PDFData;
import core.september.util.PDFRunnable;
import core.september.util.PdfHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class RootGui extends JFrame {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel selectedFileLabel;
    private JButton readButton;
    private JList fileList;
    private JTable tableResult;

    private File file;

    public RootGui() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);



        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        new MenuHandler<>(this,(option,filechooser) -> {
            if(option != JFileChooser.CANCEL_OPTION) {
                fileList.setModel(new DefaultListModel());
                file = filechooser.getSelectedFile();
                selectedFileLabel.setText(file.getAbsolutePath());
                Set<String> filenames = Arrays.asList(file.listFiles())
                        .stream().filter(File::isFile)
                        .map(File::getAbsolutePath)
                        .collect(Collectors.toSet());

                DefaultListModel<String> listModel = new DefaultListModel<>();
                filenames.stream().sorted().forEach(fn -> listModel.addElement(fn));
                fileList.setModel(listModel);
            }
        });

        readButton.addActionListener((a) -> {
            if(file != null) {
                PDFRunnable pdfRunnable = new PDFRunnable(file,tableResult);
                Thread t = new Thread(pdfRunnable);
                t.start();


            }
        });

    }

    private void onOK() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fileChooser.showOpenDialog(RootGui.this);
                if(option != JFileChooser.CANCEL_OPTION) {
                    File dest = fileChooser.getSelectedFile();
                    PdfHandler pdfHandler = new PdfHandler(dest.getAbsolutePath(), tableResult.getModel(),PDFData.columns);
                    new Thread(pdfHandler).start();
                }
            }
        }).start();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }



    public static void main(String[] args) {
        /* Use an appropriate Look and Feel */
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Fatture");

            UIManager.setLookAndFeel( new FlatLightLaf() );
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use bold fonts */

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                RootGui dialog = new RootGui();
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }


    private void createUIComponents() {
        tableResult = new JTable(PDFData.emptySet(PDFData.columns),PDFData.columns);
        tableResult.setDefaultRenderer(Object.class, new TableCellRenderer(){
            private DefaultTableCellRenderer DEFAULT_RENDERER =  new DefaultTableCellRenderer();
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


                if (row%2 == 0){
                    c.setBackground(Color.WHITE);
                }
                else {
                    c.setBackground(Color.LIGHT_GRAY);
                }

                if(row == table.getRowCount() -1 ) {
                    c.setBackground(Color.YELLOW);
                }


                return c;
            }

        });
    }
}
