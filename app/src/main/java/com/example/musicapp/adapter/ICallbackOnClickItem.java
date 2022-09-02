package com.example.musicapp.adapter;

import com.example.musicapp.models.Song;

public interface ICallbackOnClickItem {
    void onClickItemInRecycler(Song song);

    void onClickImvFavourite(Song song);
}
