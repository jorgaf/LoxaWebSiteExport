/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.loxa.sna.gephi.websiteexporter;

import ec.loxa.sna.gephi.websiteexporter.util.statistics.GraphStatistic;
import ec.loxa.sna.gephi.websiteexporter.util.statistics.StatisticsJSON;
import ec.loxa.sna.websiteexporter.utilities.Util;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.imageio.ImageIO;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.plugin.ExporterCSV;
import org.gephi.io.exporter.plugin.ExporterGEXF;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionModel;
import org.gephi.project.api.*;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.GraphDensity;
import org.gephi.statistics.plugin.WeightedDegree;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author jorgaf
 */
public class WebSiteExporter implements Exporter, LongTask {

    public static final String PATH_JAR = "/ec/loxa/sna/gephi/websiteexporter/resources/";
    private Workspace currentWorkSpace;
    private ProgressTicket progress;
    private boolean cancel = false;
    private File path;
    private String projectName;
    private Workspace[] allWorkspaces;
    //ExporterGEXF properties
    private boolean exportAttributes;
    private boolean exportColors;
    private boolean exportDynamic;
    private boolean exportPosition;
    private boolean exportSize;
    private StatisticsJSON statistics = new StatisticsJSON();
    private File projectPath;
    private String[] selectedWorkspaces;

