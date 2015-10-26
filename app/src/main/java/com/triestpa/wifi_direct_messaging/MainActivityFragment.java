package com.triestpa.wifi_direct_messaging;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivityFragment extends Fragment {

    MainActivity mActivity;
    TextView peerInfo;
    TextView connectionInfo;
    EditText messageText;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        Button testButton = (Button) fragmentView.findViewById(R.id.detect_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverPeers();
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

        Button sendButton = (Button) fragmentView.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.startClient("192.168.49.1", 8888, messageText.getText().toString());
            }
        });


        Button serverButton = (Button) fragmentView.findViewById(R.id.start_server);
        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.startServer();
            }
        });

        messageText = (EditText) fragmentView.findViewById(R.id.message_input);
        peerInfo = (TextView) fragmentView.findViewById(R.id.peer_info);
        connectionInfo = (TextView) fragmentView.findViewById(R.id.connection_info);

        return fragmentView;
    }

    public void updateThisDevice(WifiP2pDevice device) {
        //TODO update the device in the UI
    }

    private void discoverPeers() {
        mActivity.discoverPeers();
    }

    private void updateInfo() {
        peerInfo.setText(mActivity.mPeers.toString());
    }

    private void updateConnectionInfo() {
        if (mActivity.mConnectionInfo != null) {
            connectionInfo.setText(mActivity.mConnectionInfo.toString());
        }
    }


}
