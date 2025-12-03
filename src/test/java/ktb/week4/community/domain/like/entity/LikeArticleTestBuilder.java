package ktb.week4.community.domain.like.entity;

import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.entity.ArticleTestBuilder;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;

public class LikeArticleTestBuilder {

    private Article article;
    private User user;

    private LikeArticleTestBuilder() {
    }

    public static LikeArticleTestBuilder aLikeArticle() {
        return new LikeArticleTestBuilder();
    }

    public LikeArticleTestBuilder withArticle(Article article) {
        this.article = article;
        return this;
    }

    public LikeArticleTestBuilder withUser(User user) {
        this.user = user;
        return this;
    }

    public LikeArticle build() {
        User liker = user != null ? user : UserTestBuilder.aUser().build();
        Article likedArticle = article != null ? article : ArticleTestBuilder.anArticle().withUser(liker).build();
        return new LikeArticle(likedArticle, liker);
    }
}
