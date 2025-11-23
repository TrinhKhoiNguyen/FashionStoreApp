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
import com.example.fashionstoreapp.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<Address> addresses;
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onDelete(Address address);
    }

    public AddressAdapter(Context context, List<Address> addresses, OnAddressActionListener listener) {
        this.context = context;
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvPhone, tvAddress, tvDefault;
        private ImageView btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAddressName);
            tvPhone = itemView.findViewById(R.id.tvAddressPhone);
            tvAddress = itemView.findViewById(R.id.tvAddressDetail);
            tvDefault = itemView.findViewById(R.id.tvDefaultBadge);
            btnDelete = itemView.findViewById(R.id.btnDeleteAddress);

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDelete(addresses.get(position));
                }
            });
        }

        public void bind(Address address) {
            tvName.setText(address.getName());
            tvPhone.setText(address.getPhone());
            tvAddress.setText(address.getAddress() + ", " + address.getCity());
            tvDefault.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);
        }
    }
}
