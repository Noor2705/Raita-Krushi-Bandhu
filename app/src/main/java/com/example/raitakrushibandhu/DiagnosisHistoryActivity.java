package com.example.raitakrushibandhu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DiagnosisHistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecycler;
    private DiagnosisHistoryAdapter adapter;
    private List<DiagnosisReport> reportList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis_history);

        historyRecycler = findViewById(R.id.historyRecycler);
        reportList = new ArrayList<>();
        adapter = new DiagnosisHistoryAdapter(this, reportList);

        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setAdapter(adapter);

        fetchDiagnosisReports();
    }

    private void fetchDiagnosisReports() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String loggedInUserPhone = prefs.getString("loggedInUser", null);

        if (loggedInUserPhone == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("diagnosisReports")
                .whereEqualTo("userId", loggedInUserPhone)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportList.clear();
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        DiagnosisReport report = snapshot.toObject(DiagnosisReport.class);
                        reportList.add(report);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load reports.", Toast.LENGTH_SHORT).show());
    }
}
