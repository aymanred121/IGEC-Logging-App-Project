package com.igec.user;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails);
            Toast.makeText(context, geofenceTransitionDetails, Toast.LENGTH_SHORT).show();
        } else {
//            // Log the error.
//            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
//                    geofenceTransition));
            Toast.makeText(context, "ERROR GEOFENCE", Toast.LENGTH_SHORT).show();
        }
    }

    private String getGeofenceTransitionDetails(
            GeofenceBroadcastReceiver context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {
        // String geofenceTransitionString = getTransitionString(geofenceTransition);//getTransitionString(geoFenceTransition)

        ArrayList triggeringgeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringgeofencesIdsList.add(geofence.getRequestId());
        }

        String triggeringgeofencesIdsString = TextUtils.join(",", triggeringgeofencesIdsList);

        return triggeringgeofencesIdsString;
    }
}