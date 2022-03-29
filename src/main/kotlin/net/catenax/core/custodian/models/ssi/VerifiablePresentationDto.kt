@file:UseSerializers(AnySerializer::class)

package net.catenax.core.custodian.models.ssi

import com.fasterxml.jackson.annotation.JsonProperty
import io.bkbn.kompendium.annotations.Field
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.catenax.core.custodian.plugins.AnySerializer

@Serializable
data class VerifiablePresentationDto(
    @SerialName("@context") @JsonProperty("@context")
    @Field(description = "List of Contexts", name = "@context")
    val context: List<String>,
    @Field(description = "The ID of Credential as String (URI compatible)", name = "id")
    val id: String? = null,
    @Field(description = "List of Types", name = "type")
    val type: List<String>,
    @Field(description = "The DID of the Holder as String (URI compatible)", name = "holder")
    val holder: String? = null,
    @Field(description = "List of Verifiable Credentials", name = "verifiableCredential")
    val verifiableCredential: List<VerifiableCredentialDto>? = null,
    @Field(description = "The Proof generated by the holder", name = "proof")
    val proof: LdProofDto? = null
)

@Serializable
data class VerifiablePresentationRequestDto(
    @Field(description = "The DID or BPN of the Holder", name = "holderIdentifier")
    val holderIdentifier: String,
    @Field(description = "List of Verifiable Credentials", name = "verifiableCredentials")
    val verifiableCredentials: List<VerifiableCredentialDto>
)
