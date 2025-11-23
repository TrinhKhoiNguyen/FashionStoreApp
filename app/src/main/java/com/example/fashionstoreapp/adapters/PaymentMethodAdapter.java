package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.model.PaymentMethod;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentViewHolder> {

    private Context context;
    private List<PaymentMethod> paymentMethods;
    private OnPaymentActionListener listener;

    public interface OnPaymentActionListener {
        void onDelete(PaymentMethod payment);
    }

    public PaymentMethodAdapter(Context context, List<PaymentMethod> paymentMethods, OnPaymentActionListener listener) {
        this.context = context;
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_method, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentMethod payment = paymentMethods.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    class PaymentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvType, tvDefault;
        private ImageView btnDelete;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvPaymentType);
            tvDefault = itemView.findViewById(R.id.tvPaymentDefault);
            btnDelete = itemView.findViewById(R.id.btnDeletePayment);

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDelete(paymentMethods.get(position));
                }
            });
        }

        public void bind(PaymentMethod payment) {
            tvType.setText(payment.getType());
            tvDefault.setVisibility(payment.isDefault() ? View.VISIBLE : View.GONE);
        }
    }
}
