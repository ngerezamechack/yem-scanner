/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yem.scanner.cells;

import com.yem.scanner.Boite;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller class
 *
 * @author NGEREZA
 */
public class CellItemScan extends BorderPane {

    @FXML
    private Label tfile;
    private String filename;
    //
    private ObservableList<String> lists;

    public CellItemScan(String filename, ObservableList<String> lists) {
        this.lists = lists;
        this.filename = filename;
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/yem/scanner/vues/cell_item.fxml")
        );
        loader.setRoot(this);
        loader.setController(this);
        //
        try {
            loader.load();
            tfile.setText(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimer(ActionEvent event) {
        if (Boite.showConfirmation("Enlever l'élément?", "Attention..")) {
            lists.remove(lists.indexOf(filename));
        }
    }

}
