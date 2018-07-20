package unioeste.br.openvrt.file;

public interface PrescriptionMapFinderCallback {
    void onSearchStarted();

    void onSearchEnded();

    void onShapeDiscovered(String file);
}
