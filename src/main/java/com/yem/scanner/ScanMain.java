/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yem.scanner;

import java.io.File;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author NGEREZA
 */
public class ScanMain extends Application {
    
    @Override
    public void start(Stage stage) {
        YemScanner ys = new YemScanner();
        ys.addScanListener(new ScanListener() {
            @Override
            public void scanFinish(File file) {
                getHostServices().showDocument(file.getAbsolutePath());
            }

            @Override
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
        ys.show();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
