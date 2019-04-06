package com.buffhello.pawestruck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Adapter for ExpandableListView in Support Fragment
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;

    /**
     * List of questions
     */
    private List<String> parent;

    /**
     * Each question is mapped to a list of answers containing only one element/answer.
     * Hence size of children list is always 1 and position of child is always 0
     */
    private HashMap<String, List<String>> childMap;

    ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listChildData) {
        mContext = context;
        parent = listDataHeader;
        childMap = listChildData;
    }

    @Override
    public int getGroupCount() {
        return parent.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return parent.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childMap.get(parent.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Parent (Question) View
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater parentInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (groupPosition == 0 || groupPosition == 3 || groupPosition == 10 || groupPosition == 14) {
            convertView = parentInflater.inflate(R.layout.supp_category, null);
            TextView tvCategory = convertView.findViewById(R.id.supp_tv_category);
            tvCategory.setText((String) getGroup(groupPosition));
        } else {
            convertView = parentInflater.inflate(R.layout.supp_list_parent, null);

            TextView tvParent = convertView.findViewById(R.id.supp_tv_question);
            tvParent.setText((String) getGroup(groupPosition));

            ImageView ivExpanded = convertView.findViewById(R.id.supp_iv_expand);
            if (isExpanded) ivExpanded.setImageResource(R.drawable.arrow_up);
            else ivExpanded.setImageResource(R.drawable.arrow_down);
        }

        return convertView;
    }

    /**
     * Child (Answer) View
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater childInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (groupPosition != 0 && groupPosition != 3 && groupPosition != 10 && groupPosition != 14) {
            convertView = childInflater.inflate(R.layout.supp_list_child, null);

            TextView tvChild = convertView.findViewById(R.id.supp_tv_answer);
            tvChild.setText((String) getChild(groupPosition, 0));
        } else convertView = childInflater.inflate(R.layout.view_null, null);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
