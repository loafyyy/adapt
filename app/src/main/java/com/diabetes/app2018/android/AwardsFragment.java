package com.diabetes.app2018.android;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class AwardsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AwardsAdapter adapter;
    private Context mContext;
    private List<Award> awards;

    public AwardsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_awards, container, false);
        recyclerView = view.findViewById(R.id.awards_recyclerview);
        awards = new ArrayList<>();
        awards.add(new Award("award1", "face", "description1"));
        awards.add(new Award("award2", "face", "description2"));
        awards.add(new Award("award3", "face", "description3"));

        adapter = new AwardsAdapter(mContext, awards);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        return view;
    }
}
