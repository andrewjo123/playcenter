package com.playground.task;

import com.playground.entity.*;
import com.playground.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ImgCheckTask {

    private final ItemImgRepository imgRepository;

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    @Scheduled(cron = "0 30 8 * * THU")
    @Transactional
    public void removeUnuseImage() throws Exception { // 매주 목요일 08:30에 db에 없는 이미지 파일 삭제

        System.out.println("--------------------Checking not use Image...");
        List<ItemImg> imgList=imgRepository.getImgList();
        List<Path> imgPathList=imgList.stream().map(i-> Paths.get(itemImgLocation+ File.separator +i.getImgName())).toList();

        System.out.println("--------------------Delete not use Image...");
        File[] removeFiles = new File(itemImgLocation).listFiles(file -> !imgPathList.contains(file.toPath()));
        for (File file : Objects.requireNonNull(removeFiles)) {
            if(file.delete()){
                System.out.println("Deleted file: " + file.getAbsolutePath());
            }
        }

        System.out.println("------------------Complete to delete not use Image");
    }
}
