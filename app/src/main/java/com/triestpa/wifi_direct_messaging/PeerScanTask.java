package com.triestpa.wifi_direct_messaging;

import android.os.AsyncTask;

public class PeerScanTask extends AsyncTask<Void, Integer, String> {
    private final String TAG = ServerSocketTask.class.getSimpleName();

    private MainActivity context;
    private MainActivityFragment fragment;

    public PeerScanTask(MainActivity context, MainActivityFragment fragment) {
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    protected String doInBackground(Void... params) {
        while (MainActivity.keepRetrying) {
            if (context.mPeers == null || context.mPeers.isEmpty()) {
                context.discoverPeers();
            }

            if (context.mConnectionInfo == null) {
                context.connectToDevice();
            }

            publishProgress(0);
            android.os.SystemClock.sleep(5000);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... num) {
        context.mFragment.updateConnectionInfo();
        context.updatePeerInfo();
    }

    @Override
    protected void onPostExecute(String result) {
    }
}