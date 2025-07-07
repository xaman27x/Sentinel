package com.moderation.sentinel.api.dto.response;

public class DailyUsageDto {
    private String date;
    private long count;
    private long offensiveCount;

    public DailyUsageDto() {
    }

    public DailyUsageDto(String date, long count, long offensiveCount) {
        this.date = date;
        this.count = count;
        this.offensiveCount = offensiveCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getOffensiveCount() {
        return offensiveCount;
    }

    public void setOffensiveCount(long offensiveCount) {
        this.offensiveCount = offensiveCount;
    }
}