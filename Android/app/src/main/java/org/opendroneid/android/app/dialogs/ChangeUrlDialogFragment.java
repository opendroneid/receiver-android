package org.opendroneid.android.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.opendroneid.android.R;
import org.opendroneid.android.UserFlowUtil;
import org.opendroneid.android.app.network.manager.UrlManager;

public class ChangeUrlDialogFragment extends DialogFragment {

    private TextInputEditText urlEditText;
    private TextInputLayout urlInputLayout;
    private AppCompatTextView textResetUrl;
    private String url;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fragment_change_url, null);

        urlEditText = dialogView.findViewById(R.id.edit_text_url);
        urlInputLayout = dialogView.findViewById(R.id.layout_url);
        textResetUrl = dialogView.findViewById(R.id.text_reset_default);

        builder.setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.button_user_reset_password), null)
                .setNegativeButton(R.string.button_user_cancel, (dialog, id) -> {
                    dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button changeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            changeButton.setText(getResources().getString(R.string.action_change));
            changeButton.setOnClickListener(view -> changeUrl());
        });

        urlEditText.addTextChangedListener(UserFlowUtil.getEmailTextWatcher(urlInputLayout));

        textResetUrl.setOnClickListener(view -> {
            UrlManager.deleteUrl(requireContext());
            Toast.makeText(requireContext(), getString(R.string.text_url_default), Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return dialog;
    }

    private void changeUrl() {
        String newUrl = urlEditText.getText().toString().trim();
        if(isValidUrl(newUrl)){
            UrlManager.saveUrl(requireContext(), newUrl);
            Toast.makeText(requireContext(), getString(R.string.text_url_changed), Toast.LENGTH_SHORT).show();
            dismiss();
        }else{
            Toast.makeText(requireContext(), getString(R.string.text_url_invalid), Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isValidUrl(String url) {
        // You can add more validation logic here if needed
        return Patterns.WEB_URL.matcher(url).matches();
    }

}
