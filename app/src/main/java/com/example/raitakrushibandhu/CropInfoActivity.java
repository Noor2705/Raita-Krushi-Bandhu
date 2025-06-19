package com.example.raitakrushibandhu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class CropInfoActivity extends AppCompatActivity {

    private GridView cropGrid;
    private CropInfoAdapter adapter;
    private List<CropModel> cropList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_details);

        cropGrid = findViewById(R.id.cropGrid);
        cropList = new ArrayList<>();

        cropList.add(new CropModel("Rice", R.drawable.rice));
        cropList.add(new CropModel("Wheat", R.drawable.wheat));
        cropList.add(new CropModel("Maize", R.drawable.maize));
        cropList.add(new CropModel("Sugarcane", R.drawable.sugarcane));
        cropList.add(new CropModel("Cotton", R.drawable.cotton));
        // Add more crops as needed

        adapter = new CropInfoAdapter(this, cropList);
        cropGrid.setAdapter(adapter);

        cropGrid.setOnItemClickListener((parent, view, position, id) -> {
            CropModel selectedCrop = cropList.get(position);
            Intent intent = new Intent(CropInfoActivity.this, CropDetailActivity.class);
            intent.putExtra("cropName", selectedCrop.getCropName());
            intent.putExtra("imageResId", selectedCrop.getImageResId());
            startActivity(intent);
        });

    }
}
