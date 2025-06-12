package com.orange.moviebackend.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Calendar; // 导入 Calendar
import java.util.Date;
import java.util.Objects;

@Data
public class SysMovieVo implements Serializable {

    private static final long serialVersionUID = 1L; // 添加 serialVersionUID

    // --- 静态初始化块，用于创建默认日期 ---
    private static final Date DEFAULT_START_DATE;
    private static final Date DEFAULT_END_DATE;

    static {
        Calendar calStart = Calendar.getInstance();
        calStart.clear(); // 清除当前时间信息
        calStart.set(1970, Calendar.JANUARY, 1, 0, 0, 0); // 月份从0开始 (JANUARY = 0)
        DEFAULT_START_DATE = calStart.getTime();

        Calendar calEnd = Calendar.getInstance();
        calEnd.clear();
        calEnd.set(2050, Calendar.DECEMBER, 31, 0, 0, 0); // 月份从0开始 (DECEMBER = 11)
        DEFAULT_END_DATE = calEnd.getTime();
    }

    private Integer pageNum = 1;
    private Integer pageSize = 20;

    private String movieName; // 默认值 null
    private Integer movieSort = 1; // 默认值 null
    private Long movieCategoryId; // 默认值 null

    // 设置默认日期值
    private Date startDate = DEFAULT_START_DATE;
    private Date endDate = DEFAULT_END_DATE;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysMovieVo that = (SysMovieVo) o;
        return Objects.equals(pageNum, that.pageNum) && // 应该也包含分页字段
                Objects.equals(pageSize, that.pageSize) &&
                Objects.equals(movieName, that.movieName) &&
                Objects.equals(movieSort, that.movieSort) &&
                Objects.equals(movieCategoryId, that.movieCategoryId) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNum, pageSize, movieName, movieSort, movieCategoryId, startDate, endDate);
    }
}