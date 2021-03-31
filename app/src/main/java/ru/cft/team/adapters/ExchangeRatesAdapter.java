package ru.cft.team.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.cft.team.R;
import ru.cft.team.models.ExchangeRate;

public class ExchangeRatesAdapter extends  RecyclerView.Adapter<ExchangeRatesAdapter.ExchangeRatesViewHolder>{

    private List<ExchangeRate> exchangeRates;
    private OnExchangeRateClickListener onExchangeRateClickListener;

    public ExchangeRatesAdapter(List<ExchangeRate> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public interface OnExchangeRateClickListener{
        void onExchangeRateClick (int position);
    }

    public void setOnExchangeRateClickListener(OnExchangeRateClickListener onExchangeRateClickListener) {
        this.onExchangeRateClickListener = onExchangeRateClickListener;
    }

    @NonNull
    @Override
    public ExchangeRatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exchange_rate_item, parent, false);
        return new ExchangeRatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExchangeRatesViewHolder holder, int position) {
        ExchangeRate exchangeRate = exchangeRates.get(position);
        holder.textViewCharCode.setText(exchangeRate.getCharCode());
        holder.textViewNominalNameValue.setText(exchangeRate.getNominal() + " "
                + exchangeRate.getName() + " - " + exchangeRate.getValue());
    }

    @Override
    public int getItemCount() {
        return exchangeRates.size();
    }

    class ExchangeRatesViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewCharCode;
        private TextView textViewNominalNameValue;

        public ExchangeRatesViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCharCode = itemView.findViewById(R.id.textViewCharCode);
            textViewNominalNameValue = itemView.findViewById(R.id.textViewNominalNameValue);
            //Добавляем слушатель на нажатие элемента
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onExchangeRateClickListener!=null){
                        onExchangeRateClickListener.onExchangeRateClick(getAdapterPosition());
                    }
                }
            });

        }
    }
}
