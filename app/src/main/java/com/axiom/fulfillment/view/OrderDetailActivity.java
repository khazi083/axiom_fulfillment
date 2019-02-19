package com.axiom.fulfillment.view;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.axiom.fulfillment.R;
import com.axiom.fulfillment.adaptor.OrderDetailsAdaptor;
import com.axiom.fulfillment.adaptor.PaymentDetailsAdaptor;
import com.axiom.fulfillment.api.APIClient;
import com.axiom.fulfillment.api.APIInterface;

import com.axiom.fulfillment.helper.UserSharedPreferences;
import com.axiom.fulfillment.helper.constants;
import com.axiom.fulfillment.model.Biker;
import com.axiom.fulfillment.model.BikerList;
import com.axiom.fulfillment.model.BikerRequest;
import com.axiom.fulfillment.model.ButtonStatus;
import com.axiom.fulfillment.model.ButtonStatusResult;
import com.axiom.fulfillment.model.CancelOrder;
import com.axiom.fulfillment.model.Courier;
import com.axiom.fulfillment.model.CourierList;
import com.axiom.fulfillment.model.CourierListInput;
import com.axiom.fulfillment.model.CourierOrder;
import com.axiom.fulfillment.model.CourierdispatchResponse;
import com.axiom.fulfillment.model.DeliveryRequest;
import com.axiom.fulfillment.model.OrderDetail;
import com.axiom.fulfillment.model.OrderDetailsInput;

import com.axiom.fulfillment.model.Order_details;
import com.axiom.fulfillment.model.SystemCourier;
import com.axiom.fulfillment.model.UserDetails;
import com.axiom.fulfillment.model.CommonApiResponse;
import com.axiom.fulfillment.model.bikerdispatch;
import com.axiom.fulfillment.model.createcourierorder;


