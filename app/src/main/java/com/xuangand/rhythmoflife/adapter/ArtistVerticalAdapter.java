package com.xuangand.rhythmoflife.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xuangand.rhythmoflife.databinding.ItemArtistVerticalBinding;
import com.xuangand.rhythmoflife.listener.IOnClickArtistItemListener;
import com.xuangand.rhythmoflife.model.Artist;
import com.xuangand.rhythmoflife.utils.GlideUtils;

import java.util.List;

public class ArtistVerticalAdapter extends RecyclerView.Adapter<ArtistVerticalAdapter.ArtistVerticalViewHolder> {

    private final List<Artist> mListArtist;
    public final IOnClickArtistItemListener iOnClickArtistItemListener;

    public ArtistVerticalAdapter(List<Artist> list, IOnClickArtistItemListener listener) {
        this.mListArtist = list;
        this.iOnClickArtistItemListener = listener;
    }

    @NonNull
    @Override
    public ArtistVerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArtistVerticalBinding itemArtistVerticalBinding = ItemArtistVerticalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ArtistVerticalViewHolder(itemArtistVerticalBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistVerticalViewHolder holder, int position) {
        Artist artist = mListArtist.get(position);
        if (artist == null) return;
        GlideUtils.loadUrl(artist.getImage(), holder.mItemArtistVerticalBinding.imgArtist);
        holder.mItemArtistVerticalBinding.tvArtist.setText(artist.getName());

        holder.mItemArtistVerticalBinding.layoutItem.setOnClickListener(v -> iOnClickArtistItemListener.onClickItemArtist(artist));
    }

    @Override
    public int getItemCount() {
        return null == mListArtist ? 0 : mListArtist.size();
    }

    public static class ArtistVerticalViewHolder extends RecyclerView.ViewHolder {

        private final ItemArtistVerticalBinding mItemArtistVerticalBinding;

        public ArtistVerticalViewHolder(ItemArtistVerticalBinding itemArtistVerticalBinding) {
            super(itemArtistVerticalBinding.getRoot());
            this.mItemArtistVerticalBinding = itemArtistVerticalBinding;
        }
    }
}
