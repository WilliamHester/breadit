package me.williamhester.ui.views;

import android.view.View;

import me.williamhester.models.Account;

/**
 * Created by william on 8/1/14.
 */
public class CommentViewHolder extends VotableViewHolder {
    public CommentViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected Account getAccount() {
        return null;
    }
}
