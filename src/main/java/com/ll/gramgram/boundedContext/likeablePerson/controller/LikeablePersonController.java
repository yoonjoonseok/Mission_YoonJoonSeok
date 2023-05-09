package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/usr/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/like")
    public String showLike() {
        return "usr/likeablePerson/like";
    }

    @AllArgsConstructor
    @Getter
    public static class LikeForm {
        @NotBlank
        @Size(min = 3, max = 30)
        private final String username;
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like")
    public String like(@Valid LikeForm likeForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.like(rq.getMember(), likeForm.getUsername(), likeForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String showList(Model model) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            // 해당 인스타회원이 좋아하는 사람들 목록
            List<LikeablePerson> likeablePeople = instaMember.getFromLikeablePeople();
            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/list";
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public String cancel(@PathVariable Long id) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElse(null);

        RsData canDeleteRsData = likeablePersonService.canCancel(rq.getMember(), likeablePerson);

        if (canDeleteRsData.isFail()) return rq.historyBack(canDeleteRsData);

        RsData deleteRsData = likeablePersonService.cancel(likeablePerson);

        if (deleteRsData.isFail()) return rq.historyBack(deleteRsData);

        return rq.redirectWithMsg("/usr/likeablePerson/list", deleteRsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String showModify(@PathVariable Long id, Model model) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElseThrow();

        RsData canModifyRsData = likeablePersonService.canModify(rq.getMember(), likeablePerson);

        if (canModifyRsData.isFail()) return rq.historyBack(canModifyRsData);

        model.addAttribute("likeablePerson", likeablePerson);

        return "usr/likeablePerson/modify";
    }

    @AllArgsConstructor
    @Getter
    public static class ModifyForm {
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@PathVariable Long id, @Valid ModifyForm modifyForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.modifyAttractive(rq.getMember(), id, modifyForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/toList")
    public String showToList(Model model, @RequestParam(value = "gender", required = false, defaultValue = "") String gender, @RequestParam(value = "attractiveTypeCode", required = false, defaultValue = "0") int attractiveTypeCode, @RequestParam(value = "sortCode", required = false, defaultValue = "1") int sortCode) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            // 해당 인스타회원이 좋아하는 사람들 목록
            List<LikeablePerson> likeablePeople = instaMember.getToLikeablePeople();

            likeablePeople = likeablePeople
                    .stream()
                    .filter(l -> gender.isEmpty() || l.getFromInstaMember().getGender().equals(gender)) //성별 필터링
                    .filter(l -> attractiveTypeCode == 0 || l.getAttractiveTypeCode() == attractiveTypeCode) //호감사유 필터링
                    .collect(Collectors.toList());


            switch (sortCode) {
                //날짜순
                case 2: {
                    Collections.reverse(likeablePeople);
                    break;
                }
                //인기 많은 순
                case 3: {
                    int[] countAttractiveTypeCode = new int[3];
                    likeablePeople.stream().forEach(l -> countAttractiveTypeCode[l.getAttractiveTypeCode()-1]++);
                    Collections.sort(likeablePeople, Comparator.comparingInt(l -> countAttractiveTypeCode[l.getAttractiveTypeCode()-1]*-1));
                    break;
                }
                //인기 적은 순
                case 4: {
                    int[] countAttractiveTypeCode = new int[3];
                    likeablePeople.stream().forEach(l -> countAttractiveTypeCode[l.getAttractiveTypeCode()-1]++);
                    Collections.sort(likeablePeople, Comparator.comparingInt(l -> countAttractiveTypeCode[l.getAttractiveTypeCode()-1]));
                    break;
                }
                //성별순
                case 5: {
                    Collections.sort(likeablePeople, Comparator.comparingInt(l -> l.getFromInstaMember().getGenderInt()));
                    break;
                }
                //호감사유순
                case 6: {
                    Collections.sort(likeablePeople, Comparator.comparingInt(l -> l.getAttractiveTypeCode()));
                    break;
                }
                //최신순(기본)
                default:
                    break;
            }

            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/toList";
    }
}
