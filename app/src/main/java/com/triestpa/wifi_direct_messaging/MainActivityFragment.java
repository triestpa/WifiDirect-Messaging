package com.triestpa.wifi_direct_messaging;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivityFragment extends Fragment {

    MainActivity mActivity;
    TextView peerInfo;
    TextView connectionInfo;
    TextView currentNumber;
    TextView socketInfo;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        mActivity.mFragment = this;

        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        Button testButton = (Button) fragmentView.findViewById(R.id.detect_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.discoverPeers();
                updateInfo();
            }
        });

        Button connectButton = (Button) fragmentView.findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.connectToDevice();
                updateConnectionInfo();
            }
        });

        Button startButton = (Button) fragmentView.findViewById(R.id.start_comms);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.keepGoing = true;
                mActivity.keepRetrying = true;
                mActivity.startCommunications();
            }
        });

        Button stopButton = (Button) fragmentView.findViewById(R.id.stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.keepGoing = false;
                mActivity.keepRetrying = false;
            }
        });

        peerInfo = (TextView) fragmentView.findViewById(R.id.peer_info);
        connectionInfo = (TextView) fragmentView.findViewById(R.id.connection_info);
        currentNumber = (TextView) fragmentView.findViewById(R.id.current_number);
        socketInfo = (TextView) fragmentView.findViewById(R.id.socket_info);

        return fragmentView;
    }

    private void updateInfo() {
        if (mActivity.mPeers != null && !mActivity.mPeers.isEmpty()) {
            peerInfo.setText(mActivity.mPeers.size() + " Peer(s) Found");
        }
        else {
            peerInfo.setText("No Peers Found");
        }
    }

    private void updateConnectionInfo() {
        if (mActivity.mConnectionInfo != null) {
            if (MainActivity.isServer) {
                connectionInfo.setText("Connection Established, you are host");
            } else {
                connectionInfo.setText("Connection Established, you are client");
            }
        }
        else {
            connectionInfo.setText("Not Connected");
        }
    }


}
