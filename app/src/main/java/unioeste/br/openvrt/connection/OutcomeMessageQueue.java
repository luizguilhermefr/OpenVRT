package unioeste.br.openvrt.connection;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.message.AcknowledgedMessage;
import unioeste.br.openvrt.connection.message.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class OutcomeMessageQueue extends Thread {

    private int retries = 3;

    private int timeout = 1500;

    private int capacity = 5;

    private ArrayBlockingQueue<Message> queue;

    private ArrayList<AcknowledgedMessage> acknowledgements;

    private boolean shouldDie = false;

    private OutputStream ostream;

    public OutcomeMessageQueue(OutputStream ostream) {
        queue = new ArrayBlockingQueue<>(capacity);
        acknowledgements = new ArrayList<>(capacity);
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setTimeout(int milis) {
        this.timeout = milis;
    }

    public synchronized void add(Message message) {
        queue.add(message);
    }

    public void cancel() {
        shouldDie = true;
    }

    private boolean checkAcknowlegement(int id) {
        return false;
    }

    private void sendWaitRetry(@NonNull Message message) {
        for (int i = 0; i < retries; i++) {
            write(message);
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                checkAcknowlegement(message.getId());
            }
        }
    }

    private void submitAck(AcknowledgedMessage ack) {
        acknowledgements.add(ack);
        if (true) { // TODO: If there is a message being processed with the id of the acknowledgement.
            // TODO: Set an accept/reject first
            interrupt();
        }
    }

    private void write(@NonNull Message message) {
        try {
            ostream.write(message.toBytes());
        } catch (IOException e) {
            // Do something
        }
    }

    @Override
    public void run() {
        while (!shouldDie) {
            if (!queue.isEmpty()) {
                Message message = queue.poll();
                sendWaitRetry(message);
            }
        }
    }
}
