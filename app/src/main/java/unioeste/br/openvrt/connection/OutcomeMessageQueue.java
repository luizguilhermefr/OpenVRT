package unioeste.br.openvrt.connection;

import unioeste.br.openvrt.connection.exception.MessageRefusedException;
import unioeste.br.openvrt.connection.exception.MessageTimeoutException;
import unioeste.br.openvrt.connection.message.AcknowledgedMessage;
import unioeste.br.openvrt.connection.message.Message;
import unioeste.br.openvrt.connection.message.RefusedMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class OutcomeMessageQueue extends Thread {

    private static final int DEFAULT_CAPACITY = 5;

    private int retries = 3;

    private int timeout = 1000;

    private int capacity = DEFAULT_CAPACITY;

    private Message messageBeingProcessed;

    private ArrayBlockingQueue<Message> queue;

    private ArrayList<AcknowledgedMessage> acknowledgements;

    private boolean shouldDie = false;

    private OutputStream ostream;

    private ErrorListener errorListener;

    OutcomeMessageQueue(OutputStream ostream) {
        this.ostream = ostream;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.acknowledgements = new ArrayList<>(capacity);
        this.errorListener = new ErrorListener();
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

    private MessageResponse checkAcknowlegement() {
        for (int i = 0; i < acknowledgements.size(); ++i) {
            AcknowledgedMessage ack = acknowledgements.get(i);
            if (ack.getAcknowledgedId() == messageBeingProcessed.getId()) {
                acknowledgements.remove(i);
                return ack instanceof RefusedMessage ? MessageResponse.ACK_NEGATIVE : MessageResponse.ACK_POSITIVE;
            }
        }
        return MessageResponse.ACK_TIMEOUT;
    }

    private void sendWaitRetry() throws MessageTimeoutException, MessageRefusedException {
        for (int i = 0; i < retries; i++) {
            try {
                new Thread(new WriterRunnable(messageBeingProcessed)).start();
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                MessageResponse code = checkAcknowlegement();
                switch (code) {
                    case ACK_POSITIVE:
                        return;
                    case ACK_NEGATIVE:
                        throw new MessageRefusedException();
                }
            }
        }
        throw new MessageTimeoutException();
    }

    private synchronized void ensureAckCapacity() {
        if (acknowledgements.size() == capacity) {
            acknowledgements.remove(0);
        }
    }

    synchronized void submitAck(AcknowledgedMessage ack) {
        ensureAckCapacity();
        acknowledgements.add(ack);
        if (ack.getAcknowledgedId() == messageBeingProcessed.getId()) {
            interrupt();
        }
    }

    private void onResponse(MessageResponse response) {
        messageBeingProcessed.onResponse(response);
    }

    @Override
    public void run() {
        while (!shouldDie) {
            if (!queue.isEmpty()) {
                messageBeingProcessed = queue.poll();
                try {
                    sendWaitRetry();
                    onResponse(MessageResponse.ACK_POSITIVE);
                } catch (MessageTimeoutException e) {
                    onResponse(MessageResponse.ACK_TIMEOUT);
                } catch (MessageRefusedException e) {
                    onResponse(MessageResponse.ACK_NEGATIVE);
                } finally {
                    messageBeingProcessed = null;
                }
            }
        }
    }

    public enum MessageResponse {
        ACK_NEGATIVE, ACK_POSITIVE, ACK_TIMEOUT, ERROR_ON_SEND
    }

    private class ErrorListener {
        void onErrorThrown(Exception e) {
            onResponse(MessageResponse.ERROR_ON_SEND);
        }
    }

    private class WriterRunnable implements Runnable {
        private Message frozenMessage;

        WriterRunnable(Message frozenMessage) {
            this.frozenMessage = frozenMessage;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(100);
                ostream.write(frozenMessage.toBytes());
            } catch (IOException e) {
                errorListener.onErrorThrown(e);
            } catch (InterruptedException ignored) {
                //
            }
        }
    }
}
