/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ccs.maincrabproject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Provides UI for the view with Cards.
 */
public class CardContentFragment extends Fragment {

    protected ContentAdapter adapter;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private StorageReference imagesRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        adapter = new ContentAdapter(recyclerView.getContext());
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("crabmodels");

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView picture;
        public TextView name;
        public TextView description;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_card, parent, false));
            picture = (ImageView) itemView.findViewById(R.id.card_image);
            name = (TextView) itemView.findViewById(R.id.card_title);
            description = (TextView) itemView.findViewById(R.id.card_text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_POSITION, getAdapterPosition());
                    intent.putExtra("CrabObject", (Serializable) adapter.getcrab(getAdapterPosition()));
                    context.startActivity(intent);
                }
            });

            ImageButton shareImageButton = (ImageButton) itemView.findViewById(R.id.share_button);
            shareImageButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Share article", Snackbar.LENGTH_LONG).show();
                    String temp = adapter.getcrab(getAdapterPosition()).getFileurl();
                    temp = temp.substring(temp.lastIndexOf("/")+1);
                    temp = temp.replace("images%2F", "");
                    temp = temp.substring(0, temp.lastIndexOf("?"));
                    String dbfilename = "images/" + temp;
                    Toast.makeText(v.getContext(), dbfilename, Toast.LENGTH_LONG).show();
                    imagesRef = mStorageRef.child(dbfilename);
                    imagesRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            myRef.child(adapter.getcrab(getAdapterPosition()).getId()).removeValue();
                            Toast.makeText(getContext(), "Success", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            });
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of Card in RecyclerView.

        private List<CrabModel> crabmodels;

        public ContentAdapter(Context context) {

            crabmodels = new ArrayList<>();
        }

        public void addCrabs(CrabModel crab){
            crabmodels.add(crab);
        }

        public CrabModel getcrab(int i){
            return crabmodels.get(i);
        }

        public void clearCrabs(){
            crabmodels.clear();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Picasso.get()
                    .load(crabmodels.get(position).getFileurl())
                    .placeholder(R.drawable.ic_crabby)
                    .fit()
                    .centerCrop()
                    .into(holder.picture);
            holder.name.setText(crabmodels.get(position).getClassifier());
            holder.description.setText(String.valueOf(crabmodels.get(position).getProbval()));
        }

        @Override
        public int getItemCount() {
            return crabmodels.size();
        }
    }
}
