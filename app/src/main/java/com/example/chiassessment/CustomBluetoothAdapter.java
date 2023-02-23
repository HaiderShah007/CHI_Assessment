package com.example.chiassessment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chiassessment.databinding.BluetoothListDesignBinding;

import java.util.List;

public class CustomBluetoothAdapter extends RecyclerView.Adapter<CustomBluetoothAdapter.ViewHolder>{

    List<ScanResult> dataList;
    SelectedDevice callback;

    public CustomBluetoothAdapter(List<ScanResult> dataList ,SelectedDevice callback) {
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
        holder.onBind(dataList.get(position));
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

        @SuppressLint({"MissingPermission", "SetTextI18n"})
        public void onBind(ScanResult data) {
            if (data.getDevice().getName() == null){
                binding.textBluetoothName.setText("Unknown Device");
            }else {
                binding.textBluetoothName.setText(String.valueOf(data.getDevice().getName()));
            }

            binding.textBluetoothMac.setText(String.valueOf(data.getDevice().getAddress()));
            switch (data.getDevice().getBondState()) {
                case BluetoothDevice.BOND_NONE:
                    binding.textBluetoothBoundStatus.setText("Not Bounded");
                    break;
                case BluetoothDevice.BOND_BONDING:
                    binding.textBluetoothBoundStatus.setText("Bounded");
                    break;
                case BluetoothDevice.BOND_BONDED:
                    binding.textBluetoothBoundStatus.setText("BONDED");
                    break;
            }

            ParcelUuid[] uuids = data.getDevice().getUuids();
            if (uuids != null && uuids.length > 0) {
                String uuidString = uuids[0].getUuid().toString();
                long timestamp = Long.parseLong(uuidString.substring(0, 8), 16) * 1000L;
                // timestamp is now in milliseconds
                binding.textBluetoothMS.setVisibility(View.VISIBLE);
                binding.textBluetoothMS.setText(timestamp +" ms");
            }
            if(data.getRssi() != 0){
                binding.textBluetoothDBM.setVisibility(View.VISIBLE);
                binding.textBluetoothDBM.setText(String.valueOf(calculateDistance(data.getRssi())) +" dBm");
            }
            //binding.textBluetoothMS.setText(data.getBluetoothMs() +" ms");
            //binding.textBluetoothDBM.setText(data.getBluetoothDBM() +" dBm");
            binding.button.setOnClickListener(v -> {
                callback.getSelectedDevice(data.getDevice());
            });
        }
    }
    interface SelectedDevice{
        void getSelectedDevice(BluetoothDevice device);
    }
    private double calculateDistance(int rssi) {
        double txPower = -59; // The transmit power of the Bluetooth device (in dBm)
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return distance;
        }
    }
}
