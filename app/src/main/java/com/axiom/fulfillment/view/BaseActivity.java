package com.axiom.fulfillment.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.axiom.fulfillment.api.APIClient;
import com.axiom.fulfillment.api.APIInterface;
import com.axiom.fulfillment.helper.UserSharedPreferences;
import com.axiom.fulfillment.model.tokenresponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BaseActivity extends AppCompatActivity {

    ProgressDialog progress;
    private UserSharedPreferences userpref;

    public void startLoader(String msg, Context ctx) {
        progress = new ProgressDialog(ctx);
        progress.setMessage(msg);
        progress.setCancelable(false);
        progress.show();
    }

    public void stopLoader() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    public void ShowToast(String msg, Context ctx) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    public void getToken(Context ctx) {

        APIInterface apiService = APIClient.gettoken().create(APIInterface.class);
        userpref = new UserSharedPreferences(ctx);
        Call<tokenresponse> stringCall = apiService.getaccesstoken("password", "a_karani", "1234");
        stringCall.enqueue(new Callback<tokenresponse>() {
            @Override
            public void onResponse(Call<tokenresponse> call, Response<tokenresponse> response) {
                if (response.isSuccessful()) {
                    tokenresponse responseString = response.body();
                    userpref.setAccessToken(responseString.getAccessToken());
//                    userpref.setKeyTokenExpiry(String.valueOf(responseString.getExpiresIn()));
//                    String temp=Calendar.getInstance().getTime().toString();
//                    userpref.setKeyTokenTime(temp);
                }

            }

            @Override
            public void onFailure(Call<tokenresponse> call, Throwable t) {
            }
        });
    }


}
