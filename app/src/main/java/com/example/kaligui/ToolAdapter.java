package com.example.kaligui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ToolAdapter extends RecyclerView.Adapter<ToolAdapter.ToolViewHolder> {

    private List<Tool> toolList;
    private OnToolClickListener onToolClickListener;
    private OnToolLongClickListener onToolLongClickListener;

    public interface OnToolClickListener {
        void onToolClick(Tool tool);
    }

    public interface OnToolLongClickListener {
        void onToolLongClick(Tool tool);
    }

    public ToolAdapter(List<Tool> toolList, OnToolClickListener onToolClickListener, OnToolLongClickListener onToolLongClickListener) {
        this.toolList = toolList;
        this.onToolClickListener = onToolClickListener;
        this.onToolLongClickListener = onToolLongClickListener;
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tool_item, parent, false);
        return new ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        Tool tool = toolList.get(position);
        holder.toolName.setText(tool.getName());
        holder.toolDescription.setText(tool.getDescription());
        holder.itemView.setOnClickListener(v -> onToolClickListener.onToolClick(tool));
        holder.itemView.setOnLongClickListener(v -> {
            onToolLongClickListener.onToolLongClick(tool);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return toolList.size();
    }

    public static class ToolViewHolder extends RecyclerView.ViewHolder {
        TextView toolName, toolDescription;

        public ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            toolName = itemView.findViewById(R.id.tool_name);
            toolDescription = itemView.findViewById(R.id.tool_description);
        }
    }
}