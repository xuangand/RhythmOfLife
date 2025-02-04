package com.xuangand.rhythmoflife.listener;

import com.xuangand.rhythmoflife.model.Artist;

public interface IOnAdminManagerArtistListener {
    void onClickUpdateArtist(Artist artist);
    void onClickDeleteArtist(Artist artist);
    void onClickDetailArtist(Artist artist);
}
