package com.example.chiassessment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chiassessment.databinding.ActivityDetailBinding;
import com.example.chiassessment.databinding.ActivityMainBinding;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}