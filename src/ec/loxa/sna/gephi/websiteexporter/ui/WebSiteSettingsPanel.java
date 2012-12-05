/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WebSiteSettingsPanel.java
 *
 * Created on 27-abr-2011, 19:01:05
 */
package ec.loxa.sna.gephi.websiteexporter.ui;

import ec.loxa.sna.gephi.websiteexporter.WebSiteExporter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.JFileChooser;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.*;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author jorgaf
 */
public class WebSiteSettingsPanel extends javax.swing.JPanel {

    final String LAST_PATH = "WebSiteExporterUI_Last_Path";
    private WebSiteExporter wsExporter;
    private File path;

    /** Creates new form WebSiteSettingsPanel */
    public WebSiteSettingsPanel() {
        initComponents();
        loadWorkSpaceNames();

        btnBrowse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(txtPath.getText());
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
                if (result == JFileChooser.APPROVE_OPTION) {
                    path = fileChooser.getSelectedFile();
                    txtPath.setText(path.getAbsolutePath());
                }
            }
        });
    }

    public void setup(WebSiteExporter exporter) {
        this.wsExporter = exporter;


        path = new File(NbPreferences.forModule(WebSiteExporterUI.class).get(LAST_PATH, System.getProperty("user.home")));
        txtPath.setText(path.getAbsolutePath());
        
        chbAttributes.setSelected(wsExporter.isExportAttributes());
        chbColors.setSelected(wsExporter.isExportColors());
        chbDynamic.setSelected(wsExporter.isExportDynamic());
        chbPosition.setSelected(wsExporter.isExportPosition());
        chbSize.setSelected(wsExporter.isExportSize());
    }

    public void unsetup(boolean update) {
        if (update) {
            try {
                path = new File(txtPath.getText());
            } catch (Exception e) {
            }
            NbPreferences.forModule(WebSiteExporterUI.class).put(LAST_PATH, path.getAbsolutePath());
            wsExporter.setPath(path);
            wsExporter.setExportAttributes(chbAttributes.isSelected());
            wsExporter.setExportColors(chbColors.isSelected());
            wsExporter.setExportDynamic(chbDynamic.isSelected());
            wsExporter.setExportPosition(chbPosition.isSelected());
            wsExporter.setExportSize(chbSize.isSelected());
        }
    }

    public static ValidationPanel createValidationPanel(WebSiteSettingsPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();        

        group.add(innerPanel.txtPath, Validators.FILE_MUST_BE_DIRECTORY);        

        return validationPanel;
    }

    private void loadWorkSpaceNames() {
        Project project = Lookup.getDefault().lookup(ProjectController.class).
                getCurrentProject();
        WorkspaceProvider workspaceProvider =
                project.getLookup().lookup(WorkspaceProvider.class);
        WorkspaceInformation workspaceInfortion;
        Workspace workspace;
        
        ArrayList<String> namesSelected = new ArrayList<String>();
        GraphModel graphModel;

        for (int i = 0; i < workspaceProvider.getWorkspaces().length; i++) {
            workspace = workspaceProvider.getWorkspaces()[i];

            workspaceInfortion = workspace.getLookup().lookup(WorkspaceInformation.class);
            graphModel = workspace.getLookup().lookup(GraphModel.class);
            if (graphModel.getGraphVisible().getNodeCount() > 0 || graphModel.getGraphVisible().getEdgeCount() > 0) {
                namesSelected.add(workspaceInfortion.getName());                
            }
        }
        final String[] names;
        String[] namesAux = new String[namesSelected.size()];
        names = namesSelected.toArray(namesAux);

        lstWorkspaces.setModel(new AbstractListModel() {

            String[] strings = names;

            @Override
            public int getSize() {
                return strings.length;
            }

            @Override
            public Object getElementAt(int index) {

                return strings[index];
            }
        });

        if (names.length == 1) {
            lstWorkspaces.setSelectedIndex(0);
            lstWorkspaces.setEnabled(false);
        }
    }

    public String[] getSelectedWorkspaces() {
        String[] selectedWorkspaces = new String[lstWorkspaces.getSelectedValues().length];
        for (int i = 0; i < lstWorkspaces.getSelectedValues().length; i++) {
            selectedWorkspaces[i] = lstWorkspaces.getSelectedValues()[i].toString().replace(" ", "");
        }
        return selectedWorkspaces;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        header = new org.jdesktop.swingx.JXHeader();
        pnlWSettings = new javax.swing.JPanel();
        lblPath = new javax.swing.JLabel();
        txtPath = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        lblWorkspace = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstWorkspaces = new javax.swing.JList();
        pnlGEXFSettings = new javax.swing.JPanel();
        chbAttributes = new javax.swing.JCheckBox();
        chbColors = new javax.swing.JCheckBox();
        chbDynamic = new javax.swing.JCheckBox();
        chbPosition = new javax.swing.JCheckBox();
        chbSize = new javax.swing.JCheckBox();

        header.setDescription(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.header.description")); // NOI18N
        header.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ec/loxa/sna/gephi/websiteexporter/resources/loxa.png"))); // NOI18N
        header.setIconPosition(org.jdesktop.swingx.JXHeader.IconPosition.LEFT);
        header.setTitle(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.header.title")); // NOI18N

        pnlWSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.pnlWSettings.border.title"))); // NOI18N

        lblPath.setLabelFor(txtPath);
        lblPath.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.lblPath.text")); // NOI18N

        btnBrowse.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.btnBrowse.text")); // NOI18N

        lblWorkspace.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.lblWorkspace.text")); // NOI18N

        jScrollPane1.setViewportView(lstWorkspaces);

        org.jdesktop.layout.GroupLayout pnlWSettingsLayout = new org.jdesktop.layout.GroupLayout(pnlWSettings);
        pnlWSettings.setLayout(pnlWSettingsLayout);
        pnlWSettingsLayout.setHorizontalGroup(
            pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlWSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnlWSettingsLayout.createSequentialGroup()
                        .add(lblPath)
                        .add(18, 18, 18)
                        .add(txtPath))
                    .add(pnlWSettingsLayout.createSequentialGroup()
                        .add(lblWorkspace)
                        .add(18, 18, 18)
                        .add(jScrollPane1)))
                .add(18, 18, 18)
                .add(btnBrowse)
                .addContainerGap())
        );
        pnlWSettingsLayout.setVerticalGroup(
            pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlWSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPath)
                    .add(txtPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnBrowse))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblWorkspace)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlGEXFSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.pnlGEXFSettings.border.title"))); // NOI18N

        chbAttributes.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbAttributes.text")); // NOI18N

        chbColors.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbColors.text")); // NOI18N

        chbDynamic.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbDynamic.text")); // NOI18N

        chbPosition.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbPosition.text")); // NOI18N

        chbSize.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbSize.text")); // NOI18N

        org.jdesktop.layout.GroupLayout pnlGEXFSettingsLayout = new org.jdesktop.layout.GroupLayout(pnlGEXFSettings);
        pnlGEXFSettings.setLayout(pnlGEXFSettingsLayout);
        pnlGEXFSettingsLayout.setHorizontalGroup(
            pnlGEXFSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlGEXFSettingsLayout.createSequentialGroup()
                .add(97, 97, 97)
                .add(pnlGEXFSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(chbSize)
                    .add(chbPosition)
                    .add(chbDynamic)
                    .add(chbColors)
                    .add(chbAttributes))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlGEXFSettingsLayout.setVerticalGroup(
            pnlGEXFSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlGEXFSettingsLayout.createSequentialGroup()
                .add(chbAttributes)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbColors)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbDynamic)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbPosition)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbSize)
                .add(0, 0, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlGEXFSettings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(header, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1174, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(pnlWSettings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(header, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(pnlWSettings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(pnlGEXFSettings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JCheckBox chbAttributes;
    private javax.swing.JCheckBox chbColors;
    private javax.swing.JCheckBox chbDynamic;
    private javax.swing.JCheckBox chbPosition;
    private javax.swing.JCheckBox chbSize;
    private org.jdesktop.swingx.JXHeader header;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPath;
    private javax.swing.JLabel lblWorkspace;
    private javax.swing.JList lstWorkspaces;
    private javax.swing.JPanel pnlGEXFSettings;
    private javax.swing.JPanel pnlWSettings;
    private javax.swing.JTextField txtPath;
    // End of variables declaration//GEN-END:variables
}
