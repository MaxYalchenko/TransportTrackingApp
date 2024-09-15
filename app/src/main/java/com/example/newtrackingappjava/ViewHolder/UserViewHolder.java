package com.example.newtrackingappjava.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrackingappjava.Interface.IRecyclerItemClickListener;
import com.example.newtrackingappjava.Interface.IRecyclerItemLongClickListener;
import com.example.newtrackingappjava.Model.User;
import com.example.newtrackingappjava.R;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    public TextView txt_user_email;
    public TextView txt_user_transport;
    public ImageView img_online_status; // ImageView для статуса
    private IRecyclerItemClickListener iRecyclerItemClickListener;
    private IRecyclerItemLongClickListener iRecyclerItemLongClickListener;
    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        txt_user_email = (TextView) itemView.findViewById(R.id.txt_user_email);
        txt_user_transport = (TextView) itemView.findViewById(R.id.txt_user_transport);
        img_online_status = (ImageView) itemView.findViewById(R.id.img_online_status);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void setiRecyclerItemClickListener(IRecyclerItemClickListener iRecyclerItemClickListener) {
        this.iRecyclerItemClickListener = iRecyclerItemClickListener;
    }

    public void setiRecyclerItemLongClickListener(IRecyclerItemLongClickListener iRecyclerItemLongClickListener){
        this.iRecyclerItemLongClickListener = iRecyclerItemLongClickListener;
    }


    @Override
    public void onClick(View view) {
        iRecyclerItemClickListener.onItemClickListener(view, getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View view){
       if (iRecyclerItemLongClickListener != null){
           iRecyclerItemLongClickListener.onItemLongClickListener(view, getAdapterPosition());
           return true;
       }
       return false;
    }
}
