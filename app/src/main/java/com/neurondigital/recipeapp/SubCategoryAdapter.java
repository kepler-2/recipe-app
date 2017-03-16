package com.neurondigital.recipeapp;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Category adapter to create a list with Text and Images
 */

public class SubCategoryAdapter extends RecyclerView.Adapter<ViewHolder> {
    List<SubCategory> subCategories;
    Context context;
    private AdapterView.OnItemClickListener onItemClickListener;

    SubCategoryAdapter(List<SubCategory> subCategories, AdapterView.OnItemClickListener onItemClickListener, Context context) {
        this.subCategories = subCategories;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }


    /**
     * Holds the Category elements so that they don't have to be re-created each time
     */
    public class CategoryViewHolder extends ViewHolder implements View.OnClickListener {
        LinearLayout cv;
        TextView name;
        AspectRatioImageView image;

        CategoryViewHolder(View itemView) {
            super(itemView);
            cv = (LinearLayout) itemView.findViewById(R.id.linear_view);
            image = (AspectRatioImageView) itemView.findViewById(R.id._image);
            image.setBox(true);
            name = (TextView) itemView.findViewById(R.id._name);
            cv.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //passing the clicked position to the parent class
            onItemClickListener.onItemClick(null, view, getAdapterPosition(), view.getId());
        }
    }


    @Override
    public int getItemCount() {
        return subCategories.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_item, viewGroup, false);
        ViewHolder cvh = new CategoryViewHolder(v);
        return cvh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder recipeViewHolder, final int i) {
        //set category name
        ((CategoryViewHolder) recipeViewHolder).name.setText(subCategories.get(i).name);

        //load images using Picasso
        Picasso.with(context)
                .load(subCategories.get(i).imageUrl).placeholder(R.drawable.loading)
                .fit().centerCrop()
                .into(((CategoryViewHolder) recipeViewHolder).image);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}