    @Override
    public boolean execute() {
        Progress.start(progress);
        Progress.setDisplayName(progress, getMessage("message_Export_Web_Site"));
        try {
            export();
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
        Progress.finish(progress);

        //Si el proceso se canceló se borra la carpeta raíz
        if (cancel) {
            try {
                delete(projectPath);
            } catch (IOException ex) {
            }
        }

        return !cancel;
    }

    private void export() throws Exception {
        Project project = Lookup.getDefault().lookup(ProjectController.class).
                getCurrentProject();
        //To get project name
        ProjectInformation projectInformation =
                project.getLookup().lookup(ProjectInformation.class);

        setProjectName(projectInformation.getName());

        File root = new File(getPath(), getProjectName());
        //TODO: Debe ejecutarse si y solo si no se agrega un proyecto. Verificar la estructura del index.html
        delete(root);
        root.mkdir();
        projectPath = root;

        //To get workspaces in the project
        WorkspaceProvider workspaceProvider =
                project.getLookup().lookup(WorkspaceProvider.class);


        setAllWorkspaces(workspaceProvider.getWorkspaces());

        //To get workspace information
        WorkspaceInformation workspaceInfortion;
        String workspaceName;

        GraphModel graphModel;
        AttributeModel attModel;

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        boolean hasPartitionImage;

        for (int i = 0; i < allWorkspaces.length && !cancel; i++) {
            setWorkspace(allWorkspaces[i]);
            workspaceInfortion =
                    getWorkspace().getLookup().lookup(WorkspaceInformation.class);
            workspaceName = workspaceInfortion.getName().replace(" ", "");

            //Verificar si se ha seleccionado
            if (isSelected(workspaceName)) {
                pc.openWorkspace(getWorkspace());
                Progress.setDisplayName(progress, getMessage("message_Export")
                        + " " + workspaceName);

                createDirectoryToWorkspace(projectPath, workspaceName);
                saveGEXF(workspaceName, getWorkspace());

                //Se verifica si no se canceló el proceso
                if (!cancel) {
                    PartitionModel partitionModel =
                            getWorkspace().getLookup().lookup(PartitionModel.class);

                    Progress.setDisplayName(progress,
                            getMessage("message_Getting_Statistics")
                            + " " + workspaceName);

                    graphModel = getWorkspace().getLookup().lookup(GraphModel.class);
                    attModel = getWorkspace().getLookup().lookup(AttributeModel.class);

                    saveGraphCSV(workspaceName, getWorkspace());
                    saveGraphPDF(workspaceName, getWorkspace());
                    hasPartitionImage = generatePartitionImage(partitionModel, workspaceName);
                    getStatistics(workspaceName, graphModel.getGraphVisible(), attModel, hasPartitionImage);
                } else {
                    Progress.setDisplayName(progress, getMessage("message_Cancel"));
                }
            }
        }
        //Si el proceso no fue cancelado se genera la información
        if (!cancel) {
            saveStatistics();
            copyWebSiteFiles();
            builIndexPage();
            delete(new File(projectPath.getAbsolutePath() + File.separator + "FilesWebSite.zip"));
            delete(new File(projectPath.getAbsolutePath() + File.separator + "__MACOSX"));
        }
    }

    private void getStatistics(String wsName, Graph currentGraph,
            AttributeModel attModel, boolean hasPartitionImage) throws IOException {
        Degree degree = new Degree();
        GraphDensity gDensity = new GraphDensity();
        WeightedDegree wDegree = new WeightedDegree();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        double avgDegree;
        double density;
        double wAvgDegree;

        GraphStatistic graphData = new GraphStatistic();

        graphData.setName(wsName);
        graphData.setTitle("Title to " + wsName);
        graphData.setEdges(String.valueOf(currentGraph.getEdgeCount()));
        graphData.setNodes(String.valueOf(currentGraph.getNodeCount()));

        Progress.setDisplayName(progress, getMessage("message_Degree"));

        degree.execute(currentGraph.getGraphModel(), attModel);
        avgDegree = degree.getAverageDegree();
        graphData.setAvgdegree(decimalFormat.format(avgDegree));

        Progress.setDisplayName(progress, getMessage("message_Density"));
        gDensity.execute(currentGraph.getGraphModel(), attModel);
        density = gDensity.getDensity();
        graphData.setDensity(decimalFormat.format(density));

        Progress.setDisplayName(progress, getMessage("message_WeigthedDegree"));
        wDegree.execute(currentGraph.getGraphModel(), attModel);
        wAvgDegree = wDegree.getAverageDegree();
        graphData.setAvgweighteddegree(decimalFormat.format(wAvgDegree));

        graphData.setGraphfile(wsName + "/" + wsName + ".csv");
        graphData.setPdffile(wsName + "/" + wsName + ".pdf");
        graphData.setBrowsegraph(wsName + "/" + wsName + ".gexf");

        if (hasPartitionImage) {
            graphData.setImgColorDescription(wsName + "/" + "imgDescriptor.png");
        }

        statistics.addGraph(graphData);
    }

    private void copyWebSiteFiles() throws Exception {
        Util util = new Util();
        util.setDirectoryToExtract(projectPath);
        util.setMode(Util.EXTRACT);

        util.copyFromJar(PATH_JAR, "FilesWebSite.zip");
        util.unZip(projectPath.getAbsolutePath() + File.separator + "FilesWebSite.zip");
    }

    private void builIndexPage() throws IOException {
        File index = new File(projectPath.getAbsolutePath() + File.separator + "index.html");
        StringBuilder options = new StringBuilder();
        StringBuilder menus = new StringBuilder();
        Document doc = Jsoup.parse(index, "UTF-8");
        List<GraphStatistic> gStatistics = statistics.getGraphs();
        int i = 0;
        Element comboGrafos = doc.select("#grafos").first();
        Element menu = doc.select("#menuAnalisis").first();
        menus.append("<div class=\"accordion-group\">");
        menus.append("<div class=\"accordion-heading\">");
        menus.append("<a class=\"accordion-toggle\" data-toggle=\"collapse\" data-parent=\"#menuAnalisis\" href=\"#collapse").append(projectName).append("\">").append(projectName).append("</a>");
        menus.append("</div>");
        menus.append("<div id=\"collapse").append(projectName).append("\" class=\"accordion-body collapse\">");
        menus.append("<div class=\"accordion-inner\">");
        menus.append("<ul>");
        options.append("<optgroup label='").append(projectName).append("'>");
        for (GraphStatistic gs : gStatistics) {
            if (i == 0) {
                Element body = doc.body();
                body.attr("onload", "start('" + gs.getName() + "');");
                options.append("<option value = '").append(gs.getName()).append("' selected='true'>").append(gs.getName()).append("</option>");
            } else {
                options.append("<option value = '").append(gs.getName()).append("'>").append(gs.getName()).append("</option>");
            }
            i++;
            menus.append("<li><a href='#' onClick=\"configView('").append(gs.getName()).append("')\">").append(gs.getName()).append("</a></li>");
        }
        menus.append("</ul>");
        menus.append("</div></div></div>");
        options.append("</optgroup>");

        comboGrafos.append(options.toString());
        menu.append(menus.toString());

        String pathFile = projectPath.getAbsolutePath() + File.separator + "index.html";
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(pathFile), "UTF-8");

        out.write(doc.toString());
        out.close();
    }

