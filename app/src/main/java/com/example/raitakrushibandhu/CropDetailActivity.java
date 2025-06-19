package com.example.raitakrushibandhu;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CropDetailActivity extends AppCompatActivity {

    private ImageView cropImage;
    private TextView cropNameText, soilTypeText, npkText, commonDiseasesText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_detail);

        cropImage = findViewById(R.id.cropImage);
        cropNameText = findViewById(R.id.cropNameText);
        soilTypeText = findViewById(R.id.soilTypeText);
        npkText = findViewById(R.id.npkText);
        commonDiseasesText = findViewById(R.id.commonDiseasesText);

        String cropName = getIntent().getStringExtra("cropName");
        int imageResId = getIntent().getIntExtra("imageResId", R.drawable.ic_crop_placeholder);

        cropImage.setImageResource(imageResId);
        cropNameText.setText(cropName);

        switch (cropName.toLowerCase()) {
            case "rice":
                soilTypeText.setText("Soil Type: Clayey Loam");
                npkText.setText("NPK Requirement: N-100kg, P-50kg, K-50kg per acre");
                commonDiseasesText.setText("Common Diseases: Blast, Bacterial Leaf Blight, Sheath Blight");
                break;
            case "wheat":
                soilTypeText.setText("Soil Type: Loamy");
                npkText.setText("NPK Requirement: N-120kg, P-60kg, K-40kg per acre");
                commonDiseasesText.setText("Common Diseases: Rust, Smut, Powdery Mildew");
                break;
            case "maize":
                soilTypeText.setText("Soil Type: Well-drained Loam");
                npkText.setText("NPK Requirement: N-120kg, P-60kg, K-40kg per acre");
                commonDiseasesText.setText("Common Diseases: Downy Mildew, Leaf Blight, Rust");
                break;
            case "cotton":
                soilTypeText.setText("Soil Type: Black Soil");
                npkText.setText("NPK Requirement: N-90kg, P-45kg, K-45kg per acre");
                commonDiseasesText.setText("Common Diseases: Wilt, Leaf Curl, Boll Rot");
                break;
            case "tomato":
                soilTypeText.setText("Soil Type: Sandy Loam");
                npkText.setText("NPK Requirement: N-100kg, P-50kg, K-50kg per acre");
                commonDiseasesText.setText("Common Diseases: Early Blight, Late Blight, Leaf Spot");
                break;
            case "chilli":
                soilTypeText.setText("Soil Type: Well-drained Sandy Loam");
                npkText.setText("NPK Requirement: N-75kg, P-40kg, K-40kg per acre");
                commonDiseasesText.setText("Common Diseases: Anthracnose, Powdery Mildew, Leaf Curl");
                break;
            default:
                soilTypeText.setText("Soil Type: Data not available");
                npkText.setText("NPK Requirement: Data not available");
                commonDiseasesText.setText("Common Diseases: Data not available");
                break;
        }
    }
}
