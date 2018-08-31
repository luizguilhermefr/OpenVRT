package unioeste.br.openvrt.connection;

public class IdFactory {
    private static IdFactory ourInstance = null;

    private int lastId = 0;

    private IdFactory() {
        //
    }

    public static IdFactory getInstance() {
        if (ourInstance == null) {
            ourInstance = new IdFactory();
        }
        return ourInstance;
    }

    public synchronized int next() {
        lastId++;
        return lastId;
    }
}