    private void saveStatistics() throws Exception {
        String pathFile = projectPath.getAbsolutePath() + File.separator + "estadisticas.json";
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(pathFile), "UTF-8");

        out.write(statistics.toJSON());

        out.close();
    }

    private void saveGraphCSV(String workspaceName, Workspace currentWorkspace) throws Exception {
        String pathFile = projectPath.getAbsolutePath() + File.separator + workspaceName + File.separator + workspaceName + ".csv";
        Writer out = new OutputStreamWriter(new FileOutputStream(pathFile), "UTF-8");
        ExporterCSV exporterCSV = new ExporterCSV();
        exporterCSV.setWorkspace(currentWorkspace);
        exporterCSV.setWriter(out);
        exporterCSV.setExportVisible(true);

        exporterCSV.execute();

        out.flush();
        out.close();
    }

    private void saveGraphPDF(String workspaceName, Workspace currentWs) throws Exception {
        String pathFile = projectPath.getAbsolutePath() + File.separator + workspaceName + File.separator + workspaceName + ".pdf";
        File file = new File(pathFile);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        PDFExporter exportPDF = new PDFExporter();

        exportPDF.setMarginTop(5f);
        exportPDF.setMarginLeft(5f);
        exportPDF.setMarginRight(5f);
        exportPDF.setMarginBottom(5f);

        exportPDF.setWorkspace(currentWs);
        exportPDF.setOutputStream(out);

        exportPDF.execute();

        out.flush();
        out.close();
    }

    private void saveGEXF(String workspaceName, Workspace currentWs) throws Exception {
        String pathFile = projectPath.getAbsolutePath() + File.separator + workspaceName + File.separator + workspaceName + ".gexf";
        File file = new File(pathFile);
        ExporterGEXF gexfExporter = new ExporterGEXF();
        Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

        gexfExporter.setExportAttributes(isExportAttributes());
        gexfExporter.setExportColors(isExportColors());
        gexfExporter.setExportDynamic(isExportDynamic());
        gexfExporter.setExportPosition(isExportPosition());
        gexfExporter.setExportSize(isExportSize());

        gexfExporter.setExportVisible(true);

        gexfExporter.setWorkspace(currentWs);
        gexfExporter.setWriter(out);

        gexfExporter.execute();

        out.flush();
        out.close();

    }

    private void createDirectoryToWorkspace(File root, String workspaceName) throws IOException {
        //TODO: Verificar si el directorio existe. Si existe generar otro nombre
        File directory = new File(root, workspaceName);        
        delete(directory);
        directory.mkdir();
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (f.exists() && !f.delete()) {
            throw new IOException(getMessage("exception_Failed_to_Delete") + f);
        }
    }

    private boolean isSelected(String wsName) {
        for (String name : getSelectedWorkspaces()) {
            if (wsName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean generatePartitionImage(PartitionModel pm, String wsName) throws IOException {
        String[] text;
        Color[] colors;
        Partition partition = pm.getSelectedPartition();

        if (partition != null) {
            text = new String[partition.getParts().length];
            colors = new Color[partition.getParts().length];
            for (int i = 0; i < colors.length; i++) {
                text[i] = partition.getParts()[i].getDisplayName();
                colors[i] = partition.getParts()[i].getColor();
            }

            String max = getMaxText(text);

            BufferedImage bInformation = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

            Font font = new Font("Verdana", Font.BOLD, 12);
            FontMetrics fMetrics = bInformation.getGraphics().getFontMetrics();
            int fontWidht = fMetrics.stringWidth(max);
            int fontHeight = fMetrics.getHeight();

            int ancho = 3 + 17 + 2 + (int) (fontWidht * 1.2) + 3;
            int alto = 3 + (text.length * (fontHeight + 3)) + 3;

            BufferedImage bi = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = bi.createGraphics();
            g2d.setFont(font);
            g2d.setBackground(Color.WHITE);

            int x = 3, y = 3;

            for (int i = 0; i < colors.length; i++) {
                g2d.setColor(colors[i]);
                g2d.fillOval(x, y, 17, 17);
                g2d.setColor(Color.BLACK);
                g2d.drawString(text[i], (x + 19), y + 14);
                y += fontHeight + 3;
            }

            File f = new File(projectPath.getAbsoluteFile() + File.separator
                    + wsName + File.separator + "imgDescriptor.png");

            ImageIO.write(bi, "PNG", f);
            return true;
        } else {
            return false;
        }
    }

    private String getMaxText(String[] messages) {
        String max;

        max = messages[0];

        for (int i = 1; i < messages.length; i++) {
            if (messages[i].length() > max.length()) {
                max = messages[i];
            }
        }

        return max;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        currentWorkSpace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return currentWorkSpace;
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName.replace(" ", "");
    }

    public Workspace[] getAllWorkspaces() {
        return allWorkspaces;
    }

    public void setAllWorkspaces(Workspace[] workspaces) {
        this.allWorkspaces = workspaces;
    }

    /**
     * @return the path
     */
    public File getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(File path) {
        this.path = path;
    }

    public String getMessage(String key) {
        return NbBundle.getMessage(WebSiteExporter.class, key);
    }

    /**
     * @return the selectedWorkspaces
     */
    public String[] getSelectedWorkspaces() {
        return selectedWorkspaces;
    }

    /**
     * @param selectedWorkspaces the selectedWorkspaces to set
     */
    public void setSelectedWorkspaces(String[] selectedWorkspaces) {
        this.selectedWorkspaces = selectedWorkspaces;
    }

    /**
     * @return the exportAttributes
     */
    public boolean isExportAttributes() {
        return exportAttributes;
    }

    /**
     * @param exportAttributes the exportAttributes to set
     */
    public void setExportAttributes(boolean exportAttributes) {
        this.exportAttributes = exportAttributes;
    }

    /**
     * @return the exportColors
     */
    public boolean isExportColors() {
        return exportColors;
    }

    /**
     * @param exportColors the exportColors to set
     */
    public void setExportColors(boolean exportColors) {
        this.exportColors = exportColors;
    }

    /**
     * @return the exportDynamic
     */
    public boolean isExportDynamic() {
        return exportDynamic;
    }

    /**
     * @param exportDynamic the exportDynamic to set
     */
    public void setExportDynamic(boolean exportDynamic) {
        this.exportDynamic = exportDynamic;
    }

    /**
     * @return the exportPosition
     */
    public boolean isExportPosition() {
        return exportPosition;
    }

    /**
     * @param exportPosition the exportPosition to set
     */
    public void setExportPosition(boolean exportPosition) {
        this.exportPosition = exportPosition;
    }

    /**
     * @return the exportSize
     */
    public boolean isExportSize() {
        return exportSize;
    }

    /**
     * @param exportSize the exportSize to set
     */
    public void setExportSize(boolean exportSize) {
        this.exportSize = exportSize;
    }
}