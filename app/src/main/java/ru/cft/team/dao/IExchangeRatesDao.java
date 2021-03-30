package ru.cft.team.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ru.cft.team.models.ExchangeRate;

@Dao
public interface IExchangeRatesDao {

    @Query("SELECT * FROM `exchangeRates.db` ORDER BY repeatIndex DESC")
    List<ExchangeRate> getAllExchangeRates();

    @Insert
    void insertExchangeRate (ExchangeRate exchangeRate);

    @Update
    void updateExchangeRate (ExchangeRate exchangeRate);

}
