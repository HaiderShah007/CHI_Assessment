package com.example.chiassessment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chiassessment.databinding.BluetoothListDesignBinding;

import java.util.ArrayList;

public class BluetoothAdapter extends RecyclerView.Adapter<BluetoothAdapter.ViewHolder>{

    ArrayList<BluetoothModel> dataList;
    SelectedDevice callback;

    public BluetoothAdapter(ArrayList<BluetoothModel> dataList, SelectedDevice callback) {
        this.dataList = dataList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(BluetoothListDesignBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(dataList.get(position),position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        BluetoothListDesignBinding binding;
        ViewHolder(BluetoothListDesignBinding view) {
            super(view.getRoot());
            binding = view;
        }

        public void onBind(BluetoothModel data, int position) {
            binding.textBluetoothName.setText(data.getDeviceName());
            binding.textBluetoothMac.setText(data.getDeviceMac());
            binding.textBluetoothBoundStatus.setText(data.getDeviceBondStatus());
            binding.textBluetoothMS.setText(data.getBluetoothMs() +" ms");
            binding.textBluetoothDBM.setText(data.getBluetoothDBM() +" dBm");
            binding.container.setOnClickListener(v -> {
                callback.getSelectedDevice(data);
            });
        }
    }
    interface SelectedDevice{
        void getSelectedDevice(BluetoothModel device);
    }
}
