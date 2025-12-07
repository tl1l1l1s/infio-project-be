package ktb.week4.community.like;

import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.entity.ArticleTestBuilder;
import ktb.week4.community.domain.article.loader.ArticleLoader;
import ktb.week4.community.domain.article.repository.ArticleRepository;
import ktb.week4.community.domain.like.dto.LikeResponseDto;
import ktb.week4.community.domain.like.entity.LikeArticle;
import ktb.week4.community.domain.like.entity.LikeArticleTestBuilder;
import ktb.week4.community.domain.like.repository.LikeRepository;
import ktb.week4.community.domain.like.service.LikeCommandService;
import ktb.week4.community.domain.like.service.LikeQueryService;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;
import ktb.week4.community.domain.user.loader.UserLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {
	
	@Mock
	LikeRepository likeRepository;
	
	@Mock
	ArticleLoader articleLoader;
	
	@Mock
	ArticleRepository articleRepository;
	
	@Mock
	UserLoader userLoader;
	
	@InjectMocks
	LikeQueryService likeQueryService;
	
	@InjectMocks
	LikeCommandService likeCommandService;
	
	Article article;
	User user;
	
	@BeforeEach
	void setUp() {
		user = spy(UserTestBuilder.aUser().build());
		article = spy(ArticleTestBuilder.anArticle()
				.withUser(user)
				.build());
	}
	
	@Test
	@DisplayName("좋아요를 누른 유저가 좋아요 조회 시 isLiked가 true이다.")
	void givenLikedUser_whenGetLikeStatus_thenIsLikedTrue() {
		// given
		article.increaseLikeCount();
		when(articleLoader.getArticleById(1L)).thenReturn(article);
		when(likeRepository.existsByUserIdAndArticleId(1L, 1L)).thenReturn(true);
		
		// when
		LikeResponseDto res = likeQueryService.getLikeStatus(1L, 1L);
		
		// then
		assertThat(res).isNotNull();
		assertThat(res.articleId()).isEqualTo(1L);
		assertThat(res.likeCount()).isEqualTo(article.getLikeCount());
		assertThat(res.isLiked()).isTrue();
	}
	
	@Test
	@DisplayName("좋아요를 누르지 않은 유저가 좋아요 조회 시 isLiked가 false이다.")
	void givenUnlikedUser_whenGetLikeStatus_thenIsLikedFalse() {
		// given
		article.increaseLikeCount();
		when(articleLoader.getArticleById(1L)).thenReturn(article);
		when(likeRepository.existsByUserIdAndArticleId(2L, 1L)).thenReturn(false);
		
		// when
		LikeResponseDto res = likeQueryService.getLikeStatus(1L, 2L);
		
		// then
		assertThat(res.isLiked()).isFalse();
		assertThat(res.likeCount()).isEqualTo(article.getLikeCount());
	}
	
	@Test
	@DisplayName("@WithAnonymousUser인 경우 좋아요 조회 시 isLiked가 false이다.")
	void givenAnonymous_whenGetLikeStatus_thenIsLikedFalse() {
		// given
		when(articleLoader.getArticleById(1L)).thenReturn(article);
		when(likeRepository.existsByUserIdAndArticleId(null, 1L)).thenReturn(false);
		
		// when
		LikeResponseDto res = likeQueryService.getLikeStatus(1L, null);
		
		// then
		assertThat(res.isLiked()).isFalse();
		assertThat(res.likeCount()).isEqualTo(article.getLikeCount());
	}
	
	@Test
	@DisplayName("좋아요 시 LikeResponseDto 반환하고 likeCount가 1 증가한다.")
	void givenUser_whenLikeArticle_thenReturnsResponseAndIncrementsCount() {
		// given
		when(articleLoader.getArticleById(1L)).thenReturn(article);
		when(userLoader.getUserById(1L)).thenReturn(user);
		when(likeRepository.existsByUserIdAndArticleId(1L, 1L)).thenReturn(false);
		when(likeRepository.save(any(LikeArticle.class))).thenReturn(LikeArticleTestBuilder.aLikeArticle()
				.withArticle(article)
				.withUser(user)
				.build());
		when(articleRepository.save(article)).thenReturn(article);
		
		// when
		LikeResponseDto res = likeCommandService.likeArticle(1L, 1L);
		
		// then
		assertThat(res).isNotNull();
		assertThat(res.articleId()).isEqualTo(1L);
		assertThat(res.isLiked()).isTrue();
		assertThat(res.likeCount()).isEqualTo(article.getLikeCount());
		assertThat(article.getLikeCount()).isEqualTo(1);
		verify(likeRepository).save(any(LikeArticle.class));
		verify(articleRepository).save(article);
	}
	
	@Test
	@DisplayName("좋아요 취소 시 likeCount가 1 감소한다.")
	void givenUser_whenUnlikeArticle_thenDecrementsCount() {
		// given
		article.increaseLikeCount();
		LikeArticle like = LikeArticleTestBuilder.aLikeArticle()
				.withArticle(article)
				.withUser(user)
				.build();
		
		when(articleLoader.getArticleById(1L)).thenReturn(article);
		when(likeRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.of(like));
		when(articleRepository.save(article)).thenReturn(article);
		
		// when
		likeCommandService.unlikeArticle(1L, 1L);
		
		// then
		assertThat(article.getLikeCount()).isEqualTo(0);
		verify(likeRepository).delete(like);
		verify(articleRepository).save(article);
	}
}
