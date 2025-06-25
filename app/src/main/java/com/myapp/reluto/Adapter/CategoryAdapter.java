package com.myapp.reluto.Adapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myapp.reluto.Activity.ListFoodsActivity;
import com.myapp.reluto.Domain.Category;
import com.myapp.reluto.R;

import java.util.ArrayList;

public class    CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.viewholder> {
    ArrayList<Category> items;
    Context context;

    public CategoryAdapter(ArrayList<Category> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CategoryAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholdercategory,parent,false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.viewholder holder, int position) {

holder.titleTxt.setText(items.get(position).getName());


        switch(position){
            case 0:{
                holder.pic.setBackgroundResource(R.drawable.blackbutton_background1);
                break;
            }
            case 1:{
                holder.pic.setBackgroundResource(R.drawable.blackbutton_background1);
                break;
            }
            case 2:{
                holder.pic.setBackgroundResource(R.drawable.blackbutton_background1);
                break;
            }
            case 3:{
                holder.pic.setBackgroundResource(R.drawable.blackbutton_background1);
                break;
            }
            case 4:{
                holder.pic.setBackgroundResource(R.drawable.blackbutton_background1);
                break;
            }
            case 5:{
                holder.pic.setBackgroundResource(R.drawable.blackbutton_background1);
                break;
            }
            case 6:{
                holder.pic.setBackgroundResource(R.drawable.blackbutton_background1);
                break;
            }
            case 7:{
                holder.pic.setBackgroundResource(R.drawable.blackbutton_background1);
                break;
            }




        }
        int drawableResourceId = context.getResources().getIdentifier(items.get(position)
                .getImagePath(),"drawable",holder.itemView.getContext().getPackageName());
        Glide.with(context)
                .load(drawableResourceId)
                .into(holder.pic);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ListFoodsActivity.class);
            intent.putExtra("CategoryId",items.get(position).getId());
            intent.putExtra("CategoryName",items.get(position).getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder{
        TextView titleTxt;
        ImageView pic;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.catNameTxt);

            pic = itemView.findViewById(R.id.imgCat);
        }
    }
}
