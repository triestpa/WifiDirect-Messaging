package com.triestpa.wifi_direct_messaging;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ServerSocketTask extends AsyncTask<Void, String, String> {
    private final String TAG = ServerSocketTask.class.getSimpleName();

    private Context context;
    private TextView updateText;

    public ServerSocketTask(Context context, TextView updateText) {
        this.context = context;
        this.updateText = updateText;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */

            Log.d(TAG, "Server Listening");
            ServerSocket serverSocket = new ServerSocket(MainActivity.ServerPort);

            Socket client = serverSocket.accept();

            InetAddress clientIP = client.getInetAddress();
            Log.d(TAG, clientIP.toString());

            /**
             * If this code is reached, a client has connected and transferred data
             */

            Log.d(TAG, "Client Connected");

            PrintWriter out =
                    new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));

            String theString = in.readLine();
            publishProgress(theString);

            // Echo the message back
            out.println(theString);

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
            serverSocket.close();
            Log.d(TAG, "Socket Closed");
            return "Socket Closed";
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... num) {
        if (num[0] != null && num[0] != "") {
            //showMessage(num[0], context);
            updateText.setText(num[0]);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            CommunicationUtils.showMessage(result, context);
            updateText.setText("-1");
        }
    }
}