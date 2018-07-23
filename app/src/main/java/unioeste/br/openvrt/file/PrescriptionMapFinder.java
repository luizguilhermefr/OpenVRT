package unioeste.br.openvrt.file;

import java.io.File;

public class PrescriptionMapFinder implements Runnable {

    private File startDir;

    private final static String[] PATTERNS = {".json", ".geojson"};

    private OnSearchStartedListener searchStartedListener = null;

    private onSearchEndedListener searchEndedListener = null;

    private OnShapeDiscoveredListener shapeDiscoveredListener = null;

    public PrescriptionMapFinder(File startDir) {
        this.startDir = startDir;
    }

    public void setOnSearchStartedListener(OnSearchStartedListener searchStartedListener) {
        this.searchStartedListener = searchStartedListener;
    }

    public void setOnSearchEndedListener(onSearchEndedListener searchEndedListener) {
        this.searchEndedListener = searchEndedListener;
    }

    public void setOnShapeDiscoveredListener(OnShapeDiscoveredListener shapeDiscoveredListener) {
        this.shapeDiscoveredListener = shapeDiscoveredListener;
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
                    if (shapeDiscoveredListener != null) {
                        shapeDiscoveredListener.onShapeDiscovered(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        if (searchStartedListener != null) {
            searchStartedListener.onSearchStarted();
        }

        walkDir(startDir);

        if (searchEndedListener != null) {
            searchEndedListener.onSearchEnded();
        }
    }

    public interface OnSearchStartedListener {
        void onSearchStarted();
    }

    public interface onSearchEndedListener {
        void onSearchEnded();
    }

    public interface OnShapeDiscoveredListener {
        void onShapeDiscovered(String file);
    }
}
