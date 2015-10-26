package com.triestpa.wifi_direct_messaging;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketTask extends AsyncTask<Void, String, String> {
    private final String TAG = ServerSocketTask.class.getSimpleName();

    private Context context;
    private TextView statusText;
    private String host;
    private int port;

    private TextView updateText;
    private CommunicationUtils.OnSocketTaskCompleted listener;


    public ClientSocketTask(Context context, TextView updateText, CommunicationUtils.OnSocketTaskCompleted listener) {
        this.context = context;
        this.updateText = updateText;
        this.listener = listener;
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

            MainActivity.socketOpen = true;
            MainActivity.keepGoing = true;
            MainActivity.numAttempts = 0;

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(0);

            while (MainActivity.keepGoing) {
                int currentNum = CommunicationUtils.numberBounce(out, in);

                if (currentNum == -1) {
                    MainActivity.keepGoing = false;
                }

                publishProgress("" + currentNum);
            }

            out.close();
            in.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            //Pause for a second on the background thread before retrying
            if (MainActivity.keepRetrying) {
                android.os.SystemClock.sleep(1000);
            }
        }


        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */ finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                        MainActivity.socketOpen = false;
                        return "Socket Closed";
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... num) {
        if (num[0] != null && num[0] != "") {
            updateText.setText(num[0]);
        }
    }

    @Override
    protected void onPostExecute(String message) {
        if (message != null) {
            CommunicationUtils.showToast(message, context);
            Log.d(TAG, message);
        }
        updateText.setText("-1");
        listener.onSocketTaskCompleted();
    }
}