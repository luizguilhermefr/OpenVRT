package unioeste.br.openvrt.file;

public interface PrescriptionMapFinderCallback {
    void onSearchEnded();

    void onShapeDiscovered(String file);
}
