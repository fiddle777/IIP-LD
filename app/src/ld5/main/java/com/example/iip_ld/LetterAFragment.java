package com.example.iip_ld;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class LetterAFragment extends Fragment {
    public LetterAFragment() {
        super(R.layout.fragment_result);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String text = requireArguments().getString("text");
        // Count uppercase A and lowercase a together.
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            if (letter == 'A' || letter == 'a') {
                count++;
            }
        }

        TextView title = view.findViewById(R.id.result_title);
        TextView details = view.findViewById(R.id.result_details);
        title.setText(R.string.a_title);
        details.setText(getString(R.string.a_result, count));
    }
}
