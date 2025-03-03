package com.xuangand.rhythmoflife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xuangand.rhythmoflife.R;
import com.xuangand.rhythmoflife.constant.GlobalFunction;
import com.xuangand.rhythmoflife.databinding.ItemContactBinding;
import com.xuangand.rhythmoflife.model.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context context;
    private final List<Contact> listContact;



    public ContactAdapter(Context context, List<Contact> listContact) {
        this.context = context;
        this.listContact = listContact;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactBinding itemContactBinding = ItemContactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(itemContactBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = listContact.get(position);
        if (contact == null) {
            return;
        }
        holder.mItemContactBinding.imgContact.setImageResource(contact.getImage());
        switch (contact.getId()) {
            case Contact.FACEBOOK:
                holder.mItemContactBinding.tvContact.setText(context.getString(R.string.label_facebook));
                break;

            case Contact.INSTAGRAM:
                holder.mItemContactBinding.tvContact.setText(context.getString(R.string.label_instagram));
                break;
        }

        holder.mItemContactBinding.layoutItem.setOnClickListener(v -> {
            switch (contact.getId()) {
                case Contact.FACEBOOK:
                    GlobalFunction.onClickOpenFacebook(context);
                    break;

                case Contact.INSTAGRAM:
                    GlobalFunction.onClickOpenInstagram(context);
                    break;
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == listContact ? 0 : listContact.size();
    }

    public void release() {
        context = null;
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        private final ItemContactBinding mItemContactBinding;

        public ContactViewHolder(ItemContactBinding itemContactBinding) {
            super(itemContactBinding.getRoot());
            this.mItemContactBinding = itemContactBinding;
        }
    }
}