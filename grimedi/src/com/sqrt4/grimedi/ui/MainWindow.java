

/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created by JFormDesigner on Fri Mar 15 22:29:38 GMT 2013
 */

package com.sqrt4.grimedi.ui;

import com.sqrt.liblab.LabCollection;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.io.DataSource;
import com.sqrt4.grimedi.Main;
import com.sqrt4.grimedi.ui.component.BusyDialog;
import com.sqrt4.grimedi.ui.editor.EditorMapper;
import com.sqrt4.grimedi.ui.editor.EditorPanel;
import com.sqrt4.grimedi.ui.editor.HexView;
import com.sqrt4.grimedi.util.CachedPredicate;
import com.sqrt4.grimedi.util.Predicate;
import com.sqrt4.grimedi.util.Size;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author James Lawrence
 */
public class MainWindow extends JFrame {
    private static MainWindow inst;
    public LabCollection context;
    private FilterableLabTreeModel filterableEntries;
    private Predicate<DataSource> searchPredicate;
    private BusyDialog _busy;
    private DataSource popupSource;
    private LabFile labPopupSource;
    private final JFileChooser fileChooser = new JFileChooser(".");

    static {
        SplashScreenController.setPercentage(10);
        SplashScreenController.setText("Registering codecs...");
        CodecMapper.registerDefaults();
        SplashScreenController.setPercentage(30);
        SplashScreenController.setText("Registering views...");
        EditorMapper.registerDefaults();
        SplashScreenController.setPercentage(80);
        SplashScreenController.setPercentage(100);
        SplashScreenController.setText("GrimEdi v" + Main.VERSION + " by James Lawrence");

        // It loads too fast, add artificial delay so people can appreciate the splash screen...
        if (SplashScreenController.supported()) {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            /**/
            }
        }
    }

    public static MainWindow getInstance() {
        return inst;
    }

    public MainWindow() {
        inst = this;
        SplashScreenController.setText("Initialising UI...");
        initComponents();
        fileList.setRenderDataProvider(new RenderDataProvider() {
            public String getDisplayName(Object o) {
                return o.toString();
            }

            public boolean isHtmlDisplayName(Object o) {
                return false;
            }

            public Color getBackground(Object o) {
                return fileList.getBackground();
            }

            public Color getForeground(Object o) {
                return fileList.getForeground();
            }

            public String getTooltipText(Object o) {
                return null;
            }

            ImageIcon labIcon = new ImageIcon(getClass().getResource("/lab.png"));

            public Icon getIcon(Object o) {

                if (o instanceof LabFile)
                    return labIcon;
                else if (o instanceof DataSource) {
                    EditorPanel panel = EditorMapper.editorPanelForProvider((DataSource) o);
                    return panel == null ? EditorPanel.defaultIcon : panel.getIcon();
                }
                return null;
            }
        });
        fileList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                fileSelected(e);
            }
        });
    }

    public JFileChooser createFileDialog() {
        JFileChooser jfc = new JFileChooser(fileChooser.getCurrentDirectory());
        return jfc;
    }

    private void fileSearch(CaretEvent e) {
        filterableEntries.removeFilter(searchPredicate);
        if (searchField.getText().isEmpty()) {
            filterableEntries.applyFilters();
            return;
        }
        final String search = searchField.getText().toLowerCase();
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

    public void runAsyncWithPopup(String message, final Runnable r, boolean canCancel, Action cancelCallback) {
        final AtomicBoolean abort = new AtomicBoolean(false);
        final Thread asyncThread = new Thread() {
            public void run() {
                while (_busy == null || !_busy.isVisible()) {
                    try {
                        Thread.sleep(10); // Wait until the busy dialog is shown...
                    } catch (InterruptedException ignore) {
                    }
                }

                try {
                    if(!abort.get())
                        r.run(); // Do it...
                } catch (Throwable t) {
                    t.printStackTrace(); // Catch any errors
                } finally {
                    _busy.setVisible(false);
                    _busy = null;
                }
            }
        };
        asyncThread.setDaemon(true);
        asyncThread.start();
        try {
            if (_busy == null)
                _busy = new BusyDialog(this);
            _busy.setCancellable(canCancel);
            _busy.setCancelCallback(cancelCallback);
            _busy.setTitle("Please wait...");
            _busy.setMessage(message);
            _busy.pack();
            _busy.setVisible(true);
        } catch (Throwable t) {
            abort.set(true);
            if (_busy != null)
                _busy.setVisible(true);
            t.printStackTrace();
        }
    }

    public void runAsyncWithPopup(String message, final Runnable r) {
        runAsyncWithPopup(message, r, false, null);
    }

    public void setBusyMessage(String message) {
        if (_busy == null || !_busy.isVisible())
            return;
        _busy.setMessage(message);
        _busy.pack();
    }

    public void handleException(Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(this, t.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void fileSelected(ListSelectionEvent e) {
        int row = fileList.getSelectionModel().getLeadSelectionIndex();
        Object selObj = fileList.getModel().getValueAt(fileList.convertRowIndexToModel(row), 0);
        if (selObj == null || !(selObj instanceof DataSource))
            return;
        final DataSource selected = (DataSource) selObj;

        runAsyncWithPopup("Loading " + selected.getName() + "...", new Runnable() {
            public void run() {
                while (editorPane.getComponentCount() > 0) {
                    Component c = editorPane.getComponent(0);
                    if (c instanceof EditorPanel) {
                        EditorPanel ep = (EditorPanel) c;
                        ep.onHide();

                        // Todo: check if modified, save in temporary file if it is, cleanup if not - etc.
                    }
                    editorPane.remove(0);
                }
                EntryCodec<?> codec = CodecMapper.codecForProvider(selected);
                boolean fallback = true;
                LabEntry data = null;
                try {
                    selected.position(0);
                    if (codec != null) {
                        try {
                            data = codec.read(selected);
                        } catch (OutOfMemoryError oome) {
                            System.gc();
                            data = codec.read(selected);
                        }
                    }
                } catch (Throwable e1) {
                    handleException(e1);
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
        if (context == null)
            return;
        TreePath selPath = fileList.getClosestPathForLocation(e.getX(), e.getY());
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

    private void onOpen() {
        filterableEntries = new FilterableLabTreeModel(context);
        fileList.setModel(DefaultOutlineModel.createOutlineModel(filterableEntries, filterableEntries, false, "File"));
        searchField.setText("");
        searchPredicate = null;
        searchField.setEnabled(true);
        searchLabel.setEnabled(true);
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem menuItem1;
    private JMenuItem menuItem2;
    private JSeparator separator1;
    private JMenuItem menuItem3;
    private JMenu menu2;
    private JMenuItem menuItem7;
    private JSplitPane splitPane1;
    private JPanel panel1;
    private JScrollPane scrollPane1;
    private Outline fileList;
    private JPanel panel2;
    private JLabel searchLabel;
    private JTextField searchField;
    private JPanel editorPane;
    private JPopupMenu entryPopupMenu;
    private JMenuItem menuItem4;
    private JMenuItem menuItem5;
    private JPopupMenu labPopupMenu;
    private JMenuItem menuItem6;
    private JMenuItem menuItem8;
    private OpenAction openAction;
    private CloseAction closeAction;
    private ExtractEntryAction extractEntryAction;
    private DeleteEntryAction deleteEntryAction;
    private ExtractAllAction extractAllAction;
    private OpenAboutDialogAction openAboutDialogAction;
    private SaveLabAction saveLabAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class OpenAction extends AbstractAction {
        private OpenAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Open");
            putValue(SHORT_DESCRIPTION, "Open a LAB file");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showOpenDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION)
                return;
            runAsyncWithPopup("Loading LAB files...", new Runnable() {
                public void run() {
                    File f = fileChooser.getSelectedFile();
                    try {
                        context = LabCollection.open(f);
                        onOpen();
                    } catch (IOException e1) {
                        handleException(e1);
                    }
                }
            });
        }
    }

    private class CloseAction extends AbstractAction {
        private CloseAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Close");
            putValue(SHORT_DESCRIPTION, "Close the current LAB file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            // TODO: close the LAB file...
        }
    }

    private class FilterableLabTreeModel implements TreeModel, RowModel {
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

        public int getColumnCount() {
            return 2;
        }

        public Object getValueFor(Object o, int i) {
            switch (i) {
                case 0:
                    String s = o.toString();
                    int idx = s.lastIndexOf('.');
                    if (idx == -1)
                        return "";
                    return s.substring(idx + 1);
                case 1:
                    if (o instanceof DataSource)
                        return new Size(((DataSource) o).length());
                    else if(o instanceof LabFile) {
                        long length = 0;
                        for(DataSource ds: ((LabFile) o).entries)
                            length += ds.length();
                        return new Size(length);
                    } else if(o instanceof LabCollection) {
                        long length = 0;
                        for(LabFile lf: ((LabCollection) o).labs) {
                            for(DataSource ds: lf.entries)
                                length += ds.length();
                        }
                        return new Size(length);
                    }
                    return null;
            }
            return null;
        }

        public Class getColumnClass(int i) {
            switch (i) {
                case 0:
                    return String.class;
                case 1:
                    return Integer.class;
            }
            return null;
        }

        public boolean isCellEditable(Object o, int i) {
            return false;
        }

        public void setValueFor(Object o, int i, Object o2) {
        }

        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return "Type";
                case 1:
                    return "Size";
                default:
                    return "?";
            }
        }
    }

    private class ExtractEntryAction extends AbstractAction {
        private ExtractEntryAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Extract");
            putValue(SHORT_DESCRIPTION, "Extract this entry to a file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            final JFileChooser jfc = createFileDialog();
            jfc.setSelectedFile(new File(popupSource.getName()));
            if (jfc.showSaveDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION)
                return;
            runAsyncWithPopup("Extracting " + popupSource.getName() + "...", new Runnable() {
                public void run() {
                    try {
                        FileOutputStream fos = new FileOutputStream(jfc.getSelectedFile());
                        byte[] buf = new byte[5000];
                        int copied = 0;
                        long len = popupSource.length();
                        popupSource.position(0);
                        while (copied < len) {
                            int toRead = (int) Math.min(buf.length, len - copied);
                            popupSource.get(buf, 0, toRead);
                            fos.write(buf, 0, toRead);
                            copied += toRead;
                        }
                        fos.close();
                    } catch (IOException e1) {
                        handleException(e1);
                    }
                }
            });

        }
    }

    private class DeleteEntryAction extends AbstractAction {
        private DeleteEntryAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Delete");
            putValue(SHORT_DESCRIPTION, "Delete the entry from the LAB file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            popupSource.container.entries.remove(popupSource);
            fileList.revalidate();
            // Todo: model should be notified...
        }
    }

    private class ExtractAllAction extends AbstractAction {
        private ExtractAllAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Extract all...");
            putValue(SHORT_DESCRIPTION, "Extract all the entries in this LAB file to the specified directory...");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = createFileDialog();
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showSaveDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION)
                return;

            final File dir = jfc.getSelectedFile();
            final AtomicBoolean cancelled = new AtomicBoolean(false);
            final String pre = "<html>Extracting " + labPopupSource.toString() + "...<br/>";
            runAsyncWithPopup(pre, new Runnable() {
                public void run() {
                    final byte[] buf = new byte[5000];
                    for (DataSource source : labPopupSource.entries) {
                        _busy.setMessage(pre + "\n" + "\t" + source.getName());
                        try {
                            File f = new File(dir, source.getName());
                            FileOutputStream fos = new FileOutputStream(f);
                            int copied = 0;
                            long len = source.length();
                            source.position(0);
                            while (!cancelled.get() && copied < len) {
                                int toRead = (int) Math.min(buf.length, len - copied);
                                source.get(buf, 0, toRead);
                                fos.write(buf, 0, toRead);
                                copied += toRead;
                            }
                            fos.close();
                            if(cancelled.get()) {
                                f.delete();
                                return;
                            }
                            Thread.sleep(1000);
                        } catch (Exception e1) {
                            handleException(e1);
                            break;
                        }
                    }
                }
            }, true, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    cancelled.set(true);
                }
            });
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menuItem1 = new JMenuItem();
        menuItem2 = new JMenuItem();
        separator1 = new JSeparator();
        menuItem3 = new JMenuItem();
        menu2 = new JMenu();
        menuItem7 = new JMenuItem();
        splitPane1 = new JSplitPane();
        panel1 = new JPanel();
        scrollPane1 = new JScrollPane();
        fileList = new Outline();
        panel2 = new JPanel();
        searchLabel = new JLabel();
        searchField = new JTextField();
        editorPane = new JPanel();
        entryPopupMenu = new JPopupMenu();
        menuItem4 = new JMenuItem();
        menuItem5 = new JMenuItem();
        labPopupMenu = new JPopupMenu();
        menuItem6 = new JMenuItem();
        menuItem8 = new JMenuItem();
        openAction = new OpenAction();
        closeAction = new CloseAction();
        extractEntryAction = new ExtractEntryAction();
        deleteEntryAction = new DeleteEntryAction();
        extractAllAction = new ExtractAllAction();
        openAboutDialogAction = new OpenAboutDialogAction();
        saveLabAction = new SaveLabAction();

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

            //======== menu2 ========
            {
                menu2.setText("Help");

                //---- menuItem7 ----
                menuItem7.setAction(openAboutDialogAction);
                menu2.add(menuItem7);
            }
            menuBar1.add(menu2);
        }
        setJMenuBar(menuBar1);

        //======== splitPane1 ========
        {

            //======== panel1 ========
            {
                panel1.setLayout(new BorderLayout());

                //======== scrollPane1 ========
                {

                    //---- fileList ----
                    fileList.setFillsViewportHeight(true);
                    fileList.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
                    fileList.setFullyNonEditable(true);
                    fileList.setSurrendersFocusOnKeystroke(false);
                    fileList.setShowHorizontalLines(true);
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
                    ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
                    ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
                    ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 1.0, 1.0E-4};

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

            //---- menuItem8 ----
            menuItem8.setAction(saveLabAction);
            labPopupMenu.add(menuItem8);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private class OpenAboutDialogAction extends AbstractAction {
        private OpenAboutDialogAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "About");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            AboutDialog dialog = new AboutDialog(MainWindow.this);
            dialog.pack();
            dialog.setVisible(true);
        }
    }

    private class SaveLabAction extends AbstractAction {
        private SaveLabAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Save LAB");
            putValue(SHORT_DESCRIPTION, "Rebuild and save this LAB");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = createFileDialog();
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if(jfc.showSaveDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION)
                return;

            final File dest = jfc.getSelectedFile();

            runAsyncWithPopup("Building LAB file, please wait...", new Runnable() {
                public void run() {
                    try {
                        labPopupSource.save(dest);
                    } catch (IOException e1) {
                        handleException(e1);
                    }
                }
            });
        }
    }
}