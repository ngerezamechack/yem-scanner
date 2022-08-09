/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yem.scanner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 *
 * @author NGEREZA
 */
public class Boite {

    ////
    private static Alert alert;
    private static TextArea text;

    public static String css = "";
    private static final Image ICON = new Image(
            Boite.class.getResourceAsStream("/com/yem/scanner/images/Scanner.png")
    );

    //Affiche une exception
    public static void showException(Exception ex, String title) {

        execute(() -> {
            alert = new Alert(AlertType.ERROR);
            if (!css.isBlank()) {
                alert.getDialogPane().getStylesheets().add(css);
            }
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(ICON);

            text = new TextArea();
            text.setEditable(false);
            text.setWrapText(true);
            text.setText(ex.toString());

            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.getDialogPane().setContent(text);
            alert.showAndWait();
        });

    }

    //afficher une erreur
    public static void showInformation(String info, String title, Alert.AlertType type) {

        alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(info);
        if (!css.isBlank()) {
            alert.getDialogPane().getStylesheets().add(css);
        }
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(ICON);

        alert.showAndWait();

    }

    //afficher une boite de confirmation
    public static boolean showConfirmation(String qst, String title) {

        alert = new Alert(Alert.AlertType.CONFIRMATION, qst, ButtonType.YES, ButtonType.NO);
        if (!css.isBlank()) {
            alert.getDialogPane().getStylesheets().add(css);
        }
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(ICON);

        alert.setTitle(title);
        alert.setHeaderText(null);
        return ButtonType.YES == alert.showAndWait().get();
    }

    public static boolean verifier(Object... ob) {

        for (Object o : ob) {
            //textField
            if (o instanceof TextField) {
                TextField tf = (TextField) o;
                if (tf.getText().isBlank()) {
                    Boite.showInformation("Information manquante :\n" + tf.getPromptText(), "", AlertType.WARNING);
                    tf.requestFocus();
                    return false;
                }
            }

            //TextArea
            if (o instanceof TextArea) {
                TextArea ta = (TextArea) o;
                if (ta.getText().isBlank()) {
                    Boite.showInformation("Information manquante :\n" + ta.getPromptText(), "", AlertType.WARNING);
                    ta.requestFocus();
                    return false;
                }
            }

            //comboBox
            if (o instanceof ComboBox) {
                ComboBox<String> cb = (ComboBox<String>) o;
                if (cb.getSelectionModel().isEmpty()) {
                    Boite.showInformation("Information manquante :\n" + cb.getPromptText(), "", AlertType.WARNING);
                    cb.requestFocus();
                    return false;
                }
            }

            //datepicker
            if (o instanceof DatePicker) {
                DatePicker dp = (DatePicker) o;
                if (dp.getEditor().getText().isBlank()) {
                    Boite.showInformation("Information manquante :\n" + dp.getPromptText(), "", AlertType.WARNING);
                    dp.requestFocus();
                    return false;
                }
            }
        }

        return true;
    }

    public static void vider(Object... ob) {

        for (Object o : ob) {
            //TextField
            if (o instanceof TextField) {
                ((TextField) o).clear();
            }

            //TextArea
            if (o instanceof TextArea) {
                ((TextArea) o).clear();
            }

            //comboBox
            if (o instanceof ComboBox) {
                ((ComboBox<String>) o).getSelectionModel().clearSelection();
            }

            //datepicker
            if (o instanceof DatePicker) {
                ((DatePicker) o).setValue(LocalDate.now());
            }

            //ImageViewer
            if (o instanceof ImageView) {
                ((ImageView) o).setImage(null);
            }

            //CheckBox
            if (o instanceof CheckBox) {
                ((CheckBox) o).setSelected(false);
            }

            //ListView
            if (o instanceof ListView) {
                ((ListView) o).getSelectionModel().clearSelection();
            }
        }

    }

    public static void execute(Runnable r) {
        Platform.runLater(r);
    }

    //intervale des dates
    public static int jours(LocalDate date) {
        LocalDate ld = LocalDate.now();
        return Period.between(date, ld).getDays();
    }

    public static long jours(LocalDate date_d, LocalDate date_f) {
        return ChronoUnit.DAYS.between(date_d, date_f);
    }

    public static long minutes(LocalDateTime h1, LocalDateTime h2) {
        return ChronoUnit.MINUTES.between(h1, h2);
    }

    public static int annees(LocalDate date) {

        LocalDate ld = LocalDate.now();
        return Period.between(date, ld).getYears();
    }

    /*
    ************************* LA DATE ****************************
     */
    public static LocalDate toLocalDate(TextField td, TextField tm, TextField ty) {

        int year = Integer.valueOf(ty.getText());
        int month = Integer.valueOf(tm.getText());
        int dayOfMonth = Integer.valueOf(td.getText());

        return LocalDate.of(year, month, dayOfMonth);
    }

    public static void fromLocalDate(LocalDate date, TextField td, TextField tm, TextField ty) {
        ty.setText(String.valueOf(date.getYear()));
        tm.setText(String.valueOf(date.getMonthValue()));
        td.setText(String.valueOf(date.getDayOfMonth()));
    }

    private static LocalDate ld;

    public static void defaultDate(TextField td, TextField tm, TextField ty) {
        ld = LocalDate.now();
        td.setText(String.valueOf(ld.getDayOfMonth()));
        tm.setText(String.valueOf(ld.getMonthValue()));
        ty.setText(String.valueOf(ld.getYear()));
    }

    /*
    **************************** L'HEURE ******************************
     */
    private static LocalTime lt;

    public static LocalTime toLocalTime(TextField th, TextField tmin) {
        int heure = Integer.valueOf(th.getText());
        int min = Integer.valueOf(tmin.getText());
        return LocalTime.of(heure, min);
    }

    public static void fromLocalTime(LocalTime time, TextField th, TextField tmin) {
        th.setText(String.valueOf(time.getHour()));
        tmin.setText(String.valueOf(time.getMinute()));
    }

    public static void defaultTime(TextField th, TextField tmin) {
        lt = LocalTime.now();
        th.setText(String.valueOf(lt.getHour()));
        tmin.setText(String.valueOf(lt.getMinute()));
    }
}
