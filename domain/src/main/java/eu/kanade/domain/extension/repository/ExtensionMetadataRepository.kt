package eu.kanade.domain.extension.repository

import eu.kanade.domain.extension.model.ExtensionMetadata

interface ExtensionMetadataRepository {
    suspend fun getAllMetadata(): List<ExtensionMetadata>
    suspend fun getMetadataByPkgName(pkgName: String): ExtensionMetadata?
    suspend fun insertMetadata(metadata: ExtensionMetadata)
    suspend fun deleteMetadata(pkgName: String)
    suspend fun deleteAllMetadata()
}
