package ktb.week4.community.domain.article.entity;

import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;

import java.util.UUID;

public class ArticleTestBuilder {

    private String title = "Title-" + UUID.randomUUID();
    private String content = "테스트용 본문";
    private String articleImage = "/upload/test.png";
    private User user;

    private ArticleTestBuilder() {
    }

    public static ArticleTestBuilder anArticle() {
        return new ArticleTestBuilder();
    }

    public ArticleTestBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public ArticleTestBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public ArticleTestBuilder withArticleImage(String articleImage) {
        this.articleImage = articleImage;
        return this;
    }

    public ArticleTestBuilder withUser(User user) {
        this.user = user;
        return this;
    }

    public Article build() {
        User author = user != null ? user : UserTestBuilder.aUser().build();
        return new Article(title, content, articleImage, author);
    }
}
