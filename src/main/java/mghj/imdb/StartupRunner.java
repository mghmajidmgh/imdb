package mghj.imdb;


import mghj.imdb.bussiness.DataLoader;
import mghj.imdb.bussiness.FileDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {
    @Autowired
    private FileDownloader fileDownloader;

    @Autowired
    private DataLoader dataLoader;

    @Override
    public void run(String... args) throws Exception {
        String dataDir = "data";
        fileDownloader.downloadFiles(dataDir);
        dataLoader.loadAllData();
    }
}
