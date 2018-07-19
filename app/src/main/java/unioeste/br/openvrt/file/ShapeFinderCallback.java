package unioeste.br.openvrt.file;

import java.io.File;
import java.util.ArrayList;

public interface ShapeFinderCallback {
    void onShapesFound(ArrayList<File> files);
}
