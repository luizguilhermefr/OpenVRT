package unioeste.br.openvrt.file;

import java.io.File;
import java.util.ArrayList;

public class ShapeFinder implements Runnable {

    private File startDir;

    private final static String[] PATTERNS = {".json", ".geojson"};

    private ArrayList<File> matches;

    private ShapeFinderCallback callback;

    public ShapeFinder(File startDir, ShapeFinderCallback callback) {
        this.startDir = startDir;
        this.callback = callback;
        this.matches = new ArrayList<>();
    }

    private Boolean fileMatches(File file) {
        for (String pattern : PATTERNS) {
            if (file.getName().endsWith(pattern)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private void walkDir(File startPoint) {
        File[] listFile = startPoint.listFiles();
        if (listFile != null) {
            for (File file : listFile) {
                System.out.println(file.getAbsolutePath());
                if (file.isDirectory()) {
                    walkDir(file);
                } else if (fileMatches(file)) {
                    matches.add(file);
                }
            }
        }
    }

    @Override
    public void run() {
        walkDir(startDir);
        callback.onShapesFound(matches);
    }
}
