package com.example.backend.service;


import com.example.backend.domain.dto.Message;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.embedded.UserInfo;
import com.example.backend.domain.video.Video;
import com.example.backend.domain.video.dto.ResponseVideoInfo;
import com.example.backend.domain.video.dto.VideoFilterRequest;
import com.example.backend.domain.video.dto.VideoUploadDto;
import com.example.backend.domain.video.enums.VideoType;
import com.example.backend.exception.ReturnCode;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VideoCategoryRepository;
import com.example.backend.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final VideoCategoryRepository videoCategoryRepository;
    private final UserRepository userRepository;

    public ResponseVideoInfo makeResVideoInfo(Video v) {
        //List<ResponseVideoInfo> responseVideoInfoList = new ArrayList<>();
        ResponseVideoInfo info = ResponseVideoInfo.builder()
                .idx(v.getIdx())
                .content(v.getContent())
                .title(v.getTitle())
                .videoType(v.getVideoType())
                .videoUrl(v.getVideoUrl())
                .thumbnailUrl(v.getThumbnailUrl())
                .hits(v.getHits())
                .createdDate(v.getCreatedDate())
                .updatedDate(v.getUpdatedDate())
                .nickname(v.getUploader().getNickname())
                .profileImgUrl(v.getUploader().getProfile().getProfileImg()) // 임시 -> user entity 수정? profile 수정?
                .build();
        return info;
    }

    @Transactional
    public List<ResponseVideoInfo> getAllVideo(){
        List<Video> videoList = videoRepository.findAll(); // 전체 비디오 목록
        List<ResponseVideoInfo> dtoList = new ArrayList<>();
        for(Video v: videoList){
            dtoList.add(makeResVideoInfo(v));
        }
        return dtoList;
    }

    @Transactional
    public List<ResponseVideoInfo> filteringVideo(VideoFilterRequest request) {
        // return type

        // 1. 카테고리 필터링

        // 1.1 카테고리 선택 항목 null 제외 선택
        Collection<String> categories = new ArrayList<>();
        if(request.getCategory1() != null) categories.add(request.getCategory1());
        if(request.getCategory2() != null) categories.add(request.getCategory2());
        if(request.getCategory3() != null) categories.add(request.getCategory3());

        Optional<List<Video>> filteredByCategory;
        // 1.1 카테고리 선택하지 않았을 경우 전체 video
        filteredByCategory = Optional.of(videoRepository.findAll());
        // 1.2. 카테고리 선택했을 경우 video-category entity 에서 찾기
        if(!categories.isEmpty()) filteredByCategory = videoCategoryRepository.findDistinctVideoInCategories(categories);



        // 2. 성별 / 시 / 구 필터링

        // 2.1 카테고리 필터링 후 비디오 존재하는 경우
        if(filteredByCategory.isPresent()) {
            List<Video> filteredCom = new ArrayList<>();
            // 3. 성별 / 도시 / 구 존재 경우 무조건 필터링
            for(Video v: filteredByCategory.get()) {
                UserInfo info = v.getUploader().getUserInfo();
                // gender null 이거나 gender 일치 시 위치 비교 가능
                if(request.getGender() != null && !request.getGender().equals(info.getGenderType().getGender())) continue;
                //
                if(request.getCity() != null && !request.getCity().equals(info.getCity())) continue; // 시 존재 && 시 다름
                if(request.getCity() != null && request.getCity().equals(info.getCity())) // 시 동일
                {
                    if(request.getDistrict() != null && !request.getDistrict().equals(info.getDistrict())) continue; // 구 존재 && 구 다륾
                } // 시가 다르면 구는 필터링 항목으로 들어가지 않는다.
                filteredCom.add(v);

            }
            List<ResponseVideoInfo> dtoList = new ArrayList<>();
            for(Video v: filteredCom){
                dtoList.add(makeResVideoInfo(v));
            }
            return dtoList;
        }
        // 2.2 비디오 존재하지 않는 경우
        else{
            return null;
        }


    }

    @Transactional
    public Message saveVideo(VideoUploadDto uploadDto) {
        // uploader 찾기
        Optional<User> user = userRepository.findByEmail(uploadDto.getEmail());

        // uploader 존재하지 않을 경우
        if(!user.isPresent()) return new Message(ReturnCode.USER_NOT_FOUND,null);

        // 비디오 생성
        Video video = Video.builder()
                .title(uploadDto.getTitle())
                .content(uploadDto.getContent())
                .hits(0L)
                .thumbnailUrl(uploadDto.getThumbUrl())
                .videoUrl(uploadDto.getVideoUrl())
                .uploader(user.get())
                .videoType(VideoType.valueOf(uploadDto.getVideoType()))
        .build();

        videoRepository.save(video);

        return new Message(ReturnCode.SUCCESS,makeResVideoInfo(video));
    }
}
