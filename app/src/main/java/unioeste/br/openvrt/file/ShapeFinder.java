package unioeste.br.openvrt.file;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ShapeFinder {

    private File startDir;

    private final static String PATTERN = ".shp";

    private ArrayList<File> matches;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public ShapeFinder(File startDir) {
        this.startDir = startDir;
        this.matches = new ArrayList<>();
    }

    private void walkDir(File startPoint) {
        File[] listFile = startPoint.listFiles();
        if (listFile != null) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    walkDir(file);
                } else if (file.getName().endsWith(PATTERN)) {
                    matches.add(file);
                }
            }
        }
    }

    public Future<ArrayList<File>> find() {
        return executor.submit(() -> {
            walkDir(startDir);
            return matches;
        });
    }
}
