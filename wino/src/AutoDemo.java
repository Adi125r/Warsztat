import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.sf.clipsrules.jni.*;

public class AutoDemo implements ActionListener {
    JFrame jfrm;

    DefaultTableModel autoList;

    String ticket = "";
    JLabel jlab;



    Environment clips;

    boolean isExecuting = false;
    Thread executionThread;

    class WeightCellRenderer extends JProgressBar implements TableCellRenderer {
        public WeightCellRenderer() {
            super(JProgressBar.HORIZONTAL, 0, 100);
            setStringPainted(false);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            setValue(((Number) value).intValue());
            return WeightCellRenderer.this;
        }
    }

    /************/
    /* WineDemo */
    /***********/
    AutoDemo() throws  FileNotFoundException {



        /* =================================== */
        /* Create a new JFrame container and */
        /* assign a layout manager to it. */
        /* =================================== */

        this.jfrm = new JFrame("AutoDemo");
        this.jfrm.getContentPane().setLayout(
                new BoxLayout(this.jfrm.getContentPane(), BoxLayout.Y_AXIS));

        /* ================================= */
        /* Give the frame an initial size. */
        /* ================================= */

        this.jfrm.setSize(480, 390);

        /* ============================================================= */
        /* Terminate the program when the user closes the application. */
        /* ============================================================= */

        this.jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        /* ============================================== */
        /* Create a panel including the preferences and */
        /* meal panels and add it to the content pane. */
        /* ============================================== */

        final JPanel choicesPanel = new JPanel();
        choicesPanel.setLayout(new FlowLayout());
        JTextArea text = new JTextArea("",5,30);
        choicesPanel.add(new JScrollPane(text));

        JButton buttText = new JButton("Submit");
        choicesPanel.add(new JScrollPane(buttText));



        buttText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticket = text.getText();
                System.out.println(ticket);
                try {
                    runAuto();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        this.jfrm.getContentPane().add(choicesPanel);

        /* ================================== */
        /* Create the recommendation panel. */
        /* ================================== */

        this.autoList = new DefaultTableModel();

        this.autoList.setDataVector(new Object[][] {},
                new Object[] { "WineTitle",
                        "RecommendationTitle" });

        final JTable table = new JTable(this.autoList) {
            public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
            }
        };

        table.setCellSelectionEnabled(false);

        final WeightCellRenderer renderer = this.new WeightCellRenderer();
        renderer.setBackground(table.getBackground());

        table.getColumnModel().getColumn(1).setCellRenderer(renderer);

        final JScrollPane pane = new JScrollPane(table);

        table.setPreferredScrollableViewportSize(new Dimension(450, 210));

        /* =================================================== */
        /* Add the recommendation panel to the content pane. */
        /* =================================================== */

        this.jfrm.getContentPane().add(pane);


        /* ======================== */
        /* Load the wine program. */
        /* ======================== */

        this.clips = new Environment();

        this.clips.load("winedemo.clp") ;

        try {
            runAuto();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* ==================== */
        /* Display the frame. */
        /* ==================== */

        this.jfrm.pack();
        this.jfrm.setVisible(true);
    }



    /* ######################## */
    /* ActionListener Methods */
    /* ######################## */

    /*******************/
    /* actionPerformed */
    /*******************/
    public void actionPerformed(ActionEvent ae) {
        if (this.clips == null)
            return;

        try {
            runAuto();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /***********/
    /* runWine */
    /***********/
    private void runAuto() throws Exception {
        String item;

        if (this.isExecuting)
            return;

        this.clips.reset();


        if (ticket.contains("piszczy")) {
            System.out.println("1");
            this.clips.assertString("(attribute (name preferred-color) (value piszczy))");
        } else if (ticket.contains("zuzyte")) {
            this.clips.assertString("(attribute (name preferred-color) (value zuzyte))");
        } else if (ticket.contains("ladowanie")) {
            this.clips.assertString("(attribute (name preferred-color) (value ladowanie))");
        }else {
            System.out.println("b");
            this.clips.assertString("(attribute (name preferred-color) (value unknown))");
        }


        if (ticket.contains("stuka")) {
            System.out.println("2");
            this.clips.assertString("(attribute (name preferred-body) (value stuka))");
        } else if (ticket.contains("cisnienie")) {
            this.clips.assertString("(attribute (name preferred-body) (value cisnienie))");
        } else if (ticket.contains("swieci")) {
            this.clips.assertString("(attribute (name preferred-body) (value swieci))");
        } else {
            System.out.println("b");
            this.clips.assertString("(attribute (name preferred-body) (value unknown))");
        }


        if (ticket.contains("obroty")) {
            System.out.println("3");
            this.clips.assertString("(attribute (name preferred-sweetness) (value obroty))");
        } else if (ticket.contains("sezon")) {
            this.clips.assertString("(attribute (name preferred-sweetness) (value sezon))");
        } else if (ticket.contains("kreci")) {
            this.clips.assertString("(attribute (name preferred-sweetness) (value kreci))");
        } else {
            System.out.println("b");
            this.clips.assertString("(attribute (name preferred-sweetness) (value unknown))");
        }


        final Runnable runThread = new Runnable() {
            public void run() {
                clips.run();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            updateAuto();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        this.isExecuting = true;

        this.executionThread = new Thread(runThread);

        this.executionThread.start();
    }

    /***************/
    /* updateWines */
    /***************/
    // It isn't necessary to explicitly throw the ClassCastException,
    // but I wrote it to make clear that the castings might not always be right.
    // It depends on the template declarations, which in this case match with the expected value types.
    private void updateAuto() throws ClassCastException{
        final String evalStr = "(Auto::get-list)";
        final MultifieldValue pv = (MultifieldValue) this.clips.eval(evalStr);
       System.out.println(pv.size());
        this.autoList.setRowCount(0);
        int  procent =0;
        String kto ="";
        try {
            for (int i = 0; i < pv.size(); i++) {
                final FactAddressValue fv = (FactAddressValue) pv.get(i);
                final int certainty;
                certainty = (int) ((FloatValue) fv.getFactSlot("certainty")).floatValue();
                System.out.println(certainty);

                    kto  = ((StringValue) fv.getFactSlot("value")).stringValue();
                    procent = certainty;
                    this.autoList.addRow(new Object[] {kto , new Integer(procent) });


            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.jfrm.pack();

        this.executionThread = null;

        this.isExecuting = false;
    }

    /********/
    /* main */
    /********/
    public static void main(String args[]) {
        /* =================================================== */
        /* Create the frame on the event dispatching thread. */
        /* =================================================== */

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new AutoDemo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
