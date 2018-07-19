package unioeste.br.openvrt.file;

import java.io.File;
import java.util.ArrayList;

public interface PrescriptionMapFinderCallback {
    void onPrescriptionMapsFound(ArrayList<String> files);
}
