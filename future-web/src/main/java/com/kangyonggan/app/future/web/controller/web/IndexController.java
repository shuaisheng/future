package com.kangyonggan.app.future.web.controller.web;

import com.github.pagehelper.PageInfo;
import com.kangyonggan.app.future.biz.service.ArticleService;
import com.kangyonggan.app.future.biz.service.CategoryService;
import com.kangyonggan.app.future.common.util.MarkdownUtil;
import com.kangyonggan.app.future.model.constants.CategoryType;
import com.kangyonggan.app.future.model.vo.Article;
import com.kangyonggan.app.future.model.vo.Category;
import com.kangyonggan.app.future.web.controller.BaseController;
import com.kangyonggan.extra.core.annotation.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author kangyonggan
 * @since 2016/12/22
 */
@Controller
@RequestMapping("/")
public class IndexController extends BaseController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 网站模板
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public String layout() {
        return "web/web-layout";
    }

    /**
     * 网站首页
     *
     * @param pageNum
     * @param model
     * @return
     */
    @RequestMapping(value = "index", method = RequestMethod.GET)
    @Monitor(type = "article", description = "访问网站首页统计")
    public String index(@RequestParam(value = "p", required = false, defaultValue = "1") int pageNum, Model model) {
        List<Article> articles = articleService.searchArticles(pageNum, null, null);
        PageInfo<Article> page = new PageInfo(articles);

        model.addAttribute("page", page);
        model.addAttribute("type", "index");
        return getPathIndex();
    }

    /**
     * 搜索
     *
     * @param key
     * @param pageNum
     * @param model
     * @return
     */
    @RequestMapping(value = "search", method = RequestMethod.GET)
    @Monitor(type = "article", description = "搜索统计")
    public String search(@RequestParam(value = "key") String key,
                         @RequestParam(value = "p", required = false, defaultValue = "1") int pageNum,
                         Model model) {
        if ("工具".equals(key)) {
            return "web/tools/index";
        }

        List<Article> articles = articleService.searchArticles(pageNum, null, key);
        PageInfo<Article> page = new PageInfo(articles);

        model.addAttribute("page", page);
        model.addAttribute("type", "search");
        return getPathIndex();
    }

    /**
     * 按栏目查看文章列表
     *
     * @param code
     * @param pageNum
     * @param model
     * @return
     */
    @RequestMapping(value = "category/{code:[\\w]+}", method = RequestMethod.GET)
    @Monitor(type = "article", description = "按栏目查看文章统计")
    public String category(@PathVariable("code") String code, @RequestParam(value = "p", required = false, defaultValue = "1") int pageNum,
                           Model model) {
        List<Article> articles = articleService.searchArticles(pageNum, code, null);
        PageInfo<Article> page = new PageInfo(articles);
        Category category = categoryService.findCategoryByTypeAndCode(CategoryType.ARTICLE.getType(), code);

        model.addAttribute("page", page);
        model.addAttribute("category", category);
        model.addAttribute("type", "category/" + code);
        return getPathIndex();
    }

    /**
     * 文章详情
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "article/{id:[\\d]+}", method = RequestMethod.GET)
    @Monitor(type = "article", description = "文章详情统计")
    public String detail(@PathVariable("id") Long id, Model model) {
        Article article = articleService.findActiveArticleById(id);

        article.setContent(MarkdownUtil.markdownToHtml(article.getContent()));

        model.addAttribute("article", article);
        return getPathRoot() + "/detail";
    }



    /**
     * 上一章
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "article/{id:[\\d]+}/prev", method = RequestMethod.GET)
    @Monitor(type = "article", description = "上一章统计")
    public String prevArticle(@PathVariable("id") Long id, Model model) {
        Article article = articleService.findPrevArticle(id);
        if (article == null) {
            model.addAttribute("message", "这已经是第一篇文章了");
            model.addAttribute("id", id);
            return getPathRoot() + "/no-article";
        }

        article.setContent(MarkdownUtil.markdownToHtml(article.getContent()));

        model.addAttribute("article", article);
        return getPathRoot() + "/detail";
    }

    /**
     * 下一章
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "article/{id:[\\d]+}/next", method = RequestMethod.GET)
    @Monitor(type = "article", description = "下一章统计")
    public String nextArticle(@PathVariable("id") Long id, Model model) {
        Article article = articleService.findNextArticle(id);
        if (article == null) {
            model.addAttribute("message", "这已经是最后一篇文章了");
            model.addAttribute("id", id);
            return getPathRoot() + "/no-article";
        }

        article.setContent(MarkdownUtil.markdownToHtml(article.getContent()));

        model.addAttribute("article", article);
        return getPathRoot() + "/detail";
    }

}
