/*
 * Created by JFormDesigner on Fri Mar 15 22:29:38 GMT 2013
 */

package com.sqrt4.grimedi.ui;

import com.sqrt.liblab.LabCollection;
import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.io.DataSource;
import com.sqrt4.grimedi.ui.component.BusyDialog;
import com.sqrt4.grimedi.ui.editor.EditorMapper;
import com.sqrt4.grimedi.ui.editor.EditorPanel;
import com.sqrt4.grimedi.ui.editor.HexView;
import com.sqrt4.grimedi.util.CachedPredicate;
import com.sqrt4.grimedi.util.Predicate;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * @author James Lawrence
 */
public class MainWindow extends JFrame {
    public LabCollection context;
    private FilterableLabTreeModel filterableEntries;
    private Predicate<DataSource> searchPredicate;
    private BusyDialog _busy;
    private DataSource popupSource;
    private LabFile labPopupSource;

    public MainWindow() {
        initComponents();
        fileList.setCellRenderer(new DefaultTreeCellRenderer() {
            ImageIcon labIcon = new ImageIcon(getClass().getResource("/lab.png"));

            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof LabFile) {
                    setIcon(labIcon);
                } else if (value instanceof DataSource) {
                    EditorPanel panel = EditorMapper.editorPanelForProvider((DataSource) value);
                    if (panel != null)
                        setIcon(panel.getIcon());
                    else
                        setIcon(EditorPanel.defaultIcon);
                }
                return c;
            }
        });
    }

    private void fileSearch(CaretEvent e) {
        filterableEntries.removeFilter(searchPredicate);
        if (searchField.getText().isEmpty()) {
            filterableEntries.applyFilters();
            return;
        }
        final String search = searchField.getText();
        final String regex = search.replace(".", "\\.").replace("*", ".*").replace("?", ".");
        searchPredicate = new CachedPredicate<DataSource>(new Predicate<DataSource>() {
            public boolean accept(DataSource dataSource) {
                String lcase = dataSource.getName().toLowerCase();
                return lcase.startsWith(search) || lcase.matches(regex);
            }
        });
        filterableEntries.addFilter(searchPredicate);
        filterableEntries.applyFilters();
    }

    public void runAsyncWithPopup(String popupTitle, String message, final Runnable r) {
        new Thread() {
            public void run() {
                while (_busy == null || !_busy.isVisible()) {
                    try {
                        Thread.sleep(100); // Wait until the busy dialog is shown...
                    } catch (InterruptedException ignore) {
                    }
                }
                try {
                    r.run(); // Do it...
                } catch (Throwable e) {
                    e.printStackTrace(); // Catch any errors
                } finally {
                    _busy.setVisible(false);
                    _busy = null;
                }
            }
        }.start();
        try {
            if (_busy == null)
                _busy = new BusyDialog(this);
            _busy.setTitle(popupTitle);
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

    public void setBusyMessage(String message) {
        if (_busy == null || !_busy.isVisible())
            return;
        _busy.setMessage(message);
    }

    private void fileSelected(TreeSelectionEvent e) {
        if (fileList.getSelectionPath() == null)
            return;
        Object selObj = fileList.getSelectionPath().getLastPathComponent();
        if (selObj == null || !(selObj instanceof DataSource))
            return;
        final DataSource selected = (DataSource) selObj;

        runAsyncWithPopup("Please wait...", "Loading " + selected.getName() + "...", new Runnable() {
            public void run() {
                while (editorPane.getComponentCount() > 0) {
                    Component c = editorPane.getComponent(0);
                    if (c instanceof EditorPanel) {
                        EditorPanel ep = (EditorPanel) c;
                        ep.onHide();
                    }
                    editorPane.remove(0);
                }
                EntryCodec<?> codec = CodecMapper.codecForProvider(selected);
                boolean fallback = true;
                LabEntry data = null;
                try {
                    selected.seek(0);
                    if (codec != null) {
                        try {
                            data = codec.read(selected);
                        } catch (OutOfMemoryError oome) {
                            System.gc();
                            data = codec.read(selected);
                        }
                    }
                } catch (Throwable e1) {
                    e1.printStackTrace();
                }
                EditorPanel panel = EditorMapper.editorPanelForProvider(selected);
                if (data != null && panel != null) {
                    panel.setWindow(MainWindow.this);
                    panel.setData(data);
                    fallback = false;
                    editorPane.add(panel);
                    panel.onShow();
                }
                if (fallback)
                    editorPane.add(new HexView(selected));

                editorPane.invalidate();
                editorPane.revalidate();
                editorPane.repaint();
            }
        });
    }

    private void fileListMousePressed(MouseEvent e) {
        TreePath selPath = fileList.getPathForLocation(e.getX(), e.getY());
        if (selPath == null)
            return;
        Object clicked = selPath.getLastPathComponent();
        if (clicked instanceof DataSource) {
            popupSource = (DataSource) clicked;
            if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
                entryPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        } else if (clicked instanceof LabFile) {
            labPopupSource = (LabFile) clicked;
            if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
                labPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
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
        entryPopupMenu = new JPopupMenu();
        menuItem4 = new JMenuItem();
        menuItem5 = new JMenuItem();
        labPopupMenu = new JPopupMenu();
        menuItem6 = new JMenuItem();
        openAction = new OpenAction();
        closeAction = new CloseAction();
        extractEntryAction = new ExtractEntryAction();
        deleteEntryAction = new DeleteEntryAction();
        extractAllAction = new ExtractAllAction();

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
                    fileList.setModel(new DefaultTreeModel(
                            new DefaultMutableTreeNode("Nothing open") {
                                {
                                }
                            }));
                    fileList.setRootVisible(false);
                    fileList.addTreeSelectionListener(new TreeSelectionListener() {
                        @Override
                        public void valueChanged(TreeSelectionEvent e) {
                            fileSelected(e);
                        }
                    });
                    fileList.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            fileListMousePressed(e);
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

        //======== entryPopupMenu ========
        {

            //---- menuItem4 ----
            menuItem4.setMnemonic('E');
            menuItem4.setAction(extractEntryAction);
            entryPopupMenu.add(menuItem4);

            //---- menuItem5 ----
            menuItem5.setMnemonic('D');
            menuItem5.setAction(deleteEntryAction);
            entryPopupMenu.add(menuItem5);
        }

        //======== labPopupMenu ========
        {

            //---- menuItem6 ----
            menuItem6.setAction(extractAllAction);
            labPopupMenu.add(menuItem6);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private void onOpen() {
        fileList.setModel(filterableEntries = new FilterableLabTreeModel(context));
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
    private JPopupMenu entryPopupMenu;
    private JMenuItem menuItem4;
    private JMenuItem menuItem5;
    private JPopupMenu labPopupMenu;
    private JMenuItem menuItem6;
    private OpenAction openAction;
    private CloseAction closeAction;
    private ExtractEntryAction extractEntryAction;
    private DeleteEntryAction deleteEntryAction;
    private ExtractAllAction extractAllAction;
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
            runAsyncWithPopup("Please wait...", "Loading LAB files...", new Runnable() {
                public void run() {
                    File f = jfc.getSelectedFile();
                    try {
                        context = LabCollection.open(f);
                        onOpen();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
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
            // TODO: close the LAB file...
        }
    }

    private class FilterableLabTreeModel implements TreeModel {
        private final LabCollection source;
        private final java.util.List<TreeModelListener> listeners = new LinkedList<TreeModelListener>();
        private final java.util.List<Predicate<DataSource>> predicates = new LinkedList<Predicate<DataSource>>();
        private final LinkedHashMap<LabFile, java.util.List<DataSource>> filtered = new LinkedHashMap<LabFile, java.util.List<DataSource>>();

        public FilterableLabTreeModel(LabCollection coll) {
            this.source = coll;
            applyFilters();
        }

        public void addFilter(Predicate<DataSource> pred) {
            predicates.add(pred);
        }

        public void removeFilter(Predicate<DataSource> pred) {
            predicates.remove(pred);
        }

        public void applyFilters() {
            for (LabFile lb : source.labs) {
                java.util.List<DataSource> source = lb.entries;
                for (Predicate<DataSource> pred : predicates) {
                    java.util.List<DataSource> accepted = new LinkedList<DataSource>();
                    for (DataSource e : source)
                        if (pred.accept(e))
                            accepted.add(e);
                    source = accepted;
                }
                if (source.isEmpty())
                    filtered.remove(lb);
                else
                    filtered.put(lb, source);
            }

            if (listeners.isEmpty())
                return;
            TreeModelEvent tme = new TreeModelEvent(this, new Object[]{source});
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).treeStructureChanged(tme);
            }
        }

        public Object getRoot() {
            return source;
        }

        public Object getChild(Object parent, int index) {
            if (parent == context) {
                Object[] labs = filtered.keySet().toArray();
                if (labs.length == 0 && index == 0)
                    return "No entries";
                return labs[index];
            } else if (parent instanceof LabFile)
                return filtered.get(parent).get(index);
            return null;
        }

        public int getChildCount(Object parent) {
            if (parent == source) {
                int labs = filtered.keySet().size();
                if (labs == 0)
                    return 1; // No results message
                return labs;
            } else if (parent instanceof LabFile) {
                return filtered.get(parent).size();
            }
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
                return filtered.get(parent).indexOf(child);
            return -1;
        }

        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }
    }

    private class ExtractEntryAction extends AbstractAction {
        private ExtractEntryAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Extract");
            putValue(SHORT_DESCRIPTION, "Extract this entry to a file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            final JFileChooser fc = new JFileChooser(jfc.getCurrentDirectory());
            fc.setSelectedFile(new File(popupSource.getName()));
            if (fc.showSaveDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION)
                return;
            runAsyncWithPopup("Please wait...", "Extracting " + popupSource.getName() + "...", new Runnable() {
                public void run() {
                    try {
                        FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
                        byte[] buf = new byte[5000];
                        int copied = 0;
                        long len = popupSource.getLength();
                        popupSource.seek(0);
                        while (copied < len) {
                            int toRead = (int) Math.min(buf.length, len - copied);
                            int read = popupSource.read(buf, 0, toRead);
                            fos.write(buf, 0, read);
                            copied += read;
                        }
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

        }
    }

    private class DeleteEntryAction extends AbstractAction {
        private DeleteEntryAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Delete");
            putValue(SHORT_DESCRIPTION, "Delete the entry from the LAB file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            popupSource.container.entries.remove(popupSource);
        }
    }

    private class ExtractAllAction extends AbstractAction {
        private ExtractAllAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Extract all...");
            putValue(SHORT_DESCRIPTION, "Extract all the entries in this LAB file to the specified directory...");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser(jfc.getCurrentDirectory());
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showSaveDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION)
                return;

            final File dir = fc.getSelectedFile();
            final String pre = "<html>Extracting " + labPopupSource.toString() + "...<br/>";
            runAsyncWithPopup("Please wait...", pre, new Runnable() {
                public void run() {
                    final byte[] buf = new byte[5000];
                    for (DataSource source : labPopupSource.entries) {
                        _busy.setMessage(pre + "\n" + "\t" + source.getName());
                        try {
                            FileOutputStream fos = new FileOutputStream(new File(dir, source.getName()));
                            int copied = 0;
                            long len = source.getLength();
                            source.seek(0);
                            while (copied < len) {
                                int toRead = (int) Math.min(buf.length, len - copied);
                                int read = source.read(buf, 0, toRead);
                                fos.write(buf, 0, read);
                                copied += read;
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}