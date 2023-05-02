package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        return notificationRepository.findByToInstaMember(toInstaMember);
    }

    public void saveLikeNotification(LikeablePerson likeablePerson){
        Notification notification = Notification
                .builder()
                .readDate(null)
                .toInstaMember(likeablePerson.getToInstaMember())
                .fromInstaMember(likeablePerson.getFromInstaMember())
                .typeCode("Like")
                .oldGender(null)
                .oldAttractiveTypeCode(0)
                .newGender(likeablePerson.getFromInstaMember().getGender())
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .build();

        notificationRepository.save(notification);
    }

    public void saveModifyNotification(LikeablePerson likeablePerson, int oldAttractiveTypeCode){
        Notification notification = Notification
                .builder()
                .readDate(null)
                .toInstaMember(likeablePerson.getToInstaMember())
                .fromInstaMember(likeablePerson.getFromInstaMember())
                .typeCode("ModifyAttractiveType")
                .oldGender(null)
                .oldAttractiveTypeCode(oldAttractiveTypeCode)
                .newGender(likeablePerson.getFromInstaMember().getGender())
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .build();

        notificationRepository.save(notification);
    }
}
