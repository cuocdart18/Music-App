package com.example.musicapp.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.databinding.ItemSongOfflineModeBinding;
import com.example.musicapp.models.Song;

import java.util.List;

public class ListSongRecyclerAdapter extends RecyclerView.Adapter<ListSongRecyclerAdapter.SongViewHolder> {
    private List<Song> songs;
    private ICallbackOnClickItem iCallbackOnClickItem;

    public ListSongRecyclerAdapter(ICallbackOnClickItem iCallbackOnClickItem) {
        this.iCallbackOnClickItem = iCallbackOnClickItem;
    }

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

        if (song == null) {
            return;
        }

        //set data
        holder.binding.tvTitle.setText(song.getTitle());
        holder.binding.tvSinger.setText(song.getSinger());
        holder.binding.imvFavourite.setImageResource(R.drawable.ic_favorite_border);
        //set on Click
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iCallbackOnClickItem.onClickItemInRecycler(song);
            }
        });

        holder.binding.imvFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iCallbackOnClickItem.onClickImvFavourite(song);
            }
        });
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
