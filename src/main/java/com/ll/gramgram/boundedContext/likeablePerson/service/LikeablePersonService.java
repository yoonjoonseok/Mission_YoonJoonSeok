package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    @Transactional
    public RsData<LikeablePerson> likeablePersonUpdate(LikeablePerson likeablePerson, int attractiveTypeCode) {
        likeablePerson.setAttractiveTypeCode(attractiveTypeCode);
        likeablePersonRepository.save(likeablePerson); // 저장
        return RsData.of("S-2", "입력하신 인스타유저(%s)의 호감이유를 변경하였습니다.".formatted(likeablePerson.getToInstaMemberUsername()), likeablePerson);
    }
    /*
    public RsData canActorAdd(Member actor, String username, int attractiveTypeCode) {
        if (actor.hasConnectedInstaMember() == false)
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");

        if (actor.getInstaMember().getUsername().equals(username))
            return RsData.of("F-2", "본인을 호감상대로 등록할 수 없습니다.");

        InstaMember instaMember = instaMemberService.findByUsername(actor.getInstaMember().getUsername()).orElse(null);

        List<LikeablePerson> LikeablePersonList = instaMember.getFromLikeablePeople();

        Optional<LikeablePerson> likeablePerson = LikeablePersonList.stream()
                .filter(s -> s.getToInstaMemberUsername().equals(username)).findFirst();

        if(likeablePerson.isPresent()){
            if(likeablePerson.get().getAttractiveTypeCode() != attractiveTypeCode)
                return RsData.of("S-2", "변경 가능합니다.", likeablePerson.get());
            return RsData.of("F-4", "중복입니다.");
        }

        if (LikeablePersonList.size() >= 10)
            return RsData.of("F-3", "한 명이 11개 이상 등록할 수 없습니다.");

        return RsData.of("S-1", "추가 가능합니다.");
    }*/

    public RsData canActorAdd(Member actor, String username, int attractiveTypeCode) {
        if (actor.hasConnectedInstaMember() == false)
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");

        if (actor.getInstaMember().getUsername().equals(username))
            return RsData.of("F-2", "본인을 호감상대로 등록할 수 없습니다.");

        LikeablePerson likeablePerson = likeablePersonRepository.findByFromInstaMemberIdAndToInstaMember_username(actor.getInstaMember().getId(),username);

        if(likeablePerson != null){
            if(likeablePerson.getAttractiveTypeCode() != attractiveTypeCode)
                return RsData.of("S-2", "변경 가능합니다.", likeablePerson);
            return RsData.of("F-4", "중복입니다.");
        }

        Long size = likeablePersonRepository.countByFromInstaMemberId(actor.getInstaMember().getId());

        if (size >= 10)
            return RsData.of("F-3", "한 명이 11개 이상 등록할 수 없습니다.");

        return RsData.of("S-1", "추가 가능합니다.");
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {
        String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
        likeablePersonRepository.delete(likeablePerson);

        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(toInstaMemberUsername));
    }

    public RsData canActorDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "존재하지 않는 호감상태입니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId)
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제 가능합니다.");
    }
}
