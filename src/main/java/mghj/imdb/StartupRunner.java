package mghj.imdb;


import mghj.imdb.bussiness.DataLoader;
import mghj.imdb.bussiness.FileDownloader;
import mghj.imdb.bussiness.IMDbTitleFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupRunner implements CommandLineRunner {
    @Autowired
    private FileDownloader fileDownloader;

//    @Autowired
//    private DataLoader dataLoader;

    @Autowired
    IMDbTitleFinder titleFinder;

    @Override
    public void run(String... args) throws Exception {
        titleFinder.findMoviesBySameDirectorWriter();

//        String dataDir = "data";
//        fileDownloader.downloadFiles(dataDir);
//        dataLoader.loadAllData();
    }
}
