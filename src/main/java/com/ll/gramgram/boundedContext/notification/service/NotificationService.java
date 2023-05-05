package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        return notificationRepository.findByToInstaMember(toInstaMember);
    }

    @Transactional
    public void saveLikeNotification(LikeablePerson likeablePerson){
        Notification notification = makeNotification(likeablePerson, "Like", 0);

        notificationRepository.save(notification);
    }

    @Transactional
    public void saveModifyNotification(LikeablePerson likeablePerson, int oldAttractiveTypeCode){
        Notification notification = makeNotification(likeablePerson, "ModifyAttractiveType", oldAttractiveTypeCode);

        notificationRepository.save(notification);
    }

    private Notification makeNotification(LikeablePerson likeablePerson, String typeCode, int oldAttractiveTypeCode){
        Notification notification = Notification
                .builder()
                .readDate(null)
                .toInstaMember(likeablePerson.getToInstaMember())
                .fromInstaMember(likeablePerson.getFromInstaMember())
                .typeCode(typeCode)
                .oldGender(null)
                .oldAttractiveTypeCode(oldAttractiveTypeCode)
                .newGender(likeablePerson.getFromInstaMember().getGender())
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .build();

        return notification;
    }

    @Transactional
    public void updateReadDates(List<Notification> notifications){
        LocalDateTime now = LocalDateTime.now();

        RsData rsData;
        for(Notification notification:notifications)
            rsData = notification.updateReadDate(now);
    }
}
