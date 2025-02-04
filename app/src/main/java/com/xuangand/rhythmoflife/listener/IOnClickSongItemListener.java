package com.xuangand.rhythmoflife.listener;

import com.xuangand.rhythmoflife.model.Song;

public interface IOnClickSongItemListener {
    void onClickItemSong(Song song);
    void onClickFavoriteSong(Song song, boolean favorite);
    void onClickMoreOptions(Song song);
}
