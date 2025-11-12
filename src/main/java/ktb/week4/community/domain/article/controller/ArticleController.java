package ktb.week4.community.domain.article.controller;

import ktb.week4.community.domain.article.dto.ArticleResponseDto;
import ktb.week4.community.domain.article.dto.CreateArticleRequestDto;
import ktb.week4.community.domain.article.dto.GetArticlesResponseDto;
import ktb.week4.community.domain.article.dto.UpdateArticleRequestDto;
import ktb.week4.community.domain.article.service.ArticleCommandService;
import ktb.week4.community.domain.article.service.ArticleQueryService;
import ktb.week4.community.global.apiPayload.ApiResponse;
import ktb.week4.community.global.apiPayload.SuccessCode;
import ktb.week4.community.global.apiSpecification.ArticleApiSpecification;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
@AllArgsConstructor
public class ArticleController implements ArticleApiSpecification {
	private final ArticleCommandService articleCommandService;
	private final ArticleQueryService articleQueryService;
	
	@Override
	@GetMapping
	public ApiResponse<GetArticlesResponseDto> getArticles(
			@RequestParam(name = "page") int page,
			 @RequestParam(name = "size", defaultValue = "7") int size) {
		return ApiResponse.onSuccess(SuccessCode.SUCCESS, articleQueryService.getArticles(page, size));
	}
	
	@Override
	@PostMapping
	public ApiResponse<ArticleResponseDto> createArticle(
			@RequestParam Long userId,
			@RequestBody CreateArticleRequestDto request) {
		return ApiResponse.onCreateSuccess(SuccessCode.CREATE_SUCCESS, articleCommandService.createArticle(userId, request));
	}
	
	@Override
	@PatchMapping("/{articleId}")
	public ApiResponse<ArticleResponseDto> updateArticle(
			@PathVariable Long articleId,
			@RequestParam Long userId,
			@RequestBody UpdateArticleRequestDto request) {
		return ApiResponse.onSuccess(SuccessCode.UPDATE_SUCCESS, articleCommandService.updateArticle(userId, articleId, request));
	}
	
	@Override
	@DeleteMapping("/{articleId}")
	public ResponseEntity<Void> deleteArticle(
			@PathVariable Long articleId,
			@RequestParam Long userId) {
		articleCommandService.deleteArticle(userId, articleId);
		return ApiResponse.onDeleteSuccess();
	}
	
	@Override
	@GetMapping("/{articleId}")
	public ApiResponse<ArticleResponseDto> getArticle(
			@PathVariable Long articleId
	) {
		return ApiResponse.onSuccess(SuccessCode.SUCCESS, articleQueryService.getArticle(articleId));
	}
}
