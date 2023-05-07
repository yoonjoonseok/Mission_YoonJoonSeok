package com.ll.gramgram.boundedContext.notification.entity;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Notification extends BaseEntity {

    private LocalDateTime readDate;
    @ManyToOne
    @ToString.Exclude
    private InstaMember toInstaMember; // 메세지 받는 사람(호감 받는 사람)
    @ManyToOne
    @ToString.Exclude
    private InstaMember fromInstaMember; // 메세지를 발생시킨 행위를 한 사람(호감표시한 사람)
    private String typeCode; // 호감표시=Like, 호감사유변경=ModifyAttractiveType
    private String oldGender; // 해당사항 없으면 null
    private int oldAttractiveTypeCode; // 해당사항 없으면 0
    private String newGender; // 해당사항 없으면 null
    private int newAttractiveTypeCode; // 해당사항 없으면 0

    public boolean isRead() {
        return readDate != null;
    }

    public void markAsRead() {
        readDate = LocalDateTime.now();
    }

    public String getModifyDateDisplay() {
        LocalDateTime modifyDate = getModifyDate();
        LocalDateTime now = LocalDateTime.now();

        int year = modifyDate.getYear() % 100;
        String month = plusZero(modifyDate.getMonth().getValue());
        String day = plusZero(modifyDate.getDayOfMonth());
        String hour = plusZero(modifyDate.getHour());
        String minute = plusZero(modifyDate.getMinute());
        String between = getBetween(modifyDate,now);

        String result = "%d.%s.%s %s:%s, %s".formatted(year,month,day,hour,minute,between);

        return result;
    }

    private String getBetween(LocalDateTime modifyDate, LocalDateTime now){
        String between;
        long betweenDay = ChronoUnit.DAYS.between(modifyDate,now);

        if (betweenDay != 0)
            between = betweenDay + "일 전";
        else {
            long betweenHour = ChronoUnit.HOURS.between(modifyDate, now);

            if (betweenHour != 0)
                between = betweenHour + "시간 전";
            else
                between = ChronoUnit.MINUTES.between(modifyDate, now) + "분 전";
        }

        return between;
    }

    private String plusZero(int number) {
        return number < 10 ? "0" + number :  ""+number;
    }

    public String getGenderDisplayName(String Gender) {
        return switch (Gender) {
            case "W" -> "여자";
            case "M" -> "남자";
            default -> "넌바이너리";
        };
    }

    public String getAttractiveTypeDisplayName(int attractiveTypeCode) {
        return switch (attractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }
}
