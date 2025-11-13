package ktb.week4.community.domain.article.loader;

import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.repository.ArticleRepository;
import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ArticleLoader {
	private final ArticleRepository articleRepository;
	
	public Article getArticleById(Long articleId) {
		Article article = articleRepository.findById(articleId).orElseThrow(() -> new GeneralException(ErrorCode.ARTICLE_NOT_FOUND));
		
		if(article.getDeletedAt() != null) {
			throw new GeneralException((ErrorCode.ARTICLE_NOT_FOUND));
		}
		return article;
	}
}
