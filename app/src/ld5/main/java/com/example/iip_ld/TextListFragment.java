package com.example.iip_ld;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

public class TextListFragment extends Fragment {
    // Tell the Fragment which layout file to use: res/layout/fragment_text_list.xml.
    public TextListFragment() {
        super(R.layout.fragment_text_list);
    }

    // Runs after the fragment's layout is created - here we fill it with data.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView list = view.findViewById(R.id.text_list);
        // Read the phrases from strings.xml (<string-array name="phrases">).
        String[] phrases = getResources().getStringArray(R.array.phrases);
        // The Adapter turns each string into one row of the list.
        list.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, phrases));

        // What happens when a row is tapped. "position" = which row (0, 1, 2...).
        list.setOnItemClickListener((parent, row, position, id) -> {
            String text = phrases[position];

            // Choose the result fragment and pass it the selected phrase.
            Fragment result;
            if (text.contains("A") || text.contains("a")) {
                result = new LetterAFragment();     // has an A -> count the A letters
            } else {
                result = new TextStatsFragment();   // no A -> show text statistics
            }
            // A Bundle is a small "bag" of data. We put the phrase in it under the name "text".
            Bundle arguments = new Bundle();
            arguments.putString("text", text);
            result.setArguments(arguments);

            // Replace only the result panel. Each tap adds one step for Android Back.
            // (addToBackStack means pressing Back shows the previous result again.)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.result_container, result)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
