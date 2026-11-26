package com.example.iip_ld;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
public class TextStatsFragment extends Fragment {
    // Uses the same result layout as LetterAFragment (fragment_result.xml).
    public TextStatsFragment() {
        super(R.layout.fragment_result);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Get the phrase that TextListFragment put in the arguments Bundle.
        String text = requireArguments().getString("text");
        // Spaces count toward the length, but only letters count toward either case.
        // These are the Lithuanian vowels: a ą e ę ė i į y o u ų ū (written as unicode codes).
        String vowelLetters = "a\u0105e\u0119\u0117i\u012fyou\u0173\u016b";
        int vowels = 0;
        int uppercase = 0;
        int lowercase = 0;
        // Look at every character of the phrase one by one.
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            // Make it lowercase and check if it is in the vowel list.
            if (vowelLetters.indexOf(Character.toLowerCase(letter)) >= 0) {
                vowels++;
            }
            // Count big and small letters (spaces are neither, so they are skipped).
            if (Character.isUpperCase(letter)) {
                uppercase++;
            } else if (Character.isLowerCase(letter)) {
                lowercase++;
            }
        }

        // Show the title and all 4 numbers (they fill %1$d..%4$d in strings.xml).
        TextView title = view.findViewById(R.id.result_title);
        TextView details = view.findViewById(R.id.result_details);
        title.setText(R.string.stats_title);
        details.setText(getString(R.string.stats_result, text.length(), vowels, uppercase, lowercase));
    }
}
