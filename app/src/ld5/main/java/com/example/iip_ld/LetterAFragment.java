package com.example.iip_ld;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class LetterAFragment extends Fragment {
    // Use the shared result layout: res/layout/fragment_result.xml (a title + details text).
    public LetterAFragment() {
        super(R.layout.fragment_result);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Get the phrase that TextListFragment put in the arguments Bundle.
        String text = requireArguments().getString("text");
        // Count uppercase A and lowercase a together.
        // Go through the text one character at a time and add 1 for every A or a.
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            if (letter == 'A' || letter == 'a') {
                count++;
            }
        }

        // Show the title and the result. %1$d in strings.xml gets replaced by "count".
        TextView title = view.findViewById(R.id.result_title);
        TextView details = view.findViewById(R.id.result_details);
        title.setText(R.string.a_title);
        details.setText(getString(R.string.a_result, count));
    }
}
