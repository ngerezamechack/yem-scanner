/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yem.scanner;

import com.asprise.imaging.core.Imaging;
import com.asprise.imaging.core.Request;
import com.asprise.imaging.core.RequestOutputItem;
import com.asprise.imaging.core.Result;
import com.asprise.imaging.core.scan.twain.CapOptions;
import com.asprise.imaging.core.scan.twain.Source;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import com.yem.scanner.cells.CellItemScan;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 *
 * @author NGEREZA
 */
public class YemScanner extends BorderPane {

    private Imaging imaging;
    private ObservableList<ScanListener> obs = FXCollections.observableArrayList();
    private File dir = new File(System.getProperty("java.io.tmpdir") + File.separator + "scan" + File.separator);

    private Alert stage = new Alert(Alert.AlertType.NONE, "");
    private DialogPane dp = new DialogPane();

    @FXML
    private ImageView imv;

    @FXML
    private ListView<String> tlist;
    @FXML
    private ProgressIndicator pg;

    //
    @FXML
    private ComboBox<String> tscanner;
    @FXML
    private ComboBox<CapOptions.CapOptionItem> tsourceP;

    //
    private Source scanner;

    public YemScanner() {
        //
        enleverCle();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/yem/scanner/vues/panneau.fxml"));
        try {
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
            initVues();
            initDir();
        } catch (Exception e) {
            Boite.showException(e, "load");
        }
    }

