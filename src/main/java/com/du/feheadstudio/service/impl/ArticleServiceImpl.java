package com.du.feheadstudio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.du.feheadstudio.entity.Article;
import com.du.feheadstudio.entity.ArticleJumpLine;
import com.du.feheadstudio.entity.BriefArticle;
import com.du.feheadstudio.entity.SimpleArticle;
import com.du.feheadstudio.mapper.*;
import com.du.feheadstudio.pojo.ArticleSearchInfo;
import com.du.feheadstudio.pojo.TopArticleInfo;
import com.du.feheadstudio.service.IArticleService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Du425
 * @since 2022-03-04
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    private final ArticleMapper articleMapper;
    private final BriefArticleMapper briefArticleMapper;
    private final SimpleArticleMapper simpleArticleMapper;
    private final ArticleJumpLineMapper articleJumpLineMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public ArticleServiceImpl(ArticleMapper articleMapper, BriefArticleMapper briefArticleMapper,
                              SimpleArticleMapper simpleArticleMapper, ArticleJumpLineMapper articleJumpLineMapper,
                              UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.articleMapper = articleMapper;
        this.briefArticleMapper = briefArticleMapper;
        this.simpleArticleMapper = simpleArticleMapper;
        this.articleJumpLineMapper = articleJumpLineMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 保存文章
     *
     * @param article 文章
     * @return 是否成功
     */
    @Transactional
    @Override
    public Boolean saveArticle(Article article) {
        //设置sort
        article.setSort(userMapper.getArticleMum(article.getUserId()));
        //插入文章
        int insert = articleMapper.insert(article);
        //插入简略信息
        Long publishTime = article.getPublishTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(publishTime);
        BriefArticle briefArticle = new BriefArticle(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                article.getColumnId(),
                article.getTitle()
        );
        briefArticle.setUserId(article.getUserId());
        briefArticle.setArticleId(article.getArticleId());
        int insertBrief = briefArticleMapper.insert(briefArticle);
        //用户文章数加1
        Integer num = userMapper.getArticleMum(article.getUserId());
        userMapper.updateArticleNum(++num,article.getUserId());

        return insert > 0 && insertBrief > 0;
    }
    @Transactional
    @Override
    public Boolean updateArticle(Article article) {
        int update = articleMapper.updateById(article);
        //更新搜索用简略信息
        Long publishTime = article.getPublishTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(publishTime);
        BriefArticle briefArticle = new BriefArticle(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                article.getColumnId(),
                article.getTitle()
        );
        briefArticleMapper.updateById(briefArticle);
        return update > 0;
    }
    @Transactional()
    @Override
    public Boolean topArticle(TopArticleInfo info) {
        if (info.getOldArticleId() != null) {
            articleMapper.updateTop(info.getOldArticleId(), 1);
        }
        articleMapper.updateTop(info.getNewArticleId(), 0);
        return true;
    }
    @Transactional
    @Override
    public Boolean deleteArticle(String articleId) {
        //用户名
        String userId = articleMapper.getUserId(articleId);
        int delete1 = briefArticleMapper.deleteById(articleId);
        int delete2 = articleMapper.deleteById(articleId);
        //用户文章数减1
        Integer num = userMapper.getArticleMum(userId);
        userMapper.updateArticleNum( --num,userId);
        return delete1 > 0 && delete2 > 0;
    }

    @Override
    public List<SimpleArticle> getArticleListByUserId(String userId) {
        QueryWrapper<SimpleArticle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<SimpleArticle> selectList = simpleArticleMapper.selectList(queryWrapper);
        if (selectList != null) {
            selectList.forEach(simpleArticle -> userMapper.getUserNickName(simpleArticle.getUserId()));
        } else {
            selectList = new ArrayList<>(1);
        }
        return selectList;
    }
    @Transactional
    @Override
    public Article getArticleById(String articleId) {
        //用户文章浏览量加一
        String userId = articleMapper.getUserId(articleId);
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd");
        HashOperations<String, Object, Object> forHash = redisTemplate.opsForHash();
        String date = format.format(new Date());

        //存在就加一
        if (forHash.hasKey(userId, date)) {
            Integer integer = (Integer) forHash.get(userId, date);
            forHash.put(userId, date, ++integer);
        } else {
            forHash.put(userId, date, 1);
        }

        //文章浏览量加一
        articleMapper.addOneViewNum(articleId);
        return articleMapper.selectById(articleId);
    }

    @Override
    public List<SimpleArticle> searchArticleList(ArticleSearchInfo info) {
        QueryWrapper<SimpleArticle> queryWrapper = new QueryWrapper<>();
        if (info.getYear() != null) {
            queryWrapper.eq("year", info.getYear());
        }
        if (info.getMonth() != null) {
            queryWrapper.eq("month", info.getMonth());
        }
        if (info.getColumnName() != null) {
            queryWrapper.eq("column_name", info.getColumnName());
        }
        if (info.getTitleAbstruct() != null) {
            queryWrapper.like("title", info.getTitleAbstruct());
        }
        queryWrapper.eq("user_id", info.getUserId());
        List<SimpleArticle> selectList = simpleArticleMapper.selectList(queryWrapper);
        if (selectList != null) {
            selectList.forEach(simpleArticle -> userMapper.getUserNickName(simpleArticle.getUserId()));
        } else {
            selectList = new ArrayList<>(1);
        }

        return selectList;
    }
    @Transactional
    @Override
    public Boolean exchange(int a, int b, String userId) {
        QueryWrapper<ArticleJumpLine> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<ArticleJumpLine> list = articleJumpLineMapper.selectList(queryWrapper);
        ArticleJumpLine simpleArticle = list.get(a);
        list.remove(a);
        list.add(b, simpleArticle);
        AtomicInteger size = new AtomicInteger();
        list.forEach(l -> {
            l.setSort(size.getAndIncrement());
            articleJumpLineMapper.updateById(l);
        });

        return true;
    }
}
