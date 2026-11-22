package com.example.iip_ld;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class TextStatsFragment extends Fragment {
    public TextStatsFragment() {
        super(R.layout.fragment_result);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String text = requireArguments().getString("text");
        // Spaces count toward the length, but only letters count toward either case.
        String vowelLetters = "a\u0105e\u0119\u0117i\u012fyou\u0173\u016b";
        int vowels = 0;
        int uppercase = 0;
        int lowercase = 0;
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            if (vowelLetters.indexOf(Character.toLowerCase(letter)) >= 0) {
                vowels++;
            }
            if (Character.isUpperCase(letter)) {
                uppercase++;
            } else if (Character.isLowerCase(letter)) {
                lowercase++;
            }
        }

        TextView title = view.findViewById(R.id.result_title);
        TextView details = view.findViewById(R.id.result_details);
        title.setText(R.string.stats_title);
        details.setText(getString(R.string.stats_result, text.length(), vowels, uppercase, lowercase));
    }
}
