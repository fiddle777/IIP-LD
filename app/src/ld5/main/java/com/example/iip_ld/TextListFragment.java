package com.example.iip_ld;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

public class TextListFragment extends Fragment {
    public TextListFragment() {
        super(R.layout.fragment_text_list);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView list = view.findViewById(R.id.text_list);
        String[] phrases = getResources().getStringArray(R.array.phrases);
        list.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, phrases));

        list.setOnItemClickListener((parent, row, position, id) -> {
            String text = phrases[position];

            // Choose the result fragment and pass it the selected phrase.
            Fragment result;
            if (text.contains("A") || text.contains("a")) {
                result = new LetterAFragment();
            } else {
                result = new TextStatsFragment();
            }
            Bundle arguments = new Bundle();
            arguments.putString("text", text);
            result.setArguments(arguments);

            // Replace only the result panel. Each tap adds one step for Android Back.
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.result_container, result)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
