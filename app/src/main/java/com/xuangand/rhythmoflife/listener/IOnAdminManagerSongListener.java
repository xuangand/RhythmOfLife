package com.xuangand.rhythmoflife.listener;

import com.xuangand.rhythmoflife.model.Song;

public interface IOnAdminManagerSongListener {
    void onClickUpdateSong(Song song);
    void onClickDeleteSong(Song song);
    void onClickDetailSong(Song song);
}
