package mghj.imdb.bussiness;


import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;

@Component
public class FileDownloader {
    private static final String BASE_URL = "https://datasets.imdbws.com/";
    private static final String[] FILES = {
            "title.basics.tsv.gz",
            "title.akas.tsv.gz",
            "title.crew.tsv.gz",
            "title.episode.tsv.gz",
            "title.principals.tsv.gz",
            "title.ratings.tsv.gz",
            "name.basics.tsv.gz"
    };

    public void downloadFiles(String directory) throws Exception {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();

        for (String file : FILES) {
            File localFile = new File(directory, file);
            if (!localFile.exists()) {
                System.out.println("Downloading: " + file);
                FileUtils.copyURLToFile(new URL(BASE_URL + file), localFile);
            } else {
                System.out.println("File already exists: " + file);
            }
        }
    }
}
