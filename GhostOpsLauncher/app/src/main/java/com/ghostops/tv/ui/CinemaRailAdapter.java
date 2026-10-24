package com.ghostops.tv.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ghostops.tv.R;
import com.ghostops.tv.api.MediaItem;

import java.util.List;

public class CinemaRailAdapter extends RecyclerView.Adapter<CinemaRailAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
        void onItemFocused(MediaItem item);
    }

    private final Context context;
    private final List<MediaItem> items;
    private final OnItemClickListener listener;

    public CinemaRailAdapter(Context context, List<MediaItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cinema_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);
        holder.txtTitle.setText(item.getTitle());
        holder.txtBadge.setText(item.getBadge());

        // Load poster via Glide
        Glide.with(context)
                .load(item.getPosterUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            FocusScaleHelper.applyFocusAnimation(v, hasFocus);
            if (hasFocus && listener != null) {
                listener.onItemFocused(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle;
        TextView txtBadge;

        ViewHolder(View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtBadge = itemView.findViewById(R.id.txtBadge);
        }
    }
}
