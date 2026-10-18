/*
 * Copyright (C) 2025 Yaroslav Pronin <proninyaroslav@mail.ru>
 *
 * This file is part of LibreTorrent.
 *
 * LibreTorrent is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreTorrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreTorrent.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.proninyaroslav.libretorrent.ui.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import org.proninyaroslav.libretorrent.R;

@SuppressLint("RestrictedApi")
public class SettingsHeaderAdapter extends CustomPreferenceGroupAdapter {
    private boolean isSelectable = false;
    private String curOpenItemKey;
    private int rowHeight = 0;

    public SettingsHeaderAdapter(@NonNull PreferenceGroup preferenceGroup) {
        super(preferenceGroup);
    }

    /**
     * Each row's height in pixels, so that every header fits on one screen; 0 leaves the rows
     * their own height.
     */
    public void setRowHeight(int px) {
        if (px == rowHeight) {
            return;
        }
        rowHeight = px;
        notifyDataSetChanged();
    }

    public void setSelectable(boolean selectable) {
        var prev = isSelectable;
        isSelectable = selectable;
        if (curOpenItemKey != null && prev != selectable) {
            var pos = getPreferenceAdapterPosition(curOpenItemKey);
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }
        }
    }

    public void markAsOpen(String key) {
        var oldPos = curOpenItemKey == null
                ? RecyclerView.NO_POSITION
                : getPreferenceAdapterPosition(curOpenItemKey);
        var newPos = getPreferenceAdapterPosition(key);
        curOpenItemKey = key;
        if (oldPos != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPos);
        }
        notifyItemChanged(newPos);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        var pref = getItem(0);
        if (pref != null) {
            markAsOpen(pref.getKey());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        var context = holder.itemView.getContext();
        var item = getItem(position);

        if (rowHeight > 0) {
            var view = holder.itemView;
            var params = view.getLayoutParams();
            params.height = rowHeight;
            view.setLayoutParams(params);
            // The row's words sit in its first child, which keeps a 64dp minimum; the rule under
            // them is the last, and has to stay inside the shorter row.
            if (view instanceof ViewGroup group && group.getChildCount() > 1) {
                var words = group.getChildAt(0);
                var rule = group.getChildAt(group.getChildCount() - 1);
                words.setMinimumHeight(0);
                var wordsParams = words.getLayoutParams();
                wordsParams.height = rowHeight - rule.getLayoutParams().height;
                words.setLayoutParams(wordsParams);
            }
        }

        if (item != null && isSelectable && item.getKey().equals(curOpenItemKey)) {
            setSelectableBackground(context, holder);
        }
    }

    private void setSelectableBackground(@NonNull Context context, @NonNull PreferenceViewHolder holder) {
        try (var a = ThemeEnforcement.obtainStyledAttributes(
                context, null, R.styleable.MaterialCardView, R.attr.materialCardViewStyle, R.style.App_Components_ListItem)) {
            var background = new MaterialShapeDrawable();
            background.setTint(MaterialColors.getColor(holder.itemView, R.attr.colorPrimaryContainer));
            @SuppressLint("PrivateResource")
            var shape = ShapeAppearanceModel.builder(
                            context,
                            a.getResourceId(R.styleable.MaterialCardView_shapeAppearance, 0),
                            a.getResourceId(R.styleable.MaterialCardView_shapeAppearanceOverlay, 0))
                    .build();
            background.setShapeAppearanceModel(shape);
            holder.itemView.setBackground(background);
        }
    }
}