    private void initDir() throws Exception {
        if (!dir.exists()) {
            dir.mkdir();
        }

        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
        }
    }

    private void initVues() {

        imaging = new Imaging("Scan", 0);

        pg.setVisible(false);
        dp.setContent(this);

        stage.setDialogPane(dp);
        //
        stage.setResizable(false);
        stage.setOnCloseRequest((e) -> {
            e.consume();
        });
        setIcon();
        tlist.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv != null && !nv.isBlank()) {
                imv.setImage(new Image(nv));
            }
        });
        tlist.setCellFactory((ListView<String> p) -> new ListCell<>() {
            private Node node = null;

            @Override
            protected void updateItem(String t, boolean bln) {
                super.updateItem(t, bln); //To change body of generated methods, choose Tools | Templates.
                if (t != null) {
                    node = (Node) new CellItemScan(t, tlist.getItems());
                } else {
                    node = null;
                }
                setGraphic(node);
            }

        });
        //
        tscanner.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldv, String newv) -> {
            if (newv != null && !newv.isBlank()) {
                scanner = imaging.getSource(newv, false, "all", false, false, "all");
                if (scanner != null) {
                    loadSourcePapers();
                    //
                    if (!scanner.getCaps().isEmpty()) {
                        scanner.getCaps().forEach((Source.Capability t) -> {
                            System.out.println(t.getName() + " : " + t.getValue() + " = " + t.isSupported() + " :: " + t.getCurrentValue());
                        });
                        //
                    }
                }
            }
        });

        rafraichir();
    }

    private void setIcon() {
        try {
            ((Stage) dp.getScene().getWindow()).getIcons().add(
                    new Image(getClass().getResourceAsStream("/com/yem/scanner/images/Scanner.png"))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void scanner() {

        enleverCle();

        if (Boite.verifier(tscanner, tsourceP)) {
            Request request = new Request();
            request.setI18n("lang", "fr");
            request.addOutputItem(new RequestOutputItem(Imaging.OUTPUT_SAVE, Imaging.FORMAT_PNG)
                    .setSavePath("${TMP}\\\\${TMS}${EXT}"));

            CapOptions.CapOptionItem it = tsourceP.getValue();

            System.out.println("Source Papier : " + it.getValue() + " ," + it.getLabel() + " " + it.getValueAsInt());

            if (it.getValueAsInt() != 0) {
                Optional.ofNullable(scanner.getCap("CAP_FEEDERENABLED"))
                        .ifPresent((Source.Capability adf) -> {
                            System.out.println(adf.getName() + " : " + adf.getValue() + " " + adf.isSupported());
                            request.setTwainCap(adf.getCode(), it.getValue());
                        });
            }

            Optional.ofNullable(imaging.scan(request, scanner.getSourceName(), false, false))
                    .ifPresent((Result result) -> {
                        result.getImageFiles().forEach((File t) -> {
                            try {
                                tlist.getItems().add(t.toURI().toURL().toString());
                            } catch (MalformedURLException ex) {
                                ex.printStackTrace();
                            }
                        });
                    });

        }

    }

    //
    private Service<File> taskPDF;

    @FXML
    private void terminer() {
        listeFile = tlist.getItems();
        if (!listeFile.isEmpty()) {
            taskPDF = new Service<File>() {
                @Override
                protected Task<File> createTask() {
                    return new Task<File>() {
                        @Override
                        protected File call() throws Exception {
                            return generatePDF();
                        }
                    };
                }
            };
            taskPDF.setOnScheduled((e) -> {
                pg.setVisible(true);
            });
            taskPDF.setOnFailed((e) -> {
                pg.setVisible(false);
                Boite.showException(new Exception(taskPDF.getException()), "Pdf..");
            });
            taskPDF.setOnSucceeded((e) -> {
                pg.setVisible(false);
                obs.forEach((sl) -> {
                    sl.scanFinish(taskPDF.getValue());
                });
                rafraichir();
                stage.getDialogPane().getScene().getWindow().hide();

            });
            taskPDF.start();
        } else {

        }
    }

    public void show() {

        this.getStylesheets().forEach(st -> {
            Boite.css = st;
        });
        rafrachirScanner();

        if (!stage.isShowing()) {
            stage.showAndWait();
        }
    }

    //
    public void addScanListener(ScanListener sl) {
        this.obs.add(sl);
    }

    //
    @FXML
    public void rafraichir() {
        tlist.getItems().clear();
        imv.setImage(null);
        enleverCle();
    }

    @FXML
    private void rafrachirScanner() {
        //
        tscanner.getItems().clear();
        tsourceP.getItems().clear();
        //
        List<Source> s = imaging.scanListSources(true, "all", true, true);

        s.forEach((Source t) -> {
            tscanner.getItems().add(t.getSourceName());
        });

        if (!s.isEmpty()) {
            tscanner.getSelectionModel().selectFirst();
        }
    }

    private void loadSourcePapers() {

        tsourceP.getItems().clear();
        //

        //
        tsourceP.getItems().addAll(CapOptions.getPaperSource());
        tsourceP.getSelectionModel().selectFirst();
    }

    @FXML
    private void fermer() {
        if (Boite.showConfirmation("Fermer?", "Attention..")) {
            stage.getDialogPane().getScene().getWindow().hide();
            rafraichir();
        }
    }

    //
    private ObservableList<String> listeFile;

    private File generatePDF() throws Exception {
        String f = dir.getAbsolutePath() + File.separator + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".pdf";
        FileOutputStream fos = new FileOutputStream(f);
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, fos);

        doc.open();
        for (String s : listeFile) {
            com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(s);
            doc.newPage();

            image.scaleToFit(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
            image.setAlignment(com.lowagie.text.Image.ALIGN_MIDDLE);

            doc.add(image);
        }
        doc.close();
        return new File(f);
    }

    //enlever la clé
    private void enleverCle() {
        try {
            String regArg = "DELETE \"HKEY_CURRENT_USER\\Software\\Lab Asprise!\" /f";

            CommandLine cl = new CommandLine("reg")
                    .addArguments(regArg);

            DefaultExecuteResultHandler rh = new DefaultExecuteResultHandler();
            ExecuteWatchdog wd = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
            Executor exec = new DefaultExecutor();

            PumpStreamHandler psh = new PumpStreamHandler(System.out);

            exec.setStreamHandler(psh);
            exec.setWatchdog(wd);

            exec.execute(cl, rh);
            rh.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
