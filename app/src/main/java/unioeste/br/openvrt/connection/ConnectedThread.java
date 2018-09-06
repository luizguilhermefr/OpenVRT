package unioeste.br.openvrt.connection;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.exception.InvalidMessageException;
import unioeste.br.openvrt.connection.exception.UnexpectedMessageException;
import unioeste.br.openvrt.connection.message.AcknowledgedMessage;
import unioeste.br.openvrt.connection.message.Message;
import unioeste.br.openvrt.connection.message.factory.MessageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private static ConnectedThread instance;

    private OutcomeMessageQueue outcomeMessageQueue;

    private OnMessageReceivedListener messageReceivedListener;

    private OnSocketReceiveErrorListener socketReceiveErrorListener;

    private OnSocketSendErrorListener socketSendErrorListener;

    private InputStream istream;

    private OutputStream ostream;

    private boolean shouldDie = false;

    private ConnectedThread() {
        //
    }

    static ConnectedThread getInstance() {
        if (instance == null) {
            instance = new ConnectedThread();
        }

        return instance;
    }

    void setConnection(@NonNull InputStream istream, @NonNull OutputStream ostream) {
        this.istream = istream;
        this.ostream = ostream;
        initializeOutcomeMessageQueue();
    }

    private void initializeOutcomeMessageQueue() {
        cancelOutcomeMessageQueue();
        outcomeMessageQueue = new OutcomeMessageQueue(ostream);
        outcomeMessageQueue.start();
    }

    private void cancelOutcomeMessageQueue() {
        if (outcomeMessageQueue != null) {
            outcomeMessageQueue.cancel();
            outcomeMessageQueue = null;
        }
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener messageReceivedListener) {
        this.messageReceivedListener = messageReceivedListener;
    }

    public void setOnSocketErrorListener(OnSocketReceiveErrorListener socketErrorListener) {
        this.socketReceiveErrorListener = socketErrorListener;
    }

    public void setOnSocketSendErrorListener(OnSocketSendErrorListener socketSendErrorListener) {
        this.socketSendErrorListener = socketSendErrorListener;
    }

    private void onMessageReceived(byte[] buffer) {
        try {
            Message message = MessageFactory.make(buffer);
            if (message instanceof AcknowledgedMessage) {
                outcomeMessageQueue.submitAck((AcknowledgedMessage) message);
            } else if (messageReceivedListener != null) {
                messageReceivedListener.onMessageReceived(message);
            }
        } catch (InvalidMessageException | UnexpectedMessageException e) {
            // TODO: Send refused message.
        }
    }

    private void onSocketReceiveError() {
        if (socketReceiveErrorListener != null) {
            socketReceiveErrorListener.onSocketReceiveError();
        }
    }

    private void onSocketSendError() {
        if (socketSendErrorListener != null) {
            socketSendErrorListener.onSocketSendError();
        }
    }

    public void cancel() {
        shouldDie = true;
    }


    private void bury() {
        try {
            cancelOutcomeMessageQueue();
            istream.close();
            ostream.close();
        } catch (IOException ignored) {
            //
        }
    }

    private void checkConnectionReady() throws IllegalArgumentException {
        if (istream == null || ostream == null || outcomeMessageQueue == null) {
            throw new IllegalArgumentException("Must call setConnection() before start.");
        }
    }

    private boolean messageReady() throws IOException {
        return istream.available() >= Message.MSG_LEN;
    }

    private void readMessage() throws IOException {
        byte[] buffer = new byte[Message.MSG_LEN];
        int bytesRead = istream.read(buffer, 0, Message.MSG_LEN);
        if (bytesRead == Message.MSG_LEN) {
            onMessageReceived(buffer);
        } else {
            onSocketReceiveError();
        }
    }

    void write(Message message) {
        outcomeMessageQueue.add(message);
    }

    @Override
    public void run() {
        checkConnectionReady();
        while (!shouldDie) {
            try {
                if (messageReady()) {
                    readMessage();
                }
            } catch (IOException e) {
                onSocketReceiveError();
            }
        }
        bury();
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(Message message);
    }

    public interface OnSocketReceiveErrorListener {
        void onSocketReceiveError();
    }

    public interface OnSocketSendErrorListener {
        void onSocketSendError();
    }
}
