package com.example.bembersmusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder>{

    ArrayList<AudioModel> songsList;
    Context context;

    public MusicListAdapter(ArrayList<AudioModel> songsList, Context context) {
        this.songsList = songsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item,parent,false);
        return new MusicListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( MusicListAdapter.ViewHolder holder, int position) {
        AudioModel songData = songsList.get(holder.getAdapterPosition());
        Log.d("ViewHolder", String.format(Locale.ENGLISH, "position: %d, title: %s, hasImage: %b",
                position, songData.getTitle(), songData.getImage() != null));
        holder.titleTextView.setText(songData.getTitle());
        holder.authorTextView.setText(songData.getAuthor());
        if(songData.getImage() == null)
            holder.iconImageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.music_icon));
            else
            holder.iconImageView.setImageBitmap(songData.getImage());

        if(MyMediaPlayer.getCurrentIndex() == position){
            holder.titleTextView.setTextColor(Color.parseColor("#FF0000"));
        }else{
            holder.titleTextView.setTextColor(Color.parseColor("#FFFFFF"));
        }

        holder.itemView.setOnClickListener(v -> {
            //navigate to another acitivty
            Log.d("ViewHolder", String.format(Locale.ENGLISH, "ON CLICK : position: %d, title: %s, hasImage: %b",
                    position, songData.getTitle(), songData.getImage() != null));
            MyMediaPlayer myMediaPlayer = MyMediaPlayer.getInstance();
            myMediaPlayer.reset();
            myMediaPlayer.disableAutoplay();
            myMediaPlayer.setCurrentAudioFromIndex(holder.getAdapterPosition());
            if(myMediaPlayer.getAudiosList() == null)
                myMediaPlayer.setAudiosList(songsList);
            Intent intent = new Intent(context, MusicPlayerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView, authorTextView;
        ImageView iconImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
            authorTextView = itemView.findViewById(R.id.author_text);
        }
    }
}
