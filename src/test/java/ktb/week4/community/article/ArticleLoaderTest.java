package ktb.week4.community.article;

import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.entity.ArticleTestBuilder;
import ktb.week4.community.domain.article.loader.ArticleLoader;
import ktb.week4.community.domain.article.repository.ArticleRepository;
import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArticleLoaderTest {
	
	@Mock
	ArticleRepository articleRepository;
	
	@InjectMocks
	ArticleLoader articleLoader;
	
	@Nested
	class GetArticleById {
		
		@Test
		@DisplayName(("존재하지 않는 글을 조회하려 하면 ARTICLE_NOT_FOUND를 받는다."))
		void givenNotExistingArticle_whenValidateArticleExists_thenThrowsArticleNotFoundException() {
			// given
			when(articleRepository.findById(999L)).thenReturn(Optional.empty());
			
			// when
			GeneralException exc = assertThrows(
					GeneralException.class,
					() -> articleLoader.getArticleById(999L)
			);
			
			// then
			assertEquals(ErrorCode.ARTICLE_NOT_FOUND, exc.getErrorCode());
		}
		
		@Test
		@DisplayName("삭제된 글을 조회하려 하면 ARTICLE_NOT_FOUND를 받는다.")
		void givenDeletedArticle_whenValidateArticleExists_thenThrowsArticleNotFoundException() {
			// given
			Article deletedArticle = ArticleTestBuilder.anArticle().build();
			deletedArticle.deleteArticle();
			when(articleRepository.findById(1L)).thenReturn(Optional.of(deletedArticle));
			
			// when
			GeneralException exc = assertThrows(
					GeneralException.class,
					() -> articleLoader.getArticleById(1L)
			);
			
			// then
			assertEquals(ErrorCode.ARTICLE_NOT_FOUND, exc.getErrorCode());
		}
		
		@Test
		@DisplayName("정상적인 글 ID 조회 시 Article를 반환한다.")
		void givenExistingArticle_whenGetArticleById_thenReturnsArticle() {
			// given
			Article article = ArticleTestBuilder.anArticle().build();
			when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
			
			// when
			Article result = articleLoader.getArticleById(1L);
			
			// then
			assertSame(article, result);
		}
	}
}
