package eu.kanade.domain.extension.model

data class ExtensionMetadata(
    val pkgName: String,
    val name: String,
    val versionName: String,
    val versionCode: Long,
    val libVersion: Double,
    val signatureHash: String,
    val isNsfw: Boolean,
    val isShared: Boolean,
    val repoName: String?,
    val pkgFactory: String?,
    val lastModified: Long,
    val sources: List<ExtensionSource>,
)

data class ExtensionSource(
    val pkgName: String,
    val sourceId: Long,
    val name: String,
    val lang: String,
    val className: String,
)
