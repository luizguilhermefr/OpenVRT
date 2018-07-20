package unioeste.br.openvrt.file;

import java.io.File;

public class PrescriptionMapFinder implements Runnable {

    private File startDir;

    private final static String[] PATTERNS = {".json", ".geojson"};

    private PrescriptionMapFinderCallback callback;

    public PrescriptionMapFinder(File startDir, PrescriptionMapFinderCallback callback) {
        this.startDir = startDir;
        this.callback = callback;
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
                if (file.isDirectory()) {
                    walkDir(file);
                } else if (fileMatches(file)) {
                    callback.onShapeDiscovered(file.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void run() {
        callback.onSearchStarted();
        walkDir(startDir);
        callback.onSearchEnded();
    }
}
