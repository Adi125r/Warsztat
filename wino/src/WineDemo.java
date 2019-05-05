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

/* TBD module qualifier with find-all-facts */

/*

 Notes:

 This example creates just a single environment. If you create multiple environments,
 call the destroy method when you no longer need the environment. This will free the
 C data structures associated with the environment.

 clips = new Environment();
 .
 .
 .
 clips.destroy();

 Calling the clear, reset, load, loadFacts, run, eval, build, assertString,
 and makeInstance methods can trigger CLIPS garbage collection. If you need
 to retain access to a PrimitiveValue returned by a prior eval, assertString,
 or makeInstance call, retain it and then release it after the call is made.

 PrimitiveValue pv1 = clips.eval("(myFunction foo)");
 pv1.retain();
 PrimitiveValue pv2 = clips.eval("(myFunction bar)");
 .
 .
 .
 pv1.release();

 */

public class WineDemo implements ActionListener {
    JFrame jfrm;

    DefaultTableModel wineList;



    JLabel jlab;




    ResourceBundle wineResources;

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
    WineDemo() throws  FileNotFoundException {
        try {
            this.wineResources = ResourceBundle.getBundle("properties.WineResources",
                    Locale.getDefault());
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            return;
        }


        /* =================================== */
        /* Create a new JFrame container and */
        /* assign a layout manager to it. */
        /* =================================== */

        this.jfrm = new JFrame(wineResources.getString("WineDemo"));
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


        this.jfrm.getContentPane().add(choicesPanel);

        /* ================================== */
        /* Create the recommendation panel. */
        /* ================================== */

        this.wineList = new DefaultTableModel();

        this.wineList.setDataVector(new Object[][] {},
                new Object[] { this.wineResources.getString("WineTitle"),
                        this.wineResources.getString("RecommendationTitle") });

        final JTable table = new JTable(this.wineList) {
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
            runWine();
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
            runWine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***********/
    /* runWine */
    /***********/
    private void runWine() throws Exception {
        String item;

        if (this.isExecuting)
            return;

        this.clips.reset();

        item = "Red";

        if (item.equals("Red")) {
            this.clips.assertString("(attribute (name preferred-color) (value red))");
        } else if (item.equals("White")) {
            this.clips.assertString("(attribute (name preferred-color) (value white))");
        } else {
            this.clips.assertString("(attribute (name preferred-color) (value unknown))");
        }

        item = "";
        if (item.equals("Light")) {
            this.clips.assertString("(attribute (name preferred-body) (value light))");
        } else if (item.equals("Medium")) {
            this.clips.assertString("(attribute (name preferred-body) (value medium))");
        } else if (item.equals("Full")) {
            this.clips.assertString("(attribute (name preferred-body) (value full))");
        } else {
            this.clips.assertString("(attribute (name preferred-body) (value unknown))");
        }

        item = "";
        if (item.equals("Dry")) {
            this.clips.assertString("(attribute (name preferred-sweetness) (value dry))");
        } else if (item.equals("Medium")) {
            this.clips.assertString("(attribute (name preferred-sweetness) (value medium))");
        } else if (item.equals("Sweet")) {
            this.clips.assertString("(attribute (name preferred-sweetness) (value sweet))");
        } else {
            this.clips.assertString("(attribute (name preferred-sweetness) (value unknown))");
        }

        item ="";
        if (item.equals("Beef") || item.equals("Pork") || item.equals("Lamb")) {
            this.clips.assertString("(attribute (name main-component) (value meat))");
            this.clips.assertString("(attribute (name has-turkey) (value no))");
        } else if (item.equals("Turkey")) {
            this.clips.assertString("(attribute (name main-component) (value poultry))");
            this.clips.assertString("(attribute (name has-turkey) (value yes))");
        } else if (item.equals("Chicken") || item.equals("Duck")) {
            this.clips.assertString("(attribute (name main-component) (value poultry))");
            this.clips.assertString("(attribute (name has-turkey) (value no))");
        } else if (item.equals("Fish")) {
            this.clips.assertString("(attribute (name main-component) (value fish))");
            this.clips.assertString("(attribute (name has-turkey) (value no))");
        } else if (item.equals("Other")) {
            this.clips.assertString("(attribute (name main-component) (value unknown))");
            this.clips.assertString("(attribute (name has-turkey) (value no))");
        } else {
            this.clips.assertString("(attribute (name main-component) (value unknown))");
            this.clips.assertString("(attribute (name has-turkey) (value unknown))");
        }

        item = "";
        if (item.equals("None")) {
            this.clips.assertString("(attribute (name has-sauce) (value no))");
        } else if (item.equals("Spicy")) {
            this.clips.assertString("(attribute (name has-sauce) (value yes))");
            this.clips.assertString("(attribute (name sauce) (value spicy))");
        } else if (item.equals("Sweet")) {
            this.clips.assertString("(attribute (name has-sauce) (value yes))");
            this.clips.assertString("(attribute (name sauce) (value sweet))");
        } else if (item.equals("Cream")) {
            this.clips.assertString("(attribute (name has-sauce) (value yes))");
            this.clips.assertString("(attribute (name sauce) (value cream))");
        } else if (item.equals("Other")) {
            this.clips.assertString("(attribute (name has-sauce) (value yes))");
            this.clips.assertString("(attribute (name sauce) (value unknown))");
        } else {
            this.clips.assertString("(attribute (name has-sauce) (value unknown))");
            this.clips.assertString("(attribute (name sauce) (value unknown))");
        }

        item ="";
        if (item.equals("Delicate")) {
            this.clips.assertString("(attribute (name tastiness) (value delicate))");
        } else if (item.equals("Average")) {
            this.clips.assertString("(attribute (name tastiness) (value average))");
        } else if (item.equals("Strong")) {
            this.clips.assertString("(attribute (name tastiness) (value strong))");
        } else {
            this.clips.assertString("(attribute (name tastiness) (value unknown))");
        }

        final Runnable runThread = new Runnable() {
            public void run() {
                clips.run();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            updateWines();
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
    private void updateWines() throws ClassCastException{
        final String evalStr = "(WINES::get-wine-list)";
        final MultifieldValue pv = (MultifieldValue) this.clips.eval(evalStr);
        this.wineList.setRowCount(0);
        int  procent =0;
        String kto ="";
        try {
            for (int i = 0; i < pv.size(); i++) {
                final FactAddressValue fv = (FactAddressValue) pv.get(i);
                final int certainty;
                certainty = (int) ((FloatValue) fv.getFactSlot("certainty")).floatValue();
                if (procent< certainty) {
                    kto  = ((StringValue) fv.getFactSlot("value")).stringValue();
                    procent = certainty;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.wineList.addRow(new Object[] {kto , new Integer(procent) });

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
                    new WineDemo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
