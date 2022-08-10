/********************************************************************************
 * Copyright (c) 2021,2022 Contributors to the CatenaX (ng) GitHub Organisation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.managedidentitywallets

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.eclipse.tractusx.managedidentitywallets.models.NotImplementedException
import org.eclipse.tractusx.managedidentitywallets.models.WalletCreateDto
import org.eclipse.tractusx.managedidentitywallets.models.ssi.*
import org.eclipse.tractusx.managedidentitywallets.plugins.*
import org.eclipse.tractusx.managedidentitywallets.routes.appRoutes
import kotlin.test.*

@kotlinx.serialization.ExperimentalSerializationApi
class DidDocTest {

    private val server = TestServer().initServer()

    @BeforeTest
    fun setup() {
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.stop(1000, 10000)
    }

    @Test
    fun testDidDocumentOperations() {
        withTestApplication({
            EnvironmentTestSetup.setupEnvironment(environment)
            configurePersistence()
            configureOpenAPI()
            configureSecurity()
            configureRouting(EnvironmentTestSetup.walletService)
            appRoutes(EnvironmentTestSetup.walletService, EnvironmentTestSetup.bpdService)
            configureSerialization()
            configureStatusPages()
            Services.walletService = EnvironmentTestSetup.walletService
            Services.businessPartnerDataService = EnvironmentTestSetup.bpdService
        }) {
            // programmatically add a wallet
            runBlocking {
                EnvironmentTestSetup.walletService.createWallet(WalletCreateDto(EnvironmentTestSetup.DEFAULT_BPN, "name1")).did
                EnvironmentTestSetup.walletService.createWallet(WalletCreateDto(EnvironmentTestSetup.EXTRA_TEST_BPN, "name_extra"))
            }

            handleRequest(HttpMethod.Get, "/api/didDocuments/${EnvironmentTestSetup.DEFAULT_BPN}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Post, "/api/didDocuments/${EnvironmentTestSetup.DEFAULT_BPN}/services") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """{ "id": "linked_domains", "type": "linked_domains", "serviceEndpoint": "https://myhost:123"}""".trimIndent())
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }

            handleRequest(HttpMethod.Post, "/api/didDocuments/${EnvironmentTestSetup.DEFAULT_BPN}/services") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """{ "id": "did-communication", "type": "did-communication", "serviceEndpoint": "https://myhost:123"}""".trimIndent())
            }.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())
            }

            handleRequest(HttpMethod.Post, "/api/didDocuments/${EnvironmentTestSetup.DEFAULT_BPN}/services") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """{ "id": "profile", "type": "profile", "serviceEndpoint": "https://myhost:123"}""".trimIndent())
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }

            handleRequest(HttpMethod.Post, "/api/didDocuments/${EnvironmentTestSetup.DEFAULT_BPN}/services") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """{ "id": "unknown-test", "type": "unknown-test", "serviceEndpoint": "https://myhost:123"}""".trimIndent())
            }.apply {
                assertEquals(HttpStatusCode.NotImplemented, response.status())
            }

            handleRequest(HttpMethod.Post, "/api/didDocuments/${EnvironmentTestSetup.EXTRA_TEST_BPN}/services") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """{ "id": "linked_domains", "type": "linked_domains", "serviceEndpoint": "https://myhost:123"}""".trimIndent())
            }.apply {
                assertEquals(HttpStatusCode.NotImplemented, response.status())
            }

            handleRequest(HttpMethod.Delete, "/api/didDocuments/${EnvironmentTestSetup.DEFAULT_BPN}/services/linked_domains") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.NotImplemented, response.status())
            }

            handleRequest(HttpMethod.Put, "/api/didDocuments/${EnvironmentTestSetup.DEFAULT_BPN}/services/did-communication") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """{"type": "did-communication","serviceEndpoint": "https://myhost:7712"}""".trimIndent())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            // linked_domains service does not exists
            handleRequest(HttpMethod.Put, "/api/didDocuments/${EnvironmentTestSetup.DEFAULT_BPN}/services/linked_domains") {
                addHeader(HttpHeaders.Authorization, "Bearer ${EnvironmentTestSetup.UPDATE_TOKEN}")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """{"type": "linked_domains","serviceEndpoint": "https://myhost:7712"}""".trimIndent())
            }.apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            val exception = assertFailsWith<NotImplementedException> {
                runBlocking {
                    EnvironmentTestSetup.walletService.updateService(
                        EnvironmentTestSetup.EXTRA_TEST_BPN,
                        "linked_domains",
                        DidServiceUpdateRequestDto(
                            type = "linked_domains",
                            serviceEndpoint = "https://test123.com"
                        )
                    )
                }
            }
            assertTrue { exception.message!!.contains("Update Service Endpoint is not supported for the wallet ${EnvironmentTestSetup.EXTRA_TEST_BPN}") }

            runBlocking {
                EnvironmentTestSetup.walletService.deleteWallet(EnvironmentTestSetup.DEFAULT_BPN)
                EnvironmentTestSetup.walletService.deleteWallet(EnvironmentTestSetup.EXTRA_TEST_BPN)
            }
        }
    }

}