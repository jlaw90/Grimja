/*
 * Created by JFormDesigner on Sat Mar 23 12:27:44 GMT 2013
 */

package com.sqrt4.grimedi.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author James Lawrence
 */
public class BusyDialog extends JDialog {
    private boolean canCancel;
    private Action cancelCallback;

    public BusyDialog(Window owner) {
        super(owner);
        initComponents();
    }

    public BusyDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    public void setMessage(String msg) {
        label1.setText(msg);
        pack();
    }

    public void setCancelCallback(Action callback) {
        this.cancelCallback = callback;
    }

    public void setCancellable(boolean b) {
        canCancel = b;
        cancelAction.setEnabled(canCancel);
        cancelButton.setVisible(b);
        pack();
    }

    private void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && cancelCallback != null)
            cancelAction.actionPerformed(null);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        label1 = new JLabel();
        progressBar1 = new JProgressBar();
        cancelButton = new JButton();
        cancelAction = new CancelAction();

        //======== this ========
        setTitle("Please Wait");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setType(Window.Type.POPUP);
        setModal(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                BusyDialog.this.keyPressed(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {1.0, 1.0, 0.0, 1.0E-4};

        //---- label1 ----
        label1.setText("text");
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(5, 5, 5, 5), 0, 0));

        //---- progressBar1 ----
        progressBar1.setIndeterminate(true);
        contentPane.add(progressBar1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(5, 5, 5, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("text");
        cancelButton.setAction(cancelAction);
        contentPane.add(cancelButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JLabel label1;
    private JProgressBar progressBar1;
    private JButton cancelButton;
    private CancelAction cancelAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class CancelAction extends AbstractAction {
        private CancelAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Cancel");
            putValue(SHORT_DESCRIPTION, "Cancel this operation");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (cancelCallback != null)
                cancelCallback.actionPerformed(new ActionEvent(this, 0, "cancel"));
        }
    }
}
