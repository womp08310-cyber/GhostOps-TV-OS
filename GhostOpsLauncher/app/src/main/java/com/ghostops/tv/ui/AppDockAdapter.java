package com.ghostops.tv.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ghostops.tv.R;

import java.util.List;

public class AppDockAdapter extends RecyclerView.Adapter<AppDockAdapter.ViewHolder> {

    public static class AppItem {
        private final String label;
        private final String packageName;
        private final Drawable icon;
        private final Intent launchIntent;

        public AppItem(String label, String packageName, Drawable icon, Intent launchIntent) {
            this.label = label;
            this.packageName = packageName;
            this.icon = icon;
            this.launchIntent = launchIntent;
        }

        public String getLabel() { return label; }
        public String getPackageName() { return packageName; }
        public Drawable getIcon() { return icon; }
        public Intent getLaunchIntent() { return launchIntent; }
    }

    private final Context context;
    private final List<AppItem> apps;

    public AppDockAdapter(Context context, List<AppItem> apps) {
        this.context = context;
        this.apps = apps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppItem app = apps.get(position);
        holder.txtAppName.setText(app.getLabel());
        if (app.getIcon() != null) {
            holder.imgAppIcon.setImageDrawable(app.getIcon());
        }

        holder.itemView.setOnClickListener(v -> {
            if (app.getLaunchIntent() != null) {
                try {
                    context.startActivity(app.getLaunchIntent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            FocusScaleHelper.applyFocusAnimation(v, hasFocus);
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAppIcon;
        TextView txtAppName;

        ViewHolder(View itemView) {
            super(itemView);
            imgAppIcon = itemView.findViewById(R.id.imgAppIcon);
            txtAppName = itemView.findViewById(R.id.txtAppName);
        }
    }
}
