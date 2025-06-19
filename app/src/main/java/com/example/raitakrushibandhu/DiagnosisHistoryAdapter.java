package com.example.raitakrushibandhu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiagnosisHistoryAdapter extends RecyclerView.Adapter<DiagnosisHistoryAdapter.ViewHolder> {

    private Context context;
    private List<DiagnosisReport> diagnosisList;

    public DiagnosisHistoryAdapter(Context context, List<DiagnosisReport> diagnosisList) {
        this.context = context;
        this.diagnosisList = diagnosisList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diagnosis_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiagnosisReport model = diagnosisList.get(position);

        holder.diseaseName.setText("Disease: " + model.getDiseaseName());
        holder.confidence.setText("Confidence: " + model.getConfidence() + "%");
        holder.remedy.setText("Remedy: " + model.getRemedy());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(model.getTimestamp()));
        holder.timestamp.setText("Diagnosed On: " + formattedDate);

        Glide.with(context)
                .load(model.getImageUrl())
                .placeholder(R.drawable.ic_crop_placeholder)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return diagnosisList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView diseaseName, confidence, remedy, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.reportImage);
            diseaseName = itemView.findViewById(R.id.diseaseName);
            confidence = itemView.findViewById(R.id.confidence);
            remedy = itemView.findViewById(R.id.remedy);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
