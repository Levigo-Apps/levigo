package org.getcarebase.carebase.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.text.NumberFormat;

public class Cost {
    private String costDate;
    private int numberAdded;
    private double packagePrice;
    private double unitPrice;
    private String user;

    public Cost() {}

    public Cost(String costDate, int numberAdded, double packagePrice) {
        this.costDate = costDate;
        this.numberAdded = numberAdded;
        this.packagePrice = packagePrice;
        this.unitPrice = packagePrice / (double) numberAdded;
    }

    @PropertyName("cost_date")
    public void setCostDate(String costDate) {
        this.costDate = costDate;
    }

    @PropertyName("number_added")
    public void setNumberAdded(String numberAdded) {
        this.numberAdded = Integer.parseInt(numberAdded);
    }

    @PropertyName("package_price")
    public void setPackagePrice(double packagePrice) {
        this.packagePrice = packagePrice;
    }

    @PropertyName("unit_price")
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    @PropertyName("user")
    public void setUser(String user) {
        this.user = user;
    }

    @PropertyName("cost_date")
    public String getCostDate() {
        return costDate;
    }

    @PropertyName("number_added")
    public String getStringNumberAdded() {
        return Integer.toString(numberAdded);
    }

    @Exclude
    public int getNumberAdded() {
        return numberAdded;
    }

    @PropertyName("package_price")
    public double getPackagePrice() {
        return packagePrice;
    }

    @Exclude
    public String getStringPackagePrice() {
        return NumberFormat.getCurrencyInstance().format(packagePrice);
    }

    @PropertyName("unit_price")
    public double getUnitPrice() {
        return unitPrice;
    }

    @Exclude
    public String getStringUnitPrice() {
        return NumberFormat.getCurrencyInstance().format(unitPrice);
    }

    @PropertyName("user")
    public String getUser() {
        return user;
    }
}
