package unioeste.br.openvrt.file;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PrescriptionMapReader implements Runnable {

    private String path;

    private OnIOExceptionListener ioExceptionListener = null;

    private OnFileReadListener fileReadListener = null;

    public PrescriptionMapReader(String path) {
        this.path = path;
    }

    public void setOnIOExceptionListener(OnIOExceptionListener ioExceptionListener) {
        this.ioExceptionListener = ioExceptionListener;
    }

    public void setOnFileReadListener(OnFileReadListener fileReadListener) {
        this.fileReadListener = fileReadListener;
    }

    @NonNull
    private String readContents() throws IOException {
        File file = new File(path);
        StringBuilder stringBuilder = new StringBuilder();
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        do {
            line = br.readLine();
            if (line == null) {
                break;
            }
            stringBuilder.append(line);
            br.close();
        } while (true);

        return stringBuilder.toString();
    }

    @Override
    public void run() {
        try {
            String contents = readContents();
            if (fileReadListener != null) {
                fileReadListener.onFileRead(contents);
            }
        } catch (IOException e) {
            if (ioExceptionListener != null) {
                ioExceptionListener.onIOException(e);
            }
        }
    }

    public interface OnIOExceptionListener {
        void onIOException(IOException e);
    }

    public interface OnFileReadListener {
        void onFileRead(String contents);
    }
}
