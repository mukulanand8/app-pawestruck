package com.buffhello.pawestruck;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

/**
 * Contains FAQs and answers in an ExpandableListView
 */
public class SupportFragment extends Fragment {

    private ExpandableListAdapter expandableListAdapter;

    public SupportFragment() {
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_supp, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Shows Terms and Conditions and Privacy Policy
     */
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.information) {
            final SpannableString s = new SpannableString(getString(R.string.supp_info));
            Linkify.addLinks(s, Linkify.WEB_URLS);
            AlertDialog dialogBuilder = new AlertDialog.Builder(getContext()).setMessage(s).show();
            ((TextView) dialogBuilder.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        HashMap<String, List<String>> bind = new HashMap<>();
        List<String> parent = Arrays.asList(getResources().getStringArray(R.array.supp_questions));

        String[] category1 = {""};
        bind.put(parent.get(0), Arrays.asList(category1));

        String[] a1 = {getResources().getString(R.string.supp_answer1)};
        bind.put(parent.get(1), Arrays.asList(a1));

        String[] a2 = {getResources().getString(R.string.supp_answer2)};
        bind.put(parent.get(2), Arrays.asList(a2));

        String[] category2 = {""};
        bind.put(parent.get(3), Arrays.asList(category2));

        String[] a3 = {getResources().getString(R.string.supp_answer3)};
        bind.put(parent.get(4), Arrays.asList(a3));

        String[] a4 = {getResources().getString(R.string.supp_answer4)};
        bind.put(parent.get(5), Arrays.asList(a4));

        String[] a5 = {getResources().getString(R.string.supp_answer5)};
        bind.put(parent.get(6), Arrays.asList(a5));

        String[] a6 = {getResources().getString(R.string.supp_answer6)};
        bind.put(parent.get(7), Arrays.asList(a6));

        String[] a7 = {getResources().getString(R.string.supp_answer7)};
        bind.put(parent.get(8), Arrays.asList(a7));

        String[] a8 = {getResources().getString(R.string.supp_answer8)};
        bind.put(parent.get(9), Arrays.asList(a8));

        String[] category3 = {""};
        bind.put(parent.get(10), Arrays.asList(category3));

        String[] a9 = {getResources().getString(R.string.supp_answer9)};
        bind.put(parent.get(11), Arrays.asList(a9));

        String[] a10 = {getResources().getString(R.string.supp_answer10)};
        bind.put(parent.get(12), Arrays.asList(a10));

        String[] a11 = {getResources().getString(R.string.supp_answer11)};
        bind.put(parent.get(13), Arrays.asList(a11));

        String[] a12 = {getResources().getString(R.string.supp_answer12)};
        bind.put(parent.get(14), Arrays.asList(a12));

        String[] category4 = {""};
        bind.put(parent.get(15), Arrays.asList(category4));

        String[] a13 = {getResources().getString(R.string.supp_answer13)};
        bind.put(parent.get(16), Arrays.asList(a13));

        String[] a14 = {getResources().getString(R.string.supp_answer14)};
        bind.put(parent.get(17), Arrays.asList(a14));

        String[] a15 = {getResources().getString(R.string.supp_answer15)};
        bind.put(parent.get(18), Arrays.asList(a15));

        String[] a16 = {getResources().getString(R.string.supp_answer16)};
        bind.put(parent.get(19), Arrays.asList(a16));

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
