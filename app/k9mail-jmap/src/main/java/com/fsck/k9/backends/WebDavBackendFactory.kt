package com.fsck.k9.backends

import com.fsck.k9.Account
import com.fsck.k9.backend.BackendFactory
import com.fsck.k9.backend.api.Backend
import com.fsck.k9.backend.webdav.WebDavBackend
import com.fsck.k9.backend.webdav.WebDavStoreUriCreator
import com.fsck.k9.backend.webdav.WebDavStoreUriDecoder
import com.fsck.k9.mail.ServerSettings
import com.fsck.k9.mail.ssl.TrustManagerFactory
import com.fsck.k9.mail.store.webdav.DraftsFolderProvider
import com.fsck.k9.mail.store.webdav.WebDavStore
import com.fsck.k9.mail.transport.WebDavTransport
import com.fsck.k9.mailstore.K9BackendStorageFactory

class WebDavBackendFactory(
    private val backendStorageFactory: K9BackendStorageFactory,
    private val trustManagerFactory: TrustManagerFactory
) : BackendFactory {
    override val transportUriPrefix = "webdav"

    override fun createBackend(account: Account): Backend {
        val accountName = account.displayName
        val backendStorage = backendStorageFactory.createBackendStorage(account)
        val serverSettings = WebDavStoreUriDecoder.decode(account.storeUri)
        val draftsFolderProvider = createDraftsFolderProvider(account)
        val webDavStore = WebDavStore(trustManagerFactory, serverSettings, draftsFolderProvider)
        val webDavTransport = WebDavTransport(trustManagerFactory, serverSettings, draftsFolderProvider)
        return WebDavBackend(accountName, backendStorage, webDavStore, webDavTransport)
    }

    private fun createDraftsFolderProvider(account: Account): DraftsFolderProvider {
        return DraftsFolderProvider {
            account.draftsFolder ?: error("No Drafts folder configured")
        }
    }

    override fun decodeStoreUri(storeUri: String): ServerSettings {
        return WebDavStoreUriDecoder.decode(storeUri)
    }

    override fun createStoreUri(serverSettings: ServerSettings): String {
        return WebDavStoreUriCreator.create(serverSettings)
    }

    override fun decodeTransportUri(transportUri: String): ServerSettings {
        return WebDavStoreUriDecoder.decode(transportUri)
    }

    override fun createTransportUri(serverSettings: ServerSettings): String {
        return WebDavStoreUriCreator.create(serverSettings)
    }
}
