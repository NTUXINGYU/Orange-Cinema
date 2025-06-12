package com.orange.moviebackend.controller;

import com.orange.moviebackend.common.constant.MovieRankingList;
import com.orange.moviebackend.common.response.R;
import com.orange.moviebackend.domain.SysMovie;
import com.orange.moviebackend.domain.vo.SysMovieVo;
import com.orange.moviebackend.service.SysMovieService;

import org.slf4j.Logger; // 引入SLF4J Logger
import org.slf4j.LoggerFactory; // 引入SLF4J LoggerFactory

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException; // 引入 ResourceAccessException
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@RestController
public class SysMovieController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(SysMovieController.class);

    @Resource
    private SysMovieService sysMovieService;

    private final String TMDB_API_KEY = "8b73b728cbe453208e00e3b81fceac34";
    private final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private final RestTemplate restTemplate = new RestTemplate();


    @GetMapping("/sysMovie/find")
    public R findAllMovies(SysMovieVo sysMovieVo) {
        startPage();
        List<SysMovie> data = sysMovieService.findAllMovies(sysMovieVo);
        return getResult(data);
    }

    @GetMapping("/sysMovie/find/{id}")
    public R findMovieById(@PathVariable Long id) {
        return getResult(sysMovieService.findMovieById(id));
    }

    @PostMapping("/sysMovie")
    public R addMovie(@Validated @RequestBody SysMovie sysMovie) {
        return getResult(sysMovieService.addMovie(sysMovie));
    }

    @PutMapping("/sysMovie")
    public R updateMovie(@Validated @RequestBody SysMovie sysMovie) {
        return getResult(sysMovieService.updateMovie(sysMovie));
    }

    @DeleteMapping("/sysMovie/{ids}")
    public R deleteMovie(@PathVariable Long[] ids) {
        return getResult(sysMovieService.deleteMovie(ids));
    }

    @GetMapping("/sysMovie/find/rankingList/{listId}")
    public R findRankingList(@PathVariable Integer listId) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        if (listId <= 0 || listId > 3) {
            return R.error("Parameter error");
        }
        Method getList = sysMovieService.getClass().getMethod(MovieRankingList.listNames[listId - 1]);
        startPage();
        List<SysMovie> data = (List<SysMovie>) getList.invoke(sysMovieService);
        return getResult(data);
    }


    @GetMapping("/tmdb/movie/{id}")
    public ResponseEntity<String> getMovieDetails(@PathVariable String id) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(TMDB_BASE_URL + "/movie/" + id)
                .queryParam("api_key", TMDB_API_KEY);

        String urlToTmdb = builder.toUriString();
        log.info("Requesting TMDB URL for movie details: {}", urlToTmdb);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlToTmdb, String.class);
            log.info("Successfully received response from TMDB for movie details [ID: {}], Status: {}", id, responseEntity.getStatusCode());
            return responseEntity;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("TMDB Error (HttpClientErrorException or HttpServerErrorException) when fetching movie details for ID [{}]. Status: {}. Response Body: {}",
                    id, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Network Error (ResourceAccessException) when fetching movie details for ID [{}] from TMDB: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT) // 或者 SERVICE_UNAVAILABLE
                    .body("{\"error_code\":\"TMDB_CONNECTION_ERROR\", \"message\":\"Network error or timeout connecting to TMDB service for movie details.\"}");
        } catch (Exception e) {
            log.error("Unexpected generic error in getMovieDetails proxy for ID [{}]: {}", id, e.getMessage(), e); // 打印完整异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error_code\":\"INTERNAL_PROXY_ERROR\", \"message\":\"An internal error occurred in the backend proxy while fetching movie details: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/tmdb/movie/{id}/credits")
    public ResponseEntity<String> getMovieCredits(@PathVariable String id) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(TMDB_BASE_URL + "/movie/" + id + "/credits")
                .queryParam("api_key", TMDB_API_KEY);
        String urlToTmdb = builder.toUriString();
        log.info("Requesting TMDB URL for movie credits: {}", urlToTmdb);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlToTmdb, String.class);
            log.info("Successfully received response from TMDB for movie credits [ID: {}], Status: {}", id, responseEntity.getStatusCode());
            return responseEntity;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("TMDB Error (HttpClientErrorException or HttpServerErrorException) when fetching movie credits for ID [{}]. Status: {}. Response Body: {}",
                    id, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Network Error (ResourceAccessException) when fetching movie credits for ID [{}] from TMDB: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body("{\"error_code\":\"TMDB_CONNECTION_ERROR\", \"message\":\"Network error or timeout connecting to TMDB service for movie credits.\"}");
        } catch (Exception e) {
            log.error("Unexpected generic error in getMovieCredits proxy for ID [{}]: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error_code\":\"INTERNAL_PROXY_ERROR\", \"message\":\"An internal error occurred in the backend proxy while fetching movie credits: " + e.getMessage() + "\"}");
        }
    }
}