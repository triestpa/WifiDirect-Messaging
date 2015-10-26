package com.triestpa.wifi_direct_messaging;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ClientSocketTask extends AsyncTask<Void, String, String> {
    private final String TAG = ServerSocketTask.class.getSimpleName();

    private Context context;
    private TextView statusText;
    private String host;
    private int port;

    private TextView updateText;

    public ClientSocketTask(Context context, TextView updateText) {
        this.context = context;
        this.updateText = updateText;
        this.host = MainActivity.ServerAddr;
        this.port = MainActivity.ServerPort;
    }

    @Override
    protected String doInBackground(Void... params) {
        Socket socket = new Socket();

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(0);

            while (MainActivity.keepGoing) {
                int currentNum = CommunicationUtils.numberBounce(out, in);

                if (currentNum == -1) {
                    MainActivity.keepGoing = false;
                }

                publishProgress(""+currentNum);
                TimeUnit.MILLISECONDS.sleep(50);
            }

            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }


        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */ finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                        Log.d(TAG, "Socket Closed");
                        return "Socket Closed";
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
        return "There was probably an error";
    }

    @Override
    protected void onProgressUpdate(String... num) {
        if (num[0] != null && num[0] != "") {
            // showMessage(message[0], context);
            updateText.setText(num[0]);
        }
    }

    @Override
    protected void onPostExecute(String message) {
        if (message != null) {
            CommunicationUtils.showMessage(message, context);
            updateText.setText("-1");
            Log.d(TAG, message);
        }
    }
}