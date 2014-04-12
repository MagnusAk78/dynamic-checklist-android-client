package com.ma.dc;

import android.content.DialogInterface;

final class TagChooser implements DialogInterface.OnClickListener {

    private int chosenTag;
    private final String[] tags;

    TagChooser(String[] tags, int startChoice) {
        this.tags = tags;
        chosenTag = startChoice;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        chosenTag = which;
    }

    String getChosenTag() {
        if (chosenTag > 0 && chosenTag < tags.length) {
            return tags[chosenTag];
        }
        return null;
    }
}
