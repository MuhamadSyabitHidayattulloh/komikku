package eu.kanade.tachiyomi.data.extension

import eu.kanade.domain.extension.model.ExtensionMetadata
import eu.kanade.domain.extension.model.ExtensionSource
import eu.kanade.domain.extension.repository.ExtensionMetadataRepository
import tachiyomi.data.DatabaseHandler

class ExtensionMetadataRepositoryImpl(
    private val handler: DatabaseHandler,
) : ExtensionMetadataRepository {

    override suspend fun getAllMetadata(): List<ExtensionMetadata> {
        return handler.awaitList {
            extension_metadataQueries.findAllMetadata()
        }.map { metadata ->
            val sources = handler.awaitList {
                extension_metadataQueries.findSourcesByPkgName(metadata.pkg_name, ::mapExtensionSource)
            }
            mapExtensionMetadata(metadata, sources)
        }
    }

    override suspend fun getMetadataByPkgName(pkgName: String): ExtensionMetadata? {
        val metadata = handler.awaitOneOrNull {
            extension_metadataQueries.findMetadataByPkgName(pkgName)
        } ?: return null

        val sources = handler.awaitList {
            extension_metadataQueries.findSourcesByPkgName(pkgName, ::mapExtensionSource)
        }

        return mapExtensionMetadata(metadata, sources)
    }

    override suspend fun insertMetadata(metadata: ExtensionMetadata) {
        handler.await(inTransaction = true) {
            extension_metadataQueries.insertMetadata(
                metadata.pkgName,
                metadata.name,
                metadata.versionName,
                metadata.versionCode,
                metadata.libVersion,
                metadata.signatureHash,
                metadata.isNsfw,
                metadata.isShared,
                metadata.repoName,
                metadata.pkgFactory,
                metadata.lastModified,
            )

            metadata.sources.forEach { source ->
                extension_metadataQueries.insertSource(
                    source.pkgName,
                    source.sourceId,
                    source.name,
                    source.lang,
                    source.className,
                )
            }
        }
    }

    override suspend fun deleteMetadata(pkgName: String) {
        handler.await {
            extension_metadataQueries.deleteMetadata(pkgName)
        }
    }

    override suspend fun deleteAllMetadata() {
        handler.await {
            extension_metadataQueries.deleteAllMetadata()
        }
    }

    private fun mapExtensionMetadata(
        metadata: tachiyomi.data.Extension_metadata,
        sources: List<ExtensionSource>,
    ): ExtensionMetadata {
        return ExtensionMetadata(
            pkgName = metadata.pkg_name,
            name = metadata.name,
            versionName = metadata.version_name,
            versionCode = metadata.version_code,
            libVersion = metadata.lib_version,
            signatureHash = metadata.signature_hash,
            isNsfw = metadata.is_nsfw,
            isShared = metadata.is_shared,
            repoName = metadata.repo_name,
            pkgFactory = metadata.pkg_factory,
            lastModified = metadata.last_modified,
            sources = sources,
        )
    }

    private fun mapExtensionSource(
        pkg_name: String,
        source_id: Long,
        name: String,
        lang: String,
        class_name: String,
    ): ExtensionSource {
        return ExtensionSource(
            pkgName = pkg_name,
            sourceId = source_id,
            name = name,
            lang = lang,
            className = class_name,
        )
    }
}
