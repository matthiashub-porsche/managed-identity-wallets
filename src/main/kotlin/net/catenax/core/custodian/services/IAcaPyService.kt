package net.catenax.core.custodian.services

import io.ktor.client.*
import net.catenax.core.custodian.models.*
import net.catenax.core.custodian.models.ssi.acapy.*

interface IAcaPyService {

    fun getNetworkIdentifier(): String

    suspend fun getWallets(): WalletList

    suspend fun createSubWallet(subWallet: CreateSubWallet): CreatedSubWalletResult

    suspend fun assignDidToPublic(didIdentifier: String, token: String): Boolean

    suspend fun deleteSubWallet(walletData: WalletExtendedData): Boolean

    suspend fun getTokenByWalletIdAndKey(id: String, key: String): CreateWalletTokenResponse

    suspend fun createLocalDidForWallet(didCreateDto: DidCreate, token: String): DidResult

    suspend fun registerDidOnLedger(didRegistration: DidRegistration): DidRegistrationResult

    suspend fun <T> signJsonLd(signRequest: SignRequest<T>, token: String): String

    suspend fun <T> verifyJsonLd(verifyRequest: VerifyRequest<T>, token: String): VerifyResponse

    suspend fun resolveDidDoc(did: String, token: String): ResolutionResult

    suspend fun updateService(serviceEndPoint: DidEndpointWithType, token: String): Boolean

    companion object {
        fun create(acaPyConfig: AcaPyConfig, client: HttpClient): IAcaPyService {
            return AcaPyService(acaPyConfig, client)
        }
    }
}
