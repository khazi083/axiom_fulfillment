package com.axiom.fulfillment.view;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.axiom.fulfillment.R;
import com.axiom.fulfillment.api.APIClient;
import com.axiom.fulfillment.api.APIInterface;
import com.axiom.fulfillment.helper.UserSharedPreferences;
import com.axiom.fulfillment.model.UserLogin;
import com.axiom.fulfillment.model.UserLoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    EditText username, password;
    Button login, new_reg;
    UserSharedPreferences userpref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_login);
        getToken(this);
        username = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.btnLogin);
        new_reg = findViewById(R.id.new_reg);
        userpref = new UserSharedPreferences(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        if (userpref.getUserId() != 0) {
            Intent home = new Intent(LoginActivity.this, HomeDrawerActivity.class);
            startActivity(home);
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifylogin();
            }
        });

        new_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newreg = new Intent(LoginActivity.this, Registration.class);
                startActivity(newreg);
            }
        });
    }

    private void verifylogin() {
        if (username.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.empty_username, Toast.LENGTH_LONG).show();
            return;
        } else if (password.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.empty_password, Toast.LENGTH_LONG).show();
            return;
        }

        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        UserLogin user = new UserLogin(username.getText().toString(), password.getText().toString());
        Call<UserLoginResponse> stringCall = apiService.loginapi(user);
        startLoader(getString(R.string.verify_login), this);
        stringCall.enqueue(new Callback<UserLoginResponse>() {
            @Override
            public void onResponse(Call<UserLoginResponse> call, Response<UserLoginResponse> response) {
                stopLoader();
                if (response.body().getIsValiduser() && response.body().getUserDetails().size() > 0) {
                    UserLoginResponse responseString = response.body();
                    userpref.setRoleId(responseString.getEscalationId());
                    userpref.setUserId(responseString.getUserDetails().get(0).getAudsUserId());
                    userpref.setKeyUserCode(responseString.getUserDetails().get(0).getAudsUserCode());
                    userpref.setFirstName(responseString.getUserDetails().get(0).getAudsUserName());
                    userpref.setKeyEmpCode(responseString.getUserDetails().get(0).getAemsEmployeeCode());
                    userpref.setKeyUserRole(responseString.getEscalation());
                    Intent home = new Intent(LoginActivity.this, HomeDrawerActivity.class);
                    startActivity(home);
                } else
                    ShowToast(getString(R.string.invalid_login), LoginActivity.this);

            }

            @Override
            public void onFailure(Call<UserLoginResponse> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), LoginActivity.this);
            }
        });

    }

}
