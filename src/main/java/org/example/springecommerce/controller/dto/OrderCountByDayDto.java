package org.example.springecommerce.controller.dto;

public class OrderCountByDayDto {
    private int day;
    private long count;

    public OrderCountByDayDto(int day, long count) {
        this.day = day;
        this.count = count;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
