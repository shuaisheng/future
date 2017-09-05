package com.kangyonggan.app.future.web.controller.mobile;

import com.kangyonggan.app.future.biz.service.MusicService;
import com.kangyonggan.app.future.model.constants.Resp;
import com.kangyonggan.app.future.model.dto.MusicsResponse;
import com.kangyonggan.app.future.model.vo.Music;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kangyonggan
 * @since 9/5/17
 */
@RestController
@RequestMapping("mobile/music")
@Log4j2
public class MobileMusicController {

    @Autowired
    private MusicService musicService;

    /**
     * 查询音乐列表
     *
     * @param pageNum
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public MusicsResponse list(@RequestParam(value = "p", required = false, defaultValue = "1") int pageNum) {
        MusicsResponse response = new MusicsResponse();

        try {
            List<Music> musics = musicService.findMusicsByPage(pageNum);

            response.setRespCo(Resp.SUCCESS.getRespCo());
            response.setRespMsg(Resp.SUCCESS.getRespMsg());
            response.setMusics(musics);
        } catch (Exception e) {
            log.warn("查询音乐列表异常", e);
            response.setRespCo(Resp.FAILURE.getRespCo());
            response.setRespMsg(Resp.FAILURE.getRespMsg());
        }

        return response;
    }

}
