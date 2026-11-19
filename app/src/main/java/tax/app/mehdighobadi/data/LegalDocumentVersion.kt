package tax.app.mehdighobadi.data

/**
 * Immutable metadata for one legally reviewed publication of a document.
 * Dates use ISO-8601 date strings from the API.
 */
data class LegalDocumentVersion(
    val documentId: String,
    val versionId: String,
    val versionNumber: String,
    val publicationDate: String? = null,
    val effectiveDate: String? = null,
    val expirationDate: String? = null,
    val amendmentDate: String? = null,
    val checksum: String? = null,
    val status: LegalVersionStatus = LegalVersionStatus.PUBLISHED
)

enum class LegalVersionStatus {
    DRAFT, REVIEW, APPROVED, PUBLISHED, ARCHIVED;

    companion object {
        fun fromApi(value: String?): LegalVersionStatus =
            value?.trim()?.uppercase()?.let { runCatching { valueOf(it) }.getOrNull() } ?: PUBLISHED
    }
}

/** Records the legal change without mutating the historical version's text. */
data class LegalAmendment(
    val id: String,
    val documentId: String,
    val articleId: String,
    val amendmentType: String,
    val previousText: String? = null,
    val newText: String? = null,
    val publicationDate: String? = null,
    val effectiveDate: String? = null,
    val sourceReference: String? = null
)
