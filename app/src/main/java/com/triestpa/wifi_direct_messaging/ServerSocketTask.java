package com.triestpa.wifi_direct_messaging;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketTask extends AsyncTask<Void, Integer, String> {
    private final String TAG = ServerSocketTask.class.getSimpleName();

    private MainActivity context;
    private MainActivityFragment fragment;
    private CommunicationUtils.OnSocketTaskCompleted listener;

    public ServerSocketTask(MainActivity context, MainActivityFragment fragment, CommunicationUtils.OnSocketTaskCompleted listener) {
        this.context = context;
        this.fragment = fragment;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        fragment.socketInfo.setText("Server Listening");
        Log.d(TAG, "Server Listening");
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(MainActivity.ServerPort);
            Socket client = serverSocket.accept();

            /**
             * If this code is reached, a client has connected and transferred data
             */
            Log.d(TAG, "Client Connected");
            publishProgress(CommunicationUtils.CONNECTION_ESTABLISHED_CODE);

            MainActivity.keepGoing = true;
            MainActivity.numAttempts = 0;

            InetAddress clientIP = client.getInetAddress();
            Log.d(TAG, clientIP.toString());


            PrintWriter out =
                    new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));

            while (MainActivity.keepGoing) {
                int currentNum = CommunicationUtils.numberBounce(out, in);

                if (currentNum == -1) {
                    MainActivity.keepGoing = false;
                }
                publishProgress(currentNum);
            }

            out.close();
            in.close();
            serverSocket.close();
            Log.d(TAG, "Socket Closed");
            return "Socket Closed";
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            //Pause for a second on the background thread before retrying
            if (MainActivity.keepRetrying) {
                android.os.SystemClock.sleep(1000);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... num) {
        if (num[0] == CommunicationUtils.CONNECTION_ESTABLISHED_CODE) {
            fragment.socketInfo.setText("Socket Connection Opened to Client");
        }
        else {
            fragment.currentNumber.setText(""+num[0]);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            CommunicationUtils.showToast(result, context);
        }
        fragment.currentNumber.setText("-1");
        listener.onSocketTaskCompleted();
    }
}