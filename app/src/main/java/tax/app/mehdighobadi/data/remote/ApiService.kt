package tax.app.mehdighobadi.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Laravel API v1 contract.
 *
 * DTOs are intentionally transport-only. Mapping to the app's local domain
 * models will happen in repositories, keeping UI code independent of JSON.
 */
interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<AuthPayload>>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiEnvelope<Unit>>

    @GET("api/v1/legal-documents/active")
    suspend fun getActiveLegalDocument(): Response<ApiEnvelope<LegalDocumentDto>>

    @GET("api/v1/articles")
    suspend fun getArticles(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("updated_since") updatedSince: String? = null
    ): Response<ApiEnvelope<PagedPayload<ArticleDto>>>

    @GET("api/v1/articles/{id}")
    suspend fun getArticle(@Path("id") id: String): Response<ApiEnvelope<ArticleDto>>

    @GET("api/v1/infographics")
    suspend fun getInfographics(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiEnvelope<PagedPayload<InfographicDto>>>

    @GET("api/v1/exams")
    suspend fun getExams(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiEnvelope<PagedPayload<ExamDto>>>

    @GET("api/v1/exams/{id}")
    suspend fun getExam(@Path("id") id: String): Response<ApiEnvelope<ExamDto>>

    @POST("api/v1/exams/{id}/submit")
    suspend fun submitExam(
        @Path("id") id: String,
        @Body request: ExamSubmissionRequest
    ): Response<ApiEnvelope<ExamResultDto>>

    @GET("api/v1/conversations")
    suspend fun getConversations(): Response<ApiEnvelope<List<ConversationDto>>>

    @POST("api/v1/conversations")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<ApiEnvelope<ConversationDto>>

    @GET("api/v1/conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") conversationId: String,
        @Query("after_id") afterId: String? = null
    ): Response<ApiEnvelope<PagedPayload<MessageDto>>>

    @POST("api/v1/conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") conversationId: String,
        @Body request: SendMessageRequest
    ): Response<ApiEnvelope<MessageDto>>
}

data class ApiEnvelope<T>(
    val data: T? = null,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)

data class PagedPayload<T>(
    val items: List<T> = emptyList(),
    val currentPage: Int? = null,
    val lastPage: Int? = null,
    val perPage: Int? = null,
    val total: Int? = null
)

data class LoginRequest(val email: String, val password: String)
data class AuthPayload(
    val token: String,
    val tokenType: String = "Bearer",
    val user: UserDto? = null
)

data class UserDto(
    val id: String,
    val name: String? = null,
    val email: String? = null
)

data class ArticleDto(
    val id: String,
    val title: String,
    val slug: String? = null,
    val summary: String? = null,
    val content: String,
    val categoryId: String? = null,
    val updatedAt: String? = null
)

data class InfographicDto(
    val id: String,
    val title: String,
    val imageUrl: String,
    val description: String? = null
)

data class ExamDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val questionCount: Int? = null,
    val durationMinutes: Int? = null,
    val questions: List<ExamQuestionDto> = emptyList()
)

data class ExamQuestionDto(
    val id: String,
    val text: String,
    val options: List<ExamOptionDto> = emptyList()
)

data class ExamOptionDto(
    val id: String,
    val text: String
)

/** Map question IDs to selected option IDs. Never include answer keys. */
data class ExamSubmissionRequest(val answers: Map<String, String>)
data class ExamResultDto(
    val id: String? = null,
    val score: Double? = null,
    val correctCount: Int? = null,
    val totalQuestions: Int? = null,
    val message: String? = null
)

data class ConversationDto(
    val id: String,
    val subject: String? = null,
    val status: String? = null,
    val updatedAt: String? = null
)

data class CreateConversationRequest(val subject: String, val message: String)
data class SendMessageRequest(val body: String)
data class MessageDto(
    val id: String,
    val body: String,
    val senderType: String? = null,
    val createdAt: String? = null
)


/** Hierarchical, versioned legal document. chapter.clientKey must retain local keys (e.g. fasl1) to preserve study state. */
data class LegalDocumentDto(
    val id: String,
    val title: String,
    val version: String? = null,
    val publicationDate: String? = null,
    val effectiveAt: String? = null,
    val expirationDate: String? = null,
    val amendmentDate: String? = null,
    val status: String? = null,
    val checksum: String? = null,
    val publishedAt: String? = null,
    val chapters: List<LegalChapterDto> = emptyList()
)
data class LegalChapterDto(
    val id: String,
    val clientKey: String,
    val title: String,
    val sortOrder: Int = 0,
    val articles: List<LegalArticleDto> = emptyList()
)
data class LegalArticleDto(
    val id: String,
    val number: String,
    val content: String,
    val sortOrder: Int = 0,
    val sections: List<LegalSectionDto> = emptyList(),
    val notes: List<LegalNoteDto> = emptyList()
)
data class LegalSectionDto(val id: String, val title: String, val content: String, val sortOrder: Int = 0)
data class LegalNoteDto(val id: String, val label: String, val content: String, val sortOrder: Int = 0)
