package com.diabetes.app2018.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jackie on 2018-04-12.
 */

public class AwardsAdapter extends RecyclerView.Adapter<AwardsAdapter.ViewHolder>{

    private Context mContext;
    private List<Award> awards;

    public AwardsAdapter(Context context, List<Award> awards) {
        this.mContext = context;
        this.awards = awards;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.award_item, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        Award award = awards.get(position);
        holder.name.setText(award.getName());
        int id = mContext.getResources().getIdentifier(award.getImage(), "drawable", mContext.getPackageName());
        holder.image.setImageResource(id);
        holder.description.setText(award.getDescription());
    }

    @Override public int getItemCount() {
        return awards.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.award_name);
            image = (ImageView) itemView.findViewById(R.id.award_image);
            description = (TextView) itemView.findViewById(R.id.award_description);
        }
    }
}
