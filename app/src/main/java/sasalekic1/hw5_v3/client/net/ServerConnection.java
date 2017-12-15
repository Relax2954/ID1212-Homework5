package sasalekic1.hw5_v3.client.net;

/**
 * Created by SasaLekic on 15/12/17.
 */


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import Protocol.common.Message;
import Protocol.common.MessageException;
import Protocol.common.MsgType;

public class ServerConnection implements Runnable
{
    private final int SHOW_UI = 123123;
    private final String host;
    private final int port;
    protected final ResultAppender gui;
    private final LinkedBlockingQueue<Message> msgg =
            new LinkedBlockingQueue<>();
    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * Creates a new instance. Does not connect to the server.
     *
     * @param gui  The client gui object.
     * @param host The  server host name.
     * @param port The  server port number.
     */
    public ServerConnection(final ResultAppender gui, String host, int port)
    {
        this.host = host;
        this.port = port;
        this.gui = gui;
    }

    /**
     * The run method of the communication thread. First connects to
     * the server using the host name and port number specified in the
     * constructor. Second waits to receive a string from the gui and sends
     * that to the server.
     */
    @Override
    public void run()
    {
        callServer();
    }

    /**
     * Connects to the server using the host name and port number
     * specified in the constructor.
     */
    public void connect()
    {
        try
        {
            Socket clientSocket = new Socket(host, port);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

        } catch (UnknownHostException e)
        {
            System.err.println("Don't know about host: " + host + ".");
            System.exit(1);
        } catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: "
                    + host + ".");
            e.printStackTrace();
            System.exit(1);

        }
    }

    /**
     * Used to submit a string.
     *
     * @param text The string to be checked.
     */
    public void mssging(String text)
    {
        if(text.contains("start")) {
            Message mysend = new Message(MsgType.START, "game");
            msgg.add(mysend);
        }
        else{
            Message mysend = new Message(MsgType.GUESS, text);
            msgg.add(mysend);
        }
    }

    /**
     * Waits to receive a string from the server
     */
    void callServer() {
        while (true) {
            String result = "proerski";
            try {
                Message toServer = msgg.take(); //IF I TAKE AN OBJECT
                out.writeObject(toServer);
                out.flush();
                try {
                    result = new String(extractMsgBody((Message) in.readObject()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException | IOException e) {
                result = "Failed, " + e.getMessage();
            }

            gui.showResult(result);
        }
    }

    private String extractMsgBody(Message msg) {
        if (msg.getType() != MsgType.NETWORKING) {
            throw new MessageException("Received corrupt message: " + msg);
        }
        return msg.getBody();
    }
}