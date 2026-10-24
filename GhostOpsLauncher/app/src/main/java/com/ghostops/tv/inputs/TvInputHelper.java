package com.ghostops.tv.inputs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TvInputHelper {

    public static class InputOption {
        public String label;
        public String inputId;
        public boolean isPassthrough;

        public InputOption(String label, String inputId, boolean isPassthrough) {
            this.label = label;
            this.inputId = inputId;
            this.isPassthrough = isPassthrough;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static List<InputOption> getAvailableInputs(Context context) {
        List<InputOption> options = new ArrayList<>();
        TvInputManager manager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
        if (manager != null) {
            try {
                List<TvInputInfo> inputs = manager.getTvInputList();
                for (TvInputInfo info : inputs) {
                    String id = info.getId();
                    CharSequence customLabel = info.loadLabel(context);
                    String label = customLabel != null ? customLabel.toString() : id;

                    // Clean up common HDMI input labels
                    if (id.toLowerCase().contains("hdmi1")) label = "HDMI 1";
                    else if (id.toLowerCase().contains("hdmi2")) label = "HDMI 2";
                    else if (id.toLowerCase().contains("hdmi3")) label = "HDMI 3";
                    else if (id.toLowerCase().contains("hdmi4")) label = "HDMI 4";
                    else if (id.toLowerCase().contains("composite") || id.toLowerCase().contains("av")) label = "AV Input";
                    else if (info.getType() == TvInputInfo.TYPE_TUNER) label = "Live TV / Antenna";

                    options.add(new InputOption(label, id, info.isPassthroughInput()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If no hardware TvInputManager sources detected (e.g. emulator or stripped firmware), add standard defaults
        if (options.isEmpty()) {
            options.add(new InputOption("HDMI 1", "com.mediatek.tvinput/.hdmi.HDMI1", true));
            options.add(new InputOption("HDMI 2", "com.mediatek.tvinput/.hdmi.HDMI2", true));
            options.add(new InputOption("HDMI 3", "com.mediatek.tvinput/.hdmi.HDMI3", true));
            options.add(new InputOption("Live TV / Cable", "tuner", false));
        }

        return options;
    }

    public static void showInputPicker(Context context) {
        List<InputOption> inputs = getAvailableInputs(context);
        String[] titles = new String[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            titles[i] = inputs.get(i).label;
        }

        new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Select TV Input")
                .setItems(titles, (dialog, which) -> {
                    InputOption selected = inputs.get(which);
                    switchToInput(context, selected);
                })
                .show();
    }

    public static void switchToInput(Context context, InputOption input) {
        try {
            Uri uri;
            if (input.isPassthrough) {
                uri = TvContract.buildChannelUriForPassthroughInput(input.inputId);
            } else {
                uri = TvContract.Channels.CONTENT_URI;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Switching to " + input.label + "...", Toast.LENGTH_SHORT).show();
            // Fallback: try generic TV inputs action
            try {
                Intent intent = new Intent("com.android.tv.action.VIEW_INPUTS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
