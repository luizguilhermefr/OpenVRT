package unioeste.br.openvrt.exception;

public class InvalidOpenVRTGeoJsonException extends Exception {
    public InvalidOpenVRTGeoJsonException() {
        super("GeoJSON file is invalid. Each feature must contain a valid rate.");
    }
}
