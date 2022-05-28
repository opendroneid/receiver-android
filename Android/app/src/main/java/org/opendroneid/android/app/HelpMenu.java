package org.opendroneid.android.app;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.opendroneid.android.R;

public class HelpMenu extends DialogFragment {

    static HelpMenu newInstance() { return new HelpMenu(); }

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.help_text, container, true);

        TextView helpView = view.findViewById(R.id.bluetoothHelpText);
        String linkText = getString(R.string.bluetoothHelp);
        helpView.setText(Html.fromHtml(linkText));

        helpView = view.findViewById(R.id.beaconHelpText);
        linkText = getString(R.string.beaconHelp);
        helpView.setText(Html.fromHtml(linkText));
        helpView.setMovementMethod(LinkMovementMethod.getInstance());

        helpView = view.findViewById(R.id.nanHelpText);
        linkText = getString(R.string.nanHelp);
        helpView.setText(Html.fromHtml(linkText));
        return view;
    }
}
