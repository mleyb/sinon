package com.bluezero.sinon.widgets;

import com.bluezero.sinon.R;

import android.app.Dialog;
import android.content.Context;

public class LoginDialog extends Dialog {
	public LoginDialog(final Context context) {
		super(context);
		
        setContentView(R.layout.login_dialog);  
    }
}
