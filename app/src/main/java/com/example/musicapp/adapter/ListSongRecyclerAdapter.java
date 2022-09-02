package com.example.musicapp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.databinding.ItemSongOfflineModeBinding;
import com.example.musicapp.models.Song;

import java.util.List;

public class ListSongRecyclerAdapter extends RecyclerView.Adapter<ListSongRecyclerAdapter.SongViewHolder> {
    private List<Song> songs;

    public void setDataChange(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongViewHolder(ItemSongOfflineModeBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    @SuppressLint({"ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        if (song != null) {
            holder.binding.tvTitle.setText(song.getTitle());
            holder.binding.tvSinger.setText(song.getSinger());
            holder.binding.imvFavourite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        private ItemSongOfflineModeBinding binding;

        public SongViewHolder(@NonNull ItemSongOfflineModeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
