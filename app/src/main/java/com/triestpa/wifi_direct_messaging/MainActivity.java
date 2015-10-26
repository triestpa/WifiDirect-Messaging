package com.triestpa.wifi_direct_messaging;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements CommunicationUtils.OnSocketTaskCompleted {
    private final String TAG = MainActivity.class.getSimpleName();

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    WifiP2pInfo mConnectionInfo;
    MainActivity mActivity;
    WiFiDirectBroadcastReceiver mReceiver;

    MainActivityFragment mFragment;

    static final String ServerAddr = "192.168.49.1";
    static final int ServerPort = 8888;

    static boolean keepGoing = false;
    static boolean keepRetrying = false;

    static int numAttempts = 0;

    List mPeers = new ArrayList();

    static boolean isServer = false;
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

    @Override
    public void onSocketTaskCompleted() {
        Log.d(TAG, "Retrying: Attempt " + numAttempts);
        if (keepRetrying && isServer) {
            new ServerScanTask(this, mFragment).execute();
            startCommunications();
        }
        else if (keepRetrying && !isServer) {
            ++numAttempts;
            discoverPeers();
            connectToDevice();
            startCommunications();
        }
        else {
            mFragment.socketInfo.setText("Connection Aborted");
        }
    }

    public void setIsWifiP2pEnabled(boolean isEnabled) {
        wifiP2pEnabled = isEnabled;
    }

    public void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Peer Discovery Success");
                if (mPeers.size() == 0) {
                    Log.d(TAG, "No devices found");
                    mFragment.peerInfo.setText("No devices found");
                } else {
                    Log.d(TAG, mPeers.toString());
                    mFragment.peerInfo.setText(mPeers.size() + " Peer(s) Found");
                }
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e(TAG, "Peer Discovery Failed");
                mFragment.peerInfo.setText("Peer Discovery Failed");
            }
        });
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

    public void startCommunications() {
        mFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        mFragment.socketInfo.setText("Attempting to Connect");
        if (isServer) {
            new ServerSocketTask(this, mFragment, this).execute();

        } else {
            new ClientSocketTask(this, mFragment, this).execute();
        }
    }

    public void updatePeerInfo() {
        if (mPeers.size() == 0) {
            Log.d(TAG, "No devices found");
            mFragment.peerInfo.setText("No devices found");
        } else {
            Log.d(TAG, mPeers.toString());
            mFragment.peerInfo.setText(mPeers.size() + " Peer(s) Found");
        }
    }

    protected WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            // Out with the old, in with the new.
            mPeers.clear();
            mPeers.addAll(peerList.getDeviceList());
            updatePeerInfo();
        }
    };
}
