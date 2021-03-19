package ru.cft.team.models;

public class ExchangeRate implements Comparable<ExchangeRate>{
    private String id;
    private String charCode;
    private int nominal;
    private String name;
    private double value;
    private Integer repeatIndex;

    public ExchangeRate(String id, String charCode, int nominal, String name, double value) {
        this.id = id;
        this.charCode = charCode;
        this.nominal = nominal;
        this.name = name;
        this.value = value;
        this.repeatIndex = 0;
    }

    public String getId() {
        return id;
    }

    public String getCharCode() {
        return charCode;
    }

    public int getNominal() {
        return nominal;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public Integer getRepeatIndex() { return repeatIndex; }

    public void setId(String id) {
        this.id = id;
    }

    public void setCharCode(String charCode) {
        this.charCode = charCode;
    }

    public void setNominal(int nominal) {
        this.nominal = nominal;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setRepeatIndex(int repeatIndex) { this.repeatIndex = repeatIndex; }


    @Override
    public String toString() {
        return charCode + " " + nominal + " " + name + " - " + value ;
    }

    //метод конвертации суммы в рублях в валюту
    public double getNumbersForAmount (int anount){
        double oneRate  = getValue()/getNominal();
        return anount/oneRate;
    }

    @Override
    public int compareTo(ExchangeRate o) {
        return -getRepeatIndex().compareTo(o.getRepeatIndex());
    }
}
