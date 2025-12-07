package ktb.week4.community.article;

import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.entity.ArticleTestBuilder;
import ktb.week4.community.domain.article.policy.ArticlePolicy;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;
import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class ArticlePolicyTest {
	
	@InjectMocks
	ArticlePolicy articlePolicy;
	
	@Nested
	class CheckWrittenBy {
		@Test
		@DisplayName("글 작성자와 다른 사용자는 해당 글을 수정/삭제할 수 없다.")
		void givenNotAuthor_whenModifyArticle_thenThrowForbidden() {
			// given
			User user = spy(UserTestBuilder.aUser()
					.build());
			doReturn(1L).when(user).getId();
			Article article = ArticleTestBuilder.anArticle()
					.withUser(user)
					.build();
			
			// when
			GeneralException exc = assertThrows(
					GeneralException.class,
					() -> articlePolicy.checkWrittenBy(article, 2L)
			);
			
			// then
			assertEquals(ErrorCode.FORBIDDEN_REQUEST, exc.getErrorCode());
		}
		
		@Test
		@DisplayName("글 작성자가 수정/삭제하는 경우 성공한다.")
		void givenAuthor_whenModifyArticle_thenPasses() {
			// given
			User user = spy(UserTestBuilder.aUser().build());
			doReturn(10L).when(user).getId();
			Article article = ArticleTestBuilder.anArticle()
					.withUser(user)
					.build();
			
			// expect
			assertDoesNotThrow(() -> articlePolicy.checkWrittenBy(article, 10L));
		}
	}
}
