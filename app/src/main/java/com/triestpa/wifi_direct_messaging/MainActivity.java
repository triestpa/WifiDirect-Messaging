package com.triestpa.wifi_direct_messaging;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    WifiP2pInfo mConnectionInfo;
    MainActivity mActivity;
    WiFiDirectBroadcastReceiver mReceiver;

    List mPeers = new ArrayList();

    boolean isServer = false;
    boolean wifiP2pEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mActivity = this;

    }

    public void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank.  Code for peer discovery goes in the
                // onReceive method, detailed below.
            }

            @Override
            public void onFailure(int reasonCode) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
                Log.e(TAG, "Peer Discovery Failed");
            }
        });
    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void setIsWifiP2pEnabled(boolean isEnabled) {
        wifiP2pEnabled = isEnabled;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectToDevice() {

        if (mPeers != null && !mPeers.isEmpty()) {

            // Picking the first device found on the network.
            WifiP2pDevice device = (WifiP2pDevice) mPeers.get(0);

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.

                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    protected WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            mPeers.clear();
            mPeers.addAll(peerList.getDeviceList());

            if (mPeers.size() == 0) {
                Log.d(TAG, "No devices found");
                return;
            } else {
                Log.d(TAG, mPeers.size() + " Peer(s) Found");
            }
        }
    };


    public void startServer() {
        if (isServer) {
            new ServerSocketTask(this).execute();
        }
    }

    public void startClient(String host, int port, String message) {
        if (!isServer) {
            new ClientSocketTask(this, host, port, message).execute();
        }
    }

    public static class ServerSocketTask extends AsyncTask<Void, String, String> {
        private final String TAG = ServerSocketTask.class.getSimpleName();

        private Context context;
        int len;
        byte buf[] = new byte[1024];

        public ServerSocketTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */

                Log.d(TAG, "Server Listening");
                ServerSocket serverSocket = new ServerSocket(8888);

                while(true) {
                    Socket client = serverSocket.accept();

                    InetAddress clientIP = client.getInetAddress();
                    Log.d(TAG, clientIP.toString());

                    /**
                     * If this code is reached, a client has connected and transferred data
                     */

                    Log.d(TAG, "Client Connected");

/*
                    InputStream inputstream = client.getInputStream();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(inputstream, writer, "UTF-8");
                    String theString = writer.toString();
                   // inputstream.close();
*/

                    InputStream inputStream = client.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    String theString  = in.readLine();


                    publishProgress(theString);

                    // Echo the message back
                    OutputStream outputStream = client.getOutputStream();
                    ContentResolver cr = context.getContentResolver();
                    InputStream stringStream = null;
                    stringStream = new ByteArrayInputStream(theString.getBytes("UTF-8"));
                    while ((len = stringStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                    }
                   // outputStream.close();

                    TimeUnit.SECONDS.sleep(2);
                }
                //serverSocket.close();
                //return theString;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... message) {
            if (message[0] != null && message[0] != "") {
                showMessage(message[0], context);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d(TAG, "Message Received: " + result);
                showMessage(result, context);
            }
        }
    }

    public static class ClientSocketTask extends AsyncTask<Void, String, String> {
        private final String TAG = ServerSocketTask.class.getSimpleName();

        private Context context;
        private TextView statusText;
        private String host;
        private int port;

        String message;
        String echo = "Not Echo";

        public ClientSocketTask(Context context, String host, int port, String message) {
            this.context = context;
            this.host = host;
            this.port = port;
            this.message = message;
        }

        @Override
        protected String doInBackground(Void... params) {
            Context appContext = context.getApplicationContext();
            int len;
            Socket socket = new Socket();
            byte buf[] = new byte[1024];

            try {
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), 500);

                OutputStream outputStream = socket.getOutputStream();

                InputStream stringStream = new ByteArrayInputStream(message.getBytes("UTF-8"));
                while ((len = stringStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                socket.close();


                socket.connect((new InetSocketAddress(host, port)), 500);
                InputStream inputStream = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                echo = in.readLine();
                publishProgress(echo);

                inputStream.close();

            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
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
                            return echo;
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            }
            return echo;
        }

        @Override
        protected void onProgressUpdate(String... message) {
            showMessage(message[0], context);
        }

        @Override
        protected void onPostExecute(String message) {
            if (message != null) {
                showMessage(message, context);
                Log.d(TAG, message);
            }
        }
    }

    public static void showMessage(String message, Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Message Recieved");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
