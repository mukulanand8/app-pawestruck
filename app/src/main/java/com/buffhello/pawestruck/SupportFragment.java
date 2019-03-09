package com.buffhello.pawestruck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Contains FAQs and answers in an ExpandableListView
 */
public class SupportFragment extends Fragment {

    private ExpandableListAdapter expandableListAdapter;

    public SupportFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HashMap<String, List<String>> bind = new HashMap<>();
        List<String> parent = Arrays.asList(getResources().getStringArray(R.array.supp_questions));

        String[] a1 = {getResources().getString(R.string.supp_answer1)};
        bind.put(parent.get(0), Arrays.asList(a1));

        String[] a2 = {getResources().getString(R.string.supp_answer2)};
        bind.put(parent.get(1), Arrays.asList(a2));

        String[] a3 = {getResources().getString(R.string.supp_answer3)};
        bind.put(parent.get(2), Arrays.asList(a3));

        String[] a4 = {getResources().getString(R.string.supp_answer4)};
        bind.put(parent.get(3), Arrays.asList(a4));

        String[] a5 = {getResources().getString(R.string.supp_answer5)};
        bind.put(parent.get(4), Arrays.asList(a5));

        String[] a6 = {getResources().getString(R.string.supp_answer6)};
        bind.put(parent.get(5), Arrays.asList(a6));

        String[] a7 = {getResources().getString(R.string.supp_answer7)};
        bind.put(parent.get(6), Arrays.asList(a7));

        String[] a8 = {getResources().getString(R.string.supp_answer8)};
        bind.put(parent.get(7), Arrays.asList(a8));

        String[] a9 = {getResources().getString(R.string.supp_answer9)};
        bind.put(parent.get(8), Arrays.asList(a9));

        String[] a10 = {getResources().getString(R.string.supp_answer10)};
        bind.put(parent.get(9), Arrays.asList(a10));

        String[] a11 = {getResources().getString(R.string.supp_answer11)};
        bind.put(parent.get(10), Arrays.asList(a11));

        String[] a12 = {getResources().getString(R.string.supp_answer12)};
        bind.put(parent.get(11), Arrays.asList(a12));

        String[] a13 = {getResources().getString(R.string.supp_answer13)};
        bind.put(parent.get(12), Arrays.asList(a13));

        String[] a14 = {getResources().getString(R.string.supp_answer14)};
        bind.put(parent.get(13), Arrays.asList(a14));

        String[] a15 = {getResources().getString(R.string.supp_answer15)};
        bind.put(parent.get(14), Arrays.asList(a15));

        String[] a16 = {getResources().getString(R.string.supp_answer16)};
        bind.put(parent.get(15), Arrays.asList(a16));

        expandableListAdapter = new ExpandableListAdapter(getContext(), parent, bind);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support, container, false);
        ExpandableListView expandableListView = view.findViewById(R.id.supp_expandable_list);
        expandableListView.setAdapter(expandableListAdapter);
        return view;
    }
}
