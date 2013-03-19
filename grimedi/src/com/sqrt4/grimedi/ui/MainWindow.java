/*
 * Created by JFormDesigner on Fri Mar 15 22:29:38 GMT 2013
 */

package com.sqrt4.grimedi.ui;

import javax.swing.event.*;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt4.grimedi.ui.editor.EditorMapper;
import com.sqrt4.grimedi.ui.editor.EditorPanel;
import com.sqrt4.grimedi.ui.editor.HexView;
import com.sqrt4.grimedi.util.CachedPredicate;
import com.sqrt4.grimedi.util.FilterableListModel;
import com.sqrt4.grimedi.util.Predicate;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.*;

/**
 * @author James Lawrence
 */
public class MainWindow extends JFrame {
    public LabFile context;
    private FilterableListModel<EntryDataProvider> filterableEntries;
    private Predicate<EntryDataProvider> extPredicate, searchPredicate;

    public MainWindow() {
        initComponents();
    }

    private void fileSelected(ListSelectionEvent e) {
        // Todo: lookup view and apply
        // (for now just show hexview)
        if(e.getValueIsAdjusting())
            return;
        editorPane.removeAll();

        EntryDataProvider selected = (EntryDataProvider) fileList.getSelectedValue();
        if(selected == null)
            return;
        try {
            selected.seek(0);
        } catch (IOException e1) {
            /**/
        }

        EntryCodec<?> codec = CodecMapper.codecForProvider(selected);
        boolean fallback = true;
        EditorPanel panel = EditorMapper.editorPanelForProvider(selected);
        if(panel != null) {
            try {
                LabEntry data = codec.read(selected);
                if(data != null) {
                    panel.setData(data);
                    fallback = false;
                    editorPane.add(panel);
                }
            } catch (IOException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if(fallback)
            editorPane.add(new HexView((EntryDataProvider) (fileList.getSelectedValue())));
        editorPane.invalidate();
        editorPane.revalidate();
        editorPane.repaint();
    }

    private void extFiltered(ItemEvent e) {
        filterableEntries.removeFilter(extPredicate);
        if(extFilter.getSelectedIndex() <= 0) {
            filterableEntries.applyFilters();
            return;
        }
        String ex = (String) extFilter.getSelectedItem();
        final String ext = ex.substring(0, ex.length() - 8);
        extPredicate = new CachedPredicate<EntryDataProvider>(new Predicate<EntryDataProvider>() {
            public boolean accept(EntryDataProvider entryDataProvider) {
                return entryDataProvider.getName().toLowerCase().endsWith("." + ext);
            }
        });
        filterableEntries.addFilter(extPredicate);
        filterableEntries.applyFilters();
    }

    private void fileSearch(CaretEvent e) {
        filterableEntries.removeFilter(searchPredicate);
        if(searchField.getText().isEmpty()) {
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
        fileList = new JList();
        panel2 = new JPanel();
        searchLabel = new JLabel();
        searchField = new JTextField();
        extLabel = new JLabel();
        extFilter = new JComboBox();
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
                    fileList.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            fileSelected(e);
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

                    //---- extLabel ----
                    extLabel.setText("File types:");
                    extLabel.setLabelFor(extFilter);
                    extLabel.setEnabled(false);
                    extLabel.setHorizontalAlignment(SwingConstants.TRAILING);
                    panel2.add(extLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 2), 0, 0));

                    //---- extFilter ----
                    extFilter.setEnabled(false);
                    extFilter.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            extFiltered(e);
                        }
                    });
                    panel2.add(extFilter, new GridBagConstraints(1, 1, 2, 1, 3.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
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
        fileList.setModel(filterableEntries = new FilterableListModel(context.entries));
        fileList.setCellRenderer(new LabEntryRenderer());
        Set<String> exts = new TreeSet<String>();
        for(EntryDataProvider edp: context.entries) {
            String name = edp.getName();
            if(name == null)
                continue;
            int eidx = name.lastIndexOf('.');
            if(eidx == -1)
                continue;
            String ext = name.substring(eidx+1).toLowerCase();
            exts.add(ext);
        }
        String[] arr = new String[exts.size()+1];
        arr[0] = "All entries";
        int off = 1;
        for(String s: exts)
            arr[off++] = s + " entries";
        extFilter.setModel(new DefaultComboBoxModel(arr));
        extFilter.setSelectedIndex(0);
        searchField.setText("");
        extPredicate = searchPredicate = null;
        searchField.setEnabled(true);
        searchLabel.setEnabled(true);
        extFilter.setEnabled(true);
        extLabel.setEnabled(true);
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
    private JList fileList;
    private JPanel panel2;
    private JLabel searchLabel;
    private JTextField searchField;
    private JLabel extLabel;
    private JComboBox extFilter;
    private JPanel editorPane;
    private OpenAction openAction;
    private CloseAction closeAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class LabEntryRenderer implements ListCellRenderer<EntryDataProvider> {
        public Component getListCellRendererComponent(JList<? extends EntryDataProvider> list, EntryDataProvider value, int index, boolean isSelected, boolean cellHasFocus) {
            // Todo: cache or something...
            // Todo: icons, tooltips and other niceties :)
            JLabel jl = new JLabel();
            jl.setOpaque(true);
            if (isSelected) {
                jl.setBackground(list.getSelectionBackground());
                jl.setForeground(list.getSelectionForeground());
            } else {
                jl.setBackground(list.getBackground());
                jl.setForeground(list.getForeground());
            }
            jl.setText(value.getName());
            return jl;
        }
    }

    private class OpenAction extends AbstractAction {
        private OpenAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Open");
            putValue(SHORT_DESCRIPTION, "Open a LAB file");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION)
                return;
            File f = jfc.getSelectedFile();
            try {
                context = LabFile.open(f);
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
