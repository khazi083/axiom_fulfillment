package com.axiom.fulfillment.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.axiom.fulfillment.R;
import com.axiom.fulfillment.helper.OrderActionListner;
import com.axiom.fulfillment.model.DispatchOrderitem;

import java.util.List;

public class DispatchOrderAdaptor extends RecyclerView.Adapter<DispatchOrderAdaptor.MyViewHolder> {

    private List<DispatchOrderitem> dispatchOrderitems;
    OrderActionListner listner;
    Context ctx;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView orderno, cust_name,purchaseno,orderdate,assignon,msource;
        RelativeLayout mainlayout;
        ImageView deliveryicon;

        public MyViewHolder(View view) {
            super(view);
            orderno = view.findViewById(R.id.orderno);
            cust_name = view.findViewById(R.id.cust_name);
            purchaseno=view.findViewById(R.id.purchaseno);
            orderdate=view.findViewById(R.id.orderdate);
            assignon=view.findViewById(R.id.assigneddate);
            msource=view.findViewById(R.id.source);
            mainlayout=view.findViewById(R.id.mainlayout);
            deliveryicon=view.findViewById(R.id.deliveryicon);
        }
    }

    public DispatchOrderAdaptor(Context ctx, List<DispatchOrderitem> dispatchOrderitems, OrderActionListner deliverylistner) {
        this.dispatchOrderitems = dispatchOrderitems;
        listner=deliverylistner;
        this.ctx=ctx;
    }

    @Override
    public DispatchOrderAdaptor.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dispatch_item, parent, false);

        return new DispatchOrderAdaptor.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DispatchOrderAdaptor.MyViewHolder holder, final int position) {
        DispatchOrderitem or = dispatchOrderitems.get(position);
        holder.orderno.setText(or.getObohOrderNo());
        holder.cust_name.setText(or.getObohCustFullName());
        holder.purchaseno.setText(String.valueOf(or.getObohOrderAmountTax())+" "+or.getObohCurrency());
        holder.orderdate.setText(or.getObohOrderDate().replace("T"," "));
        holder.assignon.setText(or.getObohPartnerOrderNo());
        holder.msource.setText(or.getObohOrderSource());

        if(or.getObohOrderSource().contains("HUAWEI"))
            holder.deliveryicon.setImageResource(R.drawable.huaweiksa);
        else if(or.getObohOrderSource().contains("NOON"))
            holder.deliveryicon.setImageResource(R.drawable.noon);
        else if(or.getObohOrderSource().contains("SWITCH"))
            holder.deliveryicon.setImageResource(R.drawable.aeswitch1);
        else if(or.getObohOrderSource().contains("HONOR"))
            holder.deliveryicon.setImageResource(R.drawable.honorksa);
        else if(or.getObohOrderSource().contains("HTCVIVE"))
            holder.deliveryicon.setImageResource(R.drawable.htcviveuae);
        else if(or.getObohOrderSource().contains("HTC"))
            holder.deliveryicon.setImageResource(R.drawable.htcuae);
        else if(or.getObohOrderSource().contains("WADI"))
            holder.deliveryicon.setImageResource(R.drawable.wadiksa);
        holder.mainlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.ondeliver(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dispatchOrderitems.size();
    }
}
