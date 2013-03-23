/*
 * Created by JFormDesigner on Fri Mar 15 22:29:38 GMT 2013
 */

package com.sqrt4.grimedi.ui;

import javax.swing.event.*;
import javax.swing.tree.*;

import com.sqrt.liblab.LabCollection;
import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt4.grimedi.ui.component.BusyDialog;
import com.sqrt4.grimedi.ui.editor.EditorMapper;
import com.sqrt4.grimedi.ui.editor.EditorPanel;
import com.sqrt4.grimedi.ui.editor.HexView;
import com.sqrt4.grimedi.util.CachedPredicate;
import com.sqrt4.grimedi.util.FilterableListModel;
import com.sqrt4.grimedi.util.Predicate;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author James Lawrence
 */
public class MainWindow extends JFrame {
    public LabCollection context;
    private FilterableListModel<EntryDataProvider> filterableEntries;
    private Predicate<EntryDataProvider> searchPredicate;
    private BusyDialog _busy;

    public MainWindow() {
        initComponents();
    }

    private void fileSearch(CaretEvent e) {
        filterableEntries.removeFilter(searchPredicate);
        if (searchField.getText().isEmpty()) {
            filterableEntries.applyFilters();
            return;
        }
        final String search = searchField.getText();
        searchPredicate = new CachedPredicate<EntryDataProvider>(new Predicate<EntryDataProvider>() {
            public boolean accept(EntryDataProvider entryDataProvider) {
                return entryDataProvider.getName().toLowerCase().contains(search);
            }
        });
        filterableEntries.addFilter(searchPredicate);
        filterableEntries.applyFilters();
    }

    private void showBusyDialog(String title, String message) {
        try {
            if (_busy == null)
                _busy = new BusyDialog(this);
            _busy.setTitle(title);
            _busy.setMessage(message);
            _busy.pack();
            _busy.setVisible(true);
        } catch (Throwable t) {
            if (_busy != null) {
                _busy.setVisible(false);
                _busy = null;
            }
        }
    }

    private void hideBusyDialog() {
        if (_busy == null || !_busy.isVisible())
            return;
        _busy.setVisible(false);
        _busy = null;
    }

    private void fileSelected(TreeSelectionEvent e) {
        if (fileList.getSelectionPath() == null)
            return;
        Object selObj = fileList.getSelectionPath().getLastPathComponent();
        if (selObj == null || !(selObj instanceof EntryDataProvider))
            return;
        final EntryDataProvider selected = (EntryDataProvider) selObj;

        new Thread() {
            public void run() {
                editorPane.removeAll();
                try {
                    while (_busy == null || !_busy.isVisible())
                        Thread.sleep(100); // race condition...
                    EntryCodec<?> codec = CodecMapper.codecForProvider(selected);
                    boolean fallback = true;
                    LabEntry data = null;
                    try {
                        if (codec != null)
                            data = codec.read(selected);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    EditorPanel panel = EditorMapper.editorPanelForProvider(selected);
                    if (data != null && panel != null) {
                        panel.setData(data);
                        fallback = false;
                        editorPane.add(panel);
                    }
                    if (fallback)
                        editorPane.add(new HexView(selected));
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    hideBusyDialog();
                }
                editorPane.invalidate();
                editorPane.revalidate();
                editorPane.repaint();
            }
        }.start();

        showBusyDialog("Please wait...", "Loading " + selected.getName());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menuItem1 = new JMenuItem();
        menuItem2 = new JMenuItem();
        separator1 = new JSeparator();
        menuItem3 = new JMenuItem();
        splitPane1 = new JSplitPane();
        panel1 = new JPanel();
        scrollPane1 = new JScrollPane();
        fileList = new JTree();
        panel2 = new JPanel();
        searchLabel = new JLabel();
        searchField = new JTextField();
        editorPane = new JPanel();
        openAction = new OpenAction();
        closeAction = new CloseAction();

        //======== this ========
        setTitle("GrimEdi");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("File");

                //---- menuItem1 ----
                menuItem1.setAction(openAction);
                menuItem1.setMnemonic('O');
                menu1.add(menuItem1);

                //---- menuItem2 ----
                menuItem2.setAction(closeAction);
                menu1.add(menuItem2);
                menu1.add(separator1);

                //---- menuItem3 ----
                menuItem3.setText("Exit");
                menu1.add(menuItem3);
            }
            menuBar1.add(menu1);
        }
        setJMenuBar(menuBar1);

