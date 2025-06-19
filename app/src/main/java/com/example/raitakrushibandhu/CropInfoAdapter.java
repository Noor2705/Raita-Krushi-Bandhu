package com.example.raitakrushibandhu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class CropInfoAdapter extends BaseAdapter {

    private Context context;
    private List<CropModel> cropList;

    public CropInfoAdapter(Context context, List<CropModel> cropList) {
        this.context = context;
        this.cropList = cropList;
    }

    @Override
    public int getCount() {
        return cropList.size();
    }

    @Override
    public Object getItem(int position) {
        return cropList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_crop, parent, false);
        } else {
            view = convertView;
        }

        CropModel crop = cropList.get(position);

        ImageView cropImage = view.findViewById(R.id.cropImage);
        TextView cropName = view.findViewById(R.id.cropName);

        cropImage.setImageResource(crop.getImageResId());
        cropName.setText(crop.getCropName());

        return view;
    }
}
