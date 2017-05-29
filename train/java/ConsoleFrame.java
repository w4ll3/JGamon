import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;


/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */

public class ConsoleFrame extends JFrame {

    private ConsoleApp app;

    private PrintWriter logWriter;

    public ConsoleFrame(ConsoleApp app) {
        super(app.getName());
        try {
            this.app = app;
            jbInit();
            logWriter = new PrintWriter(new LogWriter());
            logWriter.println("Welcome to " + app.getName());
            app.setLog(logWriter);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private BorderLayout borderLayout1 = new BorderLayout();
    private JTextField jTextField = new JTextField();
    private JTextArea jTextArea = new JTextArea();
    private JScrollPane jScrollPane = new JScrollPane();

    private void jbInit() throws Exception {
        getContentPane().setLayout(borderLayout1);
        setSize(400, 300);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.getContentPane().add(jScrollPane, java.awt.BorderLayout.CENTER);
        this.getContentPane().add(jTextField, java.awt.BorderLayout.SOUTH);
        jScrollPane.getViewport().add(jTextArea);

        jTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                command(jTextField.getText());
            }
        });
        jTextArea.setEditable(false);
        jTextField.selectAll();
        jTextArea.setFocusable(false);
    }

    void command(String command) {

        jTextField.setText("");

        String args[] = command.split(" +", 2);

        try {
            if (command.equals("cls")) {
                jTextArea.getDocument().remove(0, jTextArea.getDocument().getLength());
            } else

            if (args[0].equals("incl")) {
                BufferedReader br = new BufferedReader(new FileReader(args[1]));
                String line = br.readLine();;
                do {
                    command(line);
                    line = br.readLine();
                } while(line != null);
                br.close();
            } else

            {
                if (app.command(command)) {
                    logWriter.println(" > " + command);
                } else {
                    logWriter.println("Unknown command: " + command);
                }
            }
        } catch (Exception ex) {
            logWriter.println("Error during " + command + ":");
            ex.printStackTrace(logWriter);
        }

    }

    private class LogWriter extends Writer {
        public void close() {
        }

        public void flush() {
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            Document doc = jTextArea.getDocument();
            try {
                String s = new String(cbuf, off, len);
                doc.insertString(doc.getLength(), s, null);
                JScrollBar b = jScrollPane.getVerticalScrollBar();
                b.setValue(b.getMaximum());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }
}
