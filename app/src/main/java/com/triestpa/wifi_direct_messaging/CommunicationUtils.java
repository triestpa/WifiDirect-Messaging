package com.triestpa.wifi_direct_messaging;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class CommunicationUtils {

    public static void showMessage(String message, Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("New Message");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    public static int numberBounce(PrintWriter out, BufferedReader in) {
        try {
            String theString = in.readLine();
            if (theString != null) {
                int num = Integer.parseInt(theString);

                ++num;
                out.println("" + num);
                return num;
            }
        }
        catch (IOException e) {
            Log.e("NUMBER_BOUNCE_METHOD", e.getMessage());
        }
        return -1;
    }
}
