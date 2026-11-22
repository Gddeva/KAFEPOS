package com.kafepos;

import java.io.File;
import java.net.URL;

import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Memastikan style.css (tema cokelat KAFEPOS) selalu terpasang.
 *
 * Di FXML, stylesheet dipanggil dengan path relatif (@../css/style.css). Jika
 * aplikasi dijalankan dengan susunan classpath yang berbeda (mis. lewat tombol Run
 * VS Code, bukan Maven), file CSS tidak ketemu dan JavaFX diam-diam memakai tampilan
 * bawaan yang polos. Kelas ini mencari style.css di beberapa lokasi lalu memasangnya.
 */
public final class Styles {

    private static final String[] CLASSPATH_LOCATIONS = {
            "/css/style.css",
            "/resources/css/style.css"
    };

    private static final String[] FILE_LOCATIONS = {
            "src/resources/css/style.css",
            "resources/css/style.css"
    };

    private Styles() {}

    public static void ensure(Scene scene) {
        String url = findStylesheet();
        if (url == null) {
            System.err.println("PERINGATAN: style.css tidak ditemukan. "
                    + "Tampilan akan polos. Jalankan dari folder project "
                    + "(mvn clean javafx:run) atau pastikan src/resources/css/style.css ada.");
            return;
        }

        Parent root = scene.getRoot();
        if (!scene.getStylesheets().contains(url) && !root.getStylesheets().contains(url)) {
            root.getStylesheets().add(url);
        }
    }

    private static String findStylesheet() {
        for (String path : CLASSPATH_LOCATIONS) {
            URL url = Styles.class.getResource(path);
            if (url != null) {
                return url.toExternalForm();
            }
        }
        for (String path : FILE_LOCATIONS) {
            File file = new File(path);
            if (file.isFile()) {
                return file.toURI().toString();
            }
        }
        return null;
    }
}