import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OrderDetailActivity extends BaseActivity {

    private Order_details details;
    String order_status, ChannelCode, source;
    int userid, OABOID, OADBID;
    String orderno, user_name, user_code, order_seqno;
    UserSharedPreferences upref;
    EditText ordernotxt, orderdate, sorce, purchaseno, remarks, amount, totamount, comments;
    EditText cust_name, cust_mob, shipping_address;
    RecyclerView orderitems, paymentdetails;
    OrderDetailsAdaptor orderDetailsAdaptor;
    PaymentDetailsAdaptor paymentDetailsAdaptor;
    RelativeLayout main;
    Button deliver, pick, cancel, assign_biker;
    Button assign_courier, reshedule, invoice,order_cancel,invoice_awb;
    int req_code = 201;
    private List<Biker> bikers;
    private List<SystemCourier> systemCouriers;
    private ArrayList<String> bikernames;
    private ArrayList<String> couriernames;
    private ButtonStatusResult btnresult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_orderdetails);
        main = findViewById(R.id.main);
        main.setVisibility(View.INVISIBLE);
        ordernotxt = findViewById(R.id.morderno);
        orderdate = findViewById(R.id.morderdate);
        sorce = findViewById(R.id.msource);

        purchaseno = findViewById(R.id.purchaseno);
        remarks = findViewById(R.id.mremarks);
        amount = findViewById(R.id.mamount);

        totamount = findViewById(R.id.mamounttotal);
        comments = findViewById(R.id.mcomments);
        cust_name = findViewById(R.id.mname);

        cust_mob = findViewById(R.id.mmobile);
        shipping_address = findViewById(R.id.mshipadress);
        orderitems = findViewById(R.id.order_itemList);
        paymentdetails = findViewById(R.id.payment_itemList);
        deliver = findViewById(R.id.deliver);
        pick = findViewById(R.id.pick);
        cancel = findViewById(R.id.cancel);
        assign_biker = findViewById(R.id.assign_biker);
        assign_courier = findViewById(R.id.assign_courier);
        reshedule = findViewById(R.id.reshedule);
        invoice = findViewById(R.id.invoice);
        order_cancel=findViewById(R.id.order_cancel);
        couriernames = new ArrayList<>();
        bikernames = new ArrayList<>();

        deliver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ondeliver();
            }
        });
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog("pick");
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog("cancel");
            }
        });
        reshedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog("reshedule");
            }
        });
        assign_biker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getbikerlist();

            }
        });
        assign_courier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCourierList();
            }
        });

        invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String encryptorder = encrypt("7061737323313233",  details.getOrderDetail().get(0).getOrderSequence().toString());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fulfilment.axiomtelecom.com/Reports/Receipt/MPerformaInvoice.aspx?OrderNo=" + encryptorder));
                //        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fulfilment.axiomtelecom.com/Reports/Receipt/PerfomaInvoices.aspx?hs=VweuOGU8tidM6ei1sy2T3Q=="));

                startActivity(browserIntent);
            }
        });
        order_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog("cancel order");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.Order_Details));
        upref = new UserSharedPreferences(this);
        getToken(this);

        orderno = getIntent().getStringExtra(constants.ORDERNO);
        user_name = getIntent().getStringExtra(constants.User_name);
        userid = getIntent().getIntExtra(constants.userid, 0);
        user_code = getIntent().getStringExtra(constants.usercode);
        order_status = getIntent().getStringExtra(constants.order_type);
        OABOID = getIntent().getIntExtra(constants.OABOID, 0);
        OADBID = getIntent().getIntExtra(constants.OADBID, 0);
        order_seqno = getIntent().getStringExtra(constants.ObohSeq);
        ChannelCode = getIntent().getStringExtra(constants.CHANNELCODE);
        source = getIntent().getStringExtra(constants.SOURCE);
        if (!order_status.equalsIgnoreCase(constants.ORDER_CREATED)&& !order_status.equalsIgnoreCase(constants.ORDER_PICKED)&&
                !order_status.equalsIgnoreCase(constants.ORDER_DELIVERED)&& !order_status.equalsIgnoreCase(constants.ORDER_CANCELLED))
            getButtonStatus();
        getorderdata();

    }

    private void getButtonStatus() {
        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        ButtonStatus input = new ButtonStatus();
        if(order_seqno!=null && !order_seqno.isEmpty())
            input.setObohSeq(Double.valueOf(order_seqno));
        input.setUserRole(upref.getKeyUserRole());
        input.setUserDetails(new UserDetails(upref.getUserId(), upref.getFirstName(), upref.getKeyUserCode(), upref.getKeyEmpCode()));
        Call<ButtonStatusResult> stringCall = apiService.getButtonStatus(input);
        stringCall.enqueue(new Callback<ButtonStatusResult>() {
            @Override
            public void onResponse(Call<ButtonStatusResult> call, Response<ButtonStatusResult> response) {
                stopLoader();
                if (response.body()!=null && response.body().getStatus().getOutResult()) {
                    btnresult = response.body();
                    showbtn();
                } else {
                    ShowToast("Error :" + response.body().getStatus().getOutMessage(), OrderDetailActivity.this);
                }
            }

            @Override
            public void onFailure(Call<ButtonStatusResult> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), OrderDetailActivity.this);
            }
        });
    }

    private void showbtn() {

        invoice.setVisibility(View.GONE);
        order_cancel.setVisibility(View.GONE);

        if (btnresult.getCanCancel()){
            order_cancel.setVisibility(View.VISIBLE);
        }
        if(btnresult.getShowInvoice()){
            invoice.setVisibility(View.VISIBLE);
        }
    //    main.setVisibility(View.VISIBLE);
    }


    public static String encrypt(String key, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));
            System.out.println("encrypted string: " + Base64.encodeToString(encrypted, Base64.DEFAULT));
            return Base64.encodeToString(encrypted, Base64.DEFAULT).replace("\n","");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void getbikerlist() {
        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        CourierListInput ci = new CourierListInput();
        UserDetails usr = new UserDetails(upref.getUserId(), upref.getFirstName(), upref.getKeyUserCode(), upref.getKeyEmpCode());
        ci.setUserDetails(usr);
        ci.setAclmChannelCode(ChannelCode);
        Call<BikerList> stringCall = apiService.getBikerlist(ci);
        startLoader(getString(R.string.loading_bikers), this);
        stringCall.enqueue(new Callback<BikerList>() {
            @Override
            public void onResponse(Call<BikerList> call, Response<BikerList> response) {
                stopLoader();
                bikernames.clear();
                if (response.body().getStatus().getOutResult()) {
                    bikers = response.body().getBikers();
                    for (int i = 0; i < bikers.size(); i++)
                        bikernames.add(bikers.get(i).getEmployeeName());
                    dispatchdailog("biker");
                } else {
                    ShowToast("Error :" + response.body().getStatus().getOutMessage(), OrderDetailActivity.this);
                }

            }

            @Override
            public void onFailure(Call<BikerList> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), OrderDetailActivity.this);
            }
        });

    }

    private void getCourierList() {
        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        CourierListInput ci = new CourierListInput();
        UserDetails usr = new UserDetails(upref.getUserId(), upref.getFirstName(), upref.getKeyUserCode(), upref.getKeyEmpCode());
        ci.setUserDetails(usr);
        ci.setAclmChannelCode(ChannelCode);
        ci.setFulFilSystem(source);
        Call<CourierList> stringCall = apiService.getCourierlist(ci);
        startLoader(getString(R.string.loading_courier), this);
        stringCall.enqueue(new Callback<CourierList>() {
            @Override
            public void onResponse(Call<CourierList> call, Response<CourierList> response) {
                stopLoader();
                couriernames.clear();
                if (response.body().getStatus().getOutResult()) {
                    systemCouriers = response.body().getSystemCouriers();
                    for (int i = 0; i < systemCouriers.size(); i++)
                        couriernames.add(systemCouriers.get(i).getOcmName());
                    dispatchdailog("courier");
                } else {
                    ShowToast("Error :" + response.body().getStatus().getOutMessage(), OrderDetailActivity.this);
                }

            }

            @Override
            public void onFailure(Call<CourierList> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), OrderDetailActivity.this);
            }
        });

    }

    private void dispatchdailog(final String dispatchtype) {
        final Dialog dialogView = new Dialog(OrderDetailActivity.this);
        dialogView.setCancelable(true);
        dialogView.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogView.setContentView(R.layout.dispatch_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        OrderDetailActivity.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthLcl = (int) (displayMetrics.widthPixels * 0.8f);
        int heightLcl = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialogView.getWindow().setLayout(widthLcl, heightLcl);
        Button dispatch = dialogView.findViewById(R.id.dispatch);
        final Spinner dispatchlist = dialogView.findViewById(R.id.dispatchlist);
        TextView dispathtype = dialogView.findViewById(R.id.dispatchtype);
        if (dispatchtype.equals("biker")) {
            dispathtype.setText("Select Biker");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(OrderDetailActivity.this, android.R.layout.simple_spinner_item, bikernames);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dispatchlist.setAdapter(dataAdapter);
        } else if (dispatchtype.equals("courier")) {
            dispathtype.setText("Select Courier");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(OrderDetailActivity.this, android.R.layout.simple_spinner_item, couriernames);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dispatchlist.setAdapter(dataAdapter);
        }
        dispatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dispatchtype.equals("biker")) {
                    for (int i = 0; i < bikers.size(); i++) {
                        if (dispatchlist.getSelectedItem().toString().equalsIgnoreCase(bikers.get(i).getEmployeeName())) {
                            dispatchbiker(i);
                            break;
                        }
                    }
                } else if (dispatchtype.equals("courier")) {
                    for (int i = 0; i < systemCouriers.size(); i++) {
                        if (dispatchlist.getSelectedItem().toString().equalsIgnoreCase(systemCouriers.get(i).getOcmName())) {
                            dispatchcourier(i);
                            break;
                        }
                    }
                }
                dialogView.dismiss();
            }
        });


        dialogView.show();
    }

    private void dispatchcourier(int pos) {

        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        createcourierorder courier = new createcourierorder();
        UserDetails usr = new UserDetails(upref.getUserId(), upref.getFirstName(), upref.getKeyUserCode(), upref.getKeyEmpCode());
        courier.setUserDetails(usr);
        Courier c = new Courier();
        c.setCourierID(systemCouriers.get(pos).getOcmId());
        c.setCourierName(systemCouriers.get(pos).getOcmName());
        courier.setCourier(c);
        List<CourierOrder> orders = new ArrayList<>();
        CourierOrder co = new CourierOrder();
        co.setAxiomOrderNo(orderno);
        orders.add(co);
        courier.setOrders(orders);

        Call<CourierdispatchResponse> stringCall = apiService.createCourierrorder(courier);
        startLoader("Assigning order to " + systemCouriers.get(pos).getOcmName(), this);
        stringCall.enqueue(new Callback<CourierdispatchResponse>() {
            @Override
            public void onResponse(Call<CourierdispatchResponse> call, Response<CourierdispatchResponse> response) {
                stopLoader();

                if (response.body() != null && response.body().getStatus().getOutResult()) {
                    ShowToast("Order assigned Successfully.", OrderDetailActivity.this);
                    finish();
                } else {
                    ShowToast(response.body().getStatus().getOutMessage(), OrderDetailActivity.this);
                }
            }

            @Override
            public void onFailure(Call<CourierdispatchResponse> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), OrderDetailActivity.this);
            }
        });

    }

    private void dispatchbiker(Integer pos) {

        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        bikerdispatch bk = new bikerdispatch();
        UserDetails usr = new UserDetails(upref.getUserId(), upref.getFirstName(), upref.getKeyUserCode(), upref.getKeyEmpCode());
        bk.setUserDetails(usr);
        bk.setOaboStatus("ORDER_CREATE");
        bk.setObohSeq(Double.valueOf(order_seqno));
        bk.setOadbId(bikers.get(pos).getOadbId());
        Call<CommonApiResponse> stringCall = apiService.createbikerorder(bk);
        startLoader("Assigning order to " + bikers.get(pos).getEmployeeName(), this);
        stringCall.enqueue(new Callback<CommonApiResponse>() {
            @Override
            public void onResponse(Call<CommonApiResponse> call, Response<CommonApiResponse> response) {
                stopLoader();

                if (response.body() != null && response.body().getStatus().getOutResult()) {
                    ShowToast("Order assigned Successfully.", OrderDetailActivity.this);
                    finish();
                } else {
                    ShowToast("Error :" + response.body().getStatus().getOutMessage(), OrderDetailActivity.this);
                }

            }

            @Override
            public void onFailure(Call<CommonApiResponse> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), OrderDetailActivity.this);
            }
        });
    }

    private void alertDialog(final String msgtxt) {
        final Dialog dialogView = new Dialog(OrderDetailActivity.this);
        dialogView.setCancelable(true);
        dialogView.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogView.setContentView(R.layout.exit_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        OrderDetailActivity.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthLcl = (int) (displayMetrics.widthPixels * 0.8f);
        int heightLcl = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialogView.getWindow().setLayout(widthLcl, heightLcl);
        Button yesBtn = dialogView.findViewById(R.id.btn_yes);
        Button noBtn = dialogView.findViewById(R.id.btn_no);
        TextView msg = dialogView.findViewById(R.id.message);
        TextView header = dialogView.findViewById(R.id.txt_header);

        if (msgtxt.equalsIgnoreCase("pick")) {
            msg.setText("You want to pick this order for delivery ?");
            header.setText("Order Pick");
        } else if (msgtxt.equalsIgnoreCase("cancel")) {
            msg.setText("You want to Cancel this order?");
            header.setText("Order Cancel");
        } else if (msgtxt.equalsIgnoreCase("reshedule")) {
            msg.setText("You want to Postpone this order?");
            header.setText("Order Postoned");
        } else if (msgtxt.equalsIgnoreCase("cancel order")) {
            msg.setText("You want to Cancel this order?");
            header.setText("Order Cancel");
        }

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgtxt.equalsIgnoreCase("pick")) {
                    onpick();
                } else if (msgtxt.equalsIgnoreCase("cancel")) {
                    oncancel();
                } else if (msgtxt.equalsIgnoreCase("reshedule")) {
                    onreshedule();
                }else if (msgtxt.equalsIgnoreCase("cancel order")) {
                    cancel_order();
                }
                dialogView.cancel();
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.cancel();
            }
        });
        dialogView.show();

    }

    private void cancel_order() {
        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        CancelOrder co= new CancelOrder();
        co.setUserDetails(new UserDetails(upref.getUserId(), upref.getFirstName()));
        co.setObohOrderNo(orderno);
        if(order_seqno!=null && !order_seqno.isEmpty())
            co.setObohSeq(Double.valueOf(order_seqno));
        co.setReason("order cancelled from mobile");
        Call<CommonApiResponse> stringCall = apiService.cancelOrder(co);
        startLoader(getString(R.string.loading), this);
        stringCall.enqueue(new Callback<CommonApiResponse>() {
            @Override
            public void onResponse(Call<CommonApiResponse> call, Response<CommonApiResponse> response) {
                stopLoader();
                if (response.body().getStatus().getOutResult()) {
                    ShowToast("Order Cancelled.", OrderDetailActivity.this);
                    finish();
                } else {
                    ShowToast("Error :" + response.body().getStatus().getOutMessage(), OrderDetailActivity.this);
                }

            }

            @Override
            public void onFailure(Call<CommonApiResponse> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), OrderDetailActivity.this);
            }
        });
    }

    private void onreshedule() {
        DeliveryRequest req = new DeliveryRequest();
        BikerRequest bike = new BikerRequest();
        req.setAction("ORDER_POSTPONE");
        bike.setOaboId(String.valueOf(OABOID));
        bike.setOadbId(String.valueOf(OADBID));
        bike.setOaboStatus("ORDER_POSTPONE");
        bike.setOaboComments("ORDER_POSTPONE");
        bike.setUserDetails(new UserDetails(upref.getUserId(), upref.getFirstName()));
        req.setBikerRequest(bike);
        apicall(req, "Order Postponed.");
    }

    private void getorderdata() {
        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        OrderDetailsInput input = new OrderDetailsInput();
        input.setOrderNo(orderno);
        input.setUser(new UserDetails(upref.getUserId(), upref.getFirstName(), upref.getKeyUserCode(), upref.getKeyEmpCode()));
        Call<Order_details> stringCall = apiService.orderdetails(input);
        startLoader(getString(R.string.loading), this);
        stringCall.enqueue(new Callback<Order_details>() {
            @Override
            public void onResponse(Call<Order_details> call, Response<Order_details> response) {
                stopLoader();
                if (response.body()!=null && response.body().getStatus().getOutResult()) {
                    details = response.body();
                    initview();
                } else {
                    ShowToast("Error :" + response.body().getStatus().getOutMessage(), OrderDetailActivity.this);
                }

            }

            @Override
            public void onFailure(Call<Order_details> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), OrderDetailActivity.this);
            }
        });

    }

    private void initview() {
        OrderDetail mOrderDetails = details.getOrderDetail().get(0);
        ordernotxt.setText(mOrderDetails.getOrderNumber());
        orderdate.setText(mOrderDetails.getOrderDate());
        sorce.setText(mOrderDetails.getOrderSource());
        purchaseno.setText(mOrderDetails.getObohPartnerOrderNo());
        remarks.setText(mOrderDetails.getRemarks());
        amount.setText(String.valueOf(mOrderDetails.getOrderAmount()) + " " + mOrderDetails.getCurrencyCode());
        totamount.setText(String.valueOf(mOrderDetails.getTotalAmount()) + " " + mOrderDetails.getCurrencyCode());
        comments.setText(mOrderDetails.getComments());
        cust_name.setText(mOrderDetails.getCustomerFullName());
        cust_mob.setText(mOrderDetails.getCustomerMobile());
        shipping_address.setText(mOrderDetails.getShippingAddress1() + " " + mOrderDetails.getShippingRegion()
                + " " + mOrderDetails.getShippingCity() + " " + mOrderDetails.getShippingCountry());
        orderDetailsAdaptor = new OrderDetailsAdaptor(OrderDetailActivity.this, mOrderDetails.getOrderDetails());
        paymentDetailsAdaptor = new PaymentDetailsAdaptor(OrderDetailActivity.this, mOrderDetails.getPaymentDetails());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        orderitems.setLayoutManager(mLayoutManager);
        orderitems.setItemAnimator(new DefaultItemAnimator());
        orderitems.setAdapter(orderDetailsAdaptor);

        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(getApplicationContext());
        paymentdetails.setLayoutManager(mLayoutManager1);
        paymentdetails.setItemAnimator(new DefaultItemAnimator());
        paymentdetails.setAdapter(paymentDetailsAdaptor);

        if (order_status.equalsIgnoreCase(constants.ORDER_CREATED)) {
            pick.setVisibility(View.VISIBLE);
            deliver.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);
            assign_biker.setVisibility(View.GONE);
            assign_courier.setVisibility(View.GONE);
            reshedule.setVisibility(View.GONE);
            invoice.setVisibility(View.GONE);

        } else if (order_status.equalsIgnoreCase(constants.ORDER_PICKED)) {
            pick.setVisibility(View.INVISIBLE);
            deliver.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            reshedule.setVisibility(View.VISIBLE);
            assign_biker.setVisibility(View.GONE);
            assign_courier.setVisibility(View.GONE);

        } else if (order_status.equalsIgnoreCase(constants.order_rtd)) {
            pick.setVisibility(View.INVISIBLE);
            deliver.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);
            assign_biker.setVisibility(View.VISIBLE);
            assign_courier.setVisibility(View.VISIBLE);
            reshedule.setVisibility(View.GONE);
        }  else {
            pick.setVisibility(View.GONE);
            deliver.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            assign_biker.setVisibility(View.GONE);
            assign_courier.setVisibility(View.GONE);
            reshedule.setVisibility(View.GONE);
            invoice.setVisibility(View.GONE);
            reshedule.setVisibility(View.GONE);
        }
        main.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void ondeliver() {
        Intent intent = new Intent(OrderDetailActivity.this, OrderDeliveryActivity.class);
        intent.putExtra(constants.OABOID, OABOID);
        intent.putExtra(constants.OADBID, OADBID);
        intent.putExtra(constants.ORDERNO, details.getOrderDetail().get(0).getOrderNumber());
        intent.putExtra("order_amount", String.valueOf(details.getOrderDetail().get(0).getTotalAmount()) + " " +
                details.getOrderDetail().get(0).getCurrencyCode());
        intent.putExtra("cust_name", details.getOrderDetail().get(0).getCustomerFullName());
        startActivityForResult(intent, req_code);
    }

    public void oncancel() {
        DeliveryRequest req = new DeliveryRequest();
        BikerRequest bike = new BikerRequest();
        req.setAction("CANCEL");
        bike.setOaboId(String.valueOf(OABOID));
        bike.setOadbId(String.valueOf(OADBID));
        bike.setOaboStatus(constants.ORDER_CANCELLED);
        bike.setOaboComments("ORDER CANCELLED.");
        bike.setUserDetails(new UserDetails(upref.getUserId(), upref.getFirstName()));
        req.setBikerRequest(bike);
        apicall(req, "Order cancelled.");

    }

    public void onpick() {
        DeliveryRequest req = new DeliveryRequest();
        BikerRequest bike = new BikerRequest();
        req.setAction("PICK");
        bike.setOaboId(String.valueOf(OABOID));
        bike.setOadbId(String.valueOf(OADBID));
        bike.setOaboStatus(constants.ORDER_PICKED);
        bike.setOaboComments("ORDER PICKED.");
        bike.setOaboSignFileType("");
        bike.setOaboSignFileName("");
        bike.setOaboSignature("");
        bike.setUserDetails(new UserDetails(upref.getUserId(), upref.getFirstName()));
        req.setBikerRequest(bike);
        apicall(req, "Order Picked.");

    }

    private void apicall(DeliveryRequest req, final String msg) {

        APIInterface apiService = new APIClient(this).getClient().create(APIInterface.class);
        Call<CommonApiResponse> stringCall = apiService.deliverorder(req);
        startLoader(getString(R.string.loading), this);
        stringCall.enqueue(new Callback<CommonApiResponse>() {
            @Override
            public void onResponse(Call<CommonApiResponse> call, Response<CommonApiResponse> response) {
                stopLoader();
                if (response.body().getStatus().getOutResult()) {
                    ShowToast(msg, OrderDetailActivity.this);
                    finish();
                } else {
                    ShowToast("Error :" + response.body().getStatus().getOutMessage(), OrderDetailActivity.this);
                }

            }

            @Override
            public void onFailure(Call<CommonApiResponse> call, Throwable t) {
                stopLoader();
                ShowToast(getString(R.string.api_fail), OrderDetailActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == req_code && resultCode == RESULT_OK) {

                String status = data.getStringExtra("order_status");
                if (status.equalsIgnoreCase("delivered"))
                    finish();
            }
        } catch (Exception ex) {

        }
    }
}