        //======== splitPane1 ========
        {
            splitPane1.setResizeWeight(0.1);

            //======== panel1 ========
            {
                panel1.setLayout(new BorderLayout());

                //======== scrollPane1 ========
                {

                    //---- fileList ----
                    fileList.setShowsRootHandles(true);
                    fileList.setRootVisible(false);
                    fileList.addTreeSelectionListener(new TreeSelectionListener() {
                        @Override
                        public void valueChanged(TreeSelectionEvent e) {
                            fileSelected(e);
                        }
                    });
                    scrollPane1.setViewportView(fileList);
                }
                panel1.add(scrollPane1, BorderLayout.CENTER);

                //======== panel2 ========
                {
                    panel2.setLayout(new GridBagLayout());
                    ((GridBagLayout) panel2.getLayout()).columnWidths = new int[]{0, 0, 0, 0};
                    ((GridBagLayout) panel2.getLayout()).rowHeights = new int[]{0, 0, 0};
                    ((GridBagLayout) panel2.getLayout()).columnWeights = new double[]{1.0, 1.0, 0.0, 1.0E-4};
                    ((GridBagLayout) panel2.getLayout()).rowWeights = new double[]{1.0, 1.0, 1.0E-4};

                    //---- searchLabel ----
                    searchLabel.setIcon(new ImageIcon(getClass().getResource("/tm_item_search.png")));
                    searchLabel.setLabelFor(searchField);
                    searchLabel.setText("Search:");
                    searchLabel.setEnabled(false);
                    searchLabel.setHorizontalAlignment(SwingConstants.TRAILING);
                    panel2.add(searchLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 2, 2), 0, 0));

                    //---- searchField ----
                    searchField.setEnabled(false);
                    searchField.addCaretListener(new CaretListener() {
                        @Override
                        public void caretUpdate(CaretEvent e) {
                            fileSearch(e);
                        }
                    });
                    panel2.add(searchField, new GridBagConstraints(1, 0, 2, 1, 3.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 2, 0), 0, 0));
                }
                panel1.add(panel2, BorderLayout.NORTH);
            }
            splitPane1.setLeftComponent(panel1);

            //======== editorPane ========
            {
                editorPane.setLayout(new BorderLayout());
            }
            splitPane1.setRightComponent(editorPane);
        }
        contentPane.add(splitPane1, BorderLayout.CENTER);
        setSize(800, 600);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private void onOpen() {
        fileList.setModel(new TreeModel() {
            public Object getRoot() {
                return context;
            }

            public Object getChild(Object parent, int index) {
                if (parent == context)
                    return context.labs.get(index);
                else if (parent instanceof LabFile)
                    return ((LabFile) parent).entries.get(index);
                return null;
            }

            public int getChildCount(Object parent) {
                if (parent == context)
                    return context.labs.size();
                else if (parent instanceof LabFile)
                    return ((LabFile) parent).entries.size();
                return 0;
            }

            public boolean isLeaf(Object node) {
                return getChildCount(node) == 0;
            }

            public void valueForPathChanged(TreePath path, Object newValue) {
            }

            public int getIndexOfChild(Object parent, Object child) {
                if (parent == context)
                    return context.labs.indexOf(child);
                else if (parent instanceof LabFile)
                    return ((LabFile) parent).entries.indexOf(child);
                return -1;
            }

            public void addTreeModelListener(TreeModelListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeTreeModelListener(TreeModelListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        searchField.setText("");
        searchPredicate = null;
        searchField.setEnabled(true);
        searchLabel.setEnabled(true);
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem menuItem1;
    private JMenuItem menuItem2;
    private JSeparator separator1;
    private JMenuItem menuItem3;
    private JSplitPane splitPane1;
    private JPanel panel1;
    private JScrollPane scrollPane1;
    private JTree fileList;
    private JPanel panel2;
    private JLabel searchLabel;
    private JTextField searchField;
    private JPanel editorPane;
    private OpenAction openAction;
    private CloseAction closeAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    JFileChooser jfc = new JFileChooser(".");

    private class OpenAction extends AbstractAction {
        private OpenAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Open");
            putValue(SHORT_DESCRIPTION, "Open a LAB file");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showOpenDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION)
                return;
            File f = jfc.getSelectedFile();
            try {
                context = LabCollection.open(f);
                onOpen();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class CloseAction extends AbstractAction {
        private CloseAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Close");
            putValue(SHORT_DESCRIPTION, "Close the current LAB file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            // TODO add your code here
        }
    }
}
