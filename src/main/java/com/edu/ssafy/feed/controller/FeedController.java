package com.edu.ssafy.feed.controller;

import com.edu.ssafy.feed.model.dto.common.CommonRes;
import com.edu.ssafy.feed.model.dto.common.ListRes;
import com.edu.ssafy.feed.model.dto.common.SingleRes;
import com.edu.ssafy.feed.model.dto.request.FeedEditReq;
import com.edu.ssafy.feed.model.dto.request.FeedWriteReq;
import com.edu.ssafy.feed.model.dto.response.SingleFeedRes;
import com.edu.ssafy.feed.model.dto.response.UserFeedsRes;
import com.edu.ssafy.feed.model.entity.Feed;
import com.edu.ssafy.feed.model.entity.User;
import com.edu.ssafy.feed.model.service.FeedService;
import com.edu.ssafy.feed.model.service.S3UploaderService;
import com.edu.ssafy.feed.model.service.TaggedFriendsService;
import com.edu.ssafy.feed.model.service.UserService;
import com.edu.ssafy.feed.util.NicknameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final UserService userService;
    private final FeedService feedService;
    private final TaggedFriendsService taggedFriendsService;
    private final S3UploaderService s3UploaderService;

    @GetMapping("/test/header")
    public String test(@RequestHeader Map<String, String> map) {

        System.out.println("=================================================================");
        map.forEach((key, value) -> {
            System.out.println(key + " : " + value);
        });
        System.out.println("=================================================================");

        return "test";
    }

    @GetMapping("/test/nickname")
    public String test(@RequestHeader("x-nickname") String encodedNickname) {

        return NicknameUtil.decodeNickname(encodedNickname);
    }

    //피드 작성
    @PostMapping("/write")
    public CommonRes writeFeed(@RequestHeader("x-nickname") String encodedNickname, @RequestBody FeedWriteReq req) {
        String nickname = NicknameUtil.decodeNickname(encodedNickname);
        // 유저 정보 가져오기
        User user = userService.findByNickname(nickname);

//         FeedWriteReq를 Feed 엔티티로 변환
        Feed feed = req.toFeedEntity(user);

//         변환된 엔티티를 저장
        feedService.saveFeed(feed);

        taggedFriendsService.saveAllTaggedFriends(req, feed);

        return new CommonRes(201, "피드 작성 성공");
    }

    // 내 피드 전체 조회
    @GetMapping("/feeds/mine")
    public ResponseEntity<ListRes<UserFeedsRes>> getMyAllFeed(@RequestHeader("x-nickname") String encodedNickname) {
        String nickname = NicknameUtil.decodeNickname(encodedNickname);
        User user = userService.findByNickname(nickname);

        List<Feed> allFeeds = feedService.findAllByUserId(user.getId());

        List<UserFeedsRes> allUserFeedsRes = feedService.changeAllFeedsToDto(allFeeds);
        return new ResponseEntity<>(new ListRes<>(200, "피드 조회 성공", allUserFeedsRes), HttpStatus.OK);
    }

    // 특정 유저 피드 전체 조회
    @GetMapping("/feeds/nickname/{targetNickname}")
    public ResponseEntity<ListRes<UserFeedsRes>> getUserAllFeed(@RequestHeader("x-nickname") String encodedNickname, @PathVariable String targetNickname) {
        User targetUser = userService.findByNickname(targetNickname);
        List<Feed> allFeeds = feedService.findAllByUserId(targetUser.getId());

        List<UserFeedsRes> allUserFeedsRes = feedService.changeAllFeedsToDto(allFeeds);
        ListRes<UserFeedsRes> listRes = new ListRes<>(200, "피드 조회 성공", allUserFeedsRes);
        return new ResponseEntity<>(listRes, HttpStatus.OK);
    }

    // 피드 상세 조회
    @GetMapping("/{feedId}")
    public SingleRes<SingleFeedRes> getFeedDetail(@RequestHeader("x-nickname") String encodedNickname, @PathVariable Long feedId) {
        String nickname = NicknameUtil.decodeNickname(encodedNickname);
        SingleFeedRes oneByFeedId = feedService.findOneByFeedId(feedId, nickname);
        return new SingleRes<>(200, "피드 상세 조회 성공", oneByFeedId);
    }

    // 피드 수정
    @PutMapping("/edit/{feedId}")
    public CommonRes editFeed(@PathVariable Long feedId, @RequestHeader("x-nickname") String encodedNickname, @RequestBody FeedEditReq req) {
        feedService.editFeed(feedId, req);
        return new CommonRes(200, "피드 수정 성공");
    }

    // 피드 좋아요 / 좋아요 취소
    @PostMapping("/like/{feedId}")
    public ResponseEntity<?> likeFeed(@PathVariable Long feedId, @RequestHeader("x-nickname") String encodedNickname) {
        String nickname = NicknameUtil.decodeNickname(encodedNickname);
        String msg = feedService.likeFeed(feedId, nickname);
        return new ResponseEntity<>(new CommonRes(200, msg), HttpStatus.OK);
    }

    // 이미지 테스트 api
    @PostMapping("/opened/image")
    public List<String> testImage(@RequestPart(value = "images") List<MultipartFile> images) {

        if (images.isEmpty()) {
            return new ArrayList<>();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5); // 스레드 풀 생성
        List<Future<String>> futures = new ArrayList<>();

        for (MultipartFile image : images) {
            // 이미지 업로드 작업을 스레드 풀에 제출
            Future<String> future = executorService.submit(() -> s3UploaderService.upload(image, "images"));
            futures.add(future);
        }

        List<String> storedFileNames = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                storedFileNames.add(future.get()); // 업로드된 이미지의 URL을 가져옴
            } catch (Exception e) {
                throw new RuntimeException("이미지 업로드 중 에러 발생", e);
            }
        }

        executorService.shutdown(); // 스레드 풀 종료

        return storedFileNames;

    }
}