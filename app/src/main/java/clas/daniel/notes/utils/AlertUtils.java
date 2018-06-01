package clas.daniel.notes.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import clas.daniel.notes.R;

public class AlertUtils {

    public static void showAlert(Context context, String title, String message,
                                 DialogInterface.OnClickListener onClick, boolean cancelable) {
        new AlertDialog.Builder(context).setMessage(message)
                .setTitle(TextUtils.isNullOrEmpty(title) ? context.getString(R.string.app_name) : title)
                .setCancelable(cancelable).setNeutralButton(android.R.string.ok, onClick).create().show();
    }

    public static void showAlert(Context context, String message) {
        showAlert(context, null, message, null, true);
    }

    public static void showAlert(Context context, String title, String message) {
        showAlert(context, title, message, null, true);
    }

    public static void showAlertWithYesNo(Context context, String title, String message,
                                          DialogInterface.OnClickListener onClick, boolean cancelable) {
        new AlertDialog.Builder(context).setMessage(message)
                .setTitle(TextUtils.isNullOrEmpty(title) ? context.getString(R.string.app_name) : title)
                .setCancelable(cancelable).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, onClick).create().show();
    }

    public static void showAlertWithYesNo(Context context, String message, DialogInterface.OnClickListener onClick) {
        showAlertWithYesNo(context, null, message, onClick, true);
    }

    public static void showAlertWithYesNo(Context context, String title, String message, DialogInterface.OnClickListener onClick) {
        showAlertWithYesNo(context, title, message, onClick, true);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
