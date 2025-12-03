package ktb.week4.community.domain.comment.entity;

import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.entity.ArticleTestBuilder;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;

public class CommentTestBuilder {

    private String content = "테스트용 댓글";
    private User user;
    private Article article;

    private CommentTestBuilder() {
    }

    public static CommentTestBuilder aComment() {
        return new CommentTestBuilder();
    }

    public CommentTestBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public CommentTestBuilder withUser(User user) {
        this.user = user;
        return this;
    }

    public CommentTestBuilder withArticle(Article article) {
        this.article = article;
        return this;
    }

    public Comment build() {
        User author = user != null ? user : UserTestBuilder.aUser().build();
        Article targetArticle = article != null ? article : ArticleTestBuilder.anArticle().withUser(author).build();
        return new Comment(content, author, targetArticle);
    }
}
