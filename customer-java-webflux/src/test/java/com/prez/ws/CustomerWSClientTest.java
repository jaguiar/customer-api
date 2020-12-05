package com.prez.ws;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.prez.ws.model.Card;
import com.prez.ws.model.Cards;
import com.prez.ws.model.Cell;
import com.prez.ws.model.Email;
import com.prez.ws.model.File;
import com.prez.ws.model.GetCustomerWSResponse;
import com.prez.ws.model.Misc;
import com.prez.ws.model.NestedValue;
import com.prez.ws.model.PersonalDetails;
import com.prez.ws.model.PersonalInformation;
import com.prez.ws.model.Photos;
import com.prez.ws.model.Record;
import com.prez.ws.model.Service;
import com.prez.ws.model.Services;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

@Tag("integration")
class CustomerWSClientTest {

  private static final WireMockRule wiremockServer = new WireMockRule(options()
      .dynamicPort()
      .usingFilesUnderClasspath("com/devoxx/ws")
  );

  private CustomerWSClient toTest;

  @BeforeAll
  static void beforeAll() {
    wiremockServer.start();
    configureFor("localhost", wiremockServer.port());

    // GET CUSTOMER INFO

    //OK Partial response
    wiremockServer.stubFor(get("/customers/partial-customer")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBodyFile("partialCustomer.json"))
    );

    //OK full response with customer infos and loyalty and passes
    wiremockServer.stubFor(get("/customers/full-customer")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBodyFile("fullCustomer.json"))
    );

    // 301
    wiremockServer.stubFor(get("/customers/301")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(301)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"email\":\"awsome@user.com\",\"firstName\":\"Awsome\",\"lastName\":\"User\"}"))
    );

    // bad request
    wiremockServer.stubFor(get("/customers/bad")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"error\":\"Bad est un album de Mickael Jackson\"}")
        ));

    // 404
    wiremockServer.stubFor(get("/customers/unknown-id")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"))
    );

    // connection reset by peer
    wiremockServer.stubFor(get("/customers/connection-reset-by-peer")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER))
    );


    // RESPONSE FOR POST

    //OK response
    wiremockServer.stubFor(post("/customers/ok/preferences")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .withHeader("Accept-Language", new EqualToPattern("fr"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                "{\"id\":\"T-Rex\",\"seatPreference\":\"NEAR_CORRIDOR\",\"classPreference\":1, \"profileName\":\"JurassicPark\"}"))
    );

    // bad request
    wiremockServer.stubFor(post("/customers/bad/preferences")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .withHeader("Accept-Language", new EqualToPattern("en"))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"error\":\"Bad est un album de Mickael Jackson\"}")
        ));

    // connection reset by peer
    wiremockServer.stubFor(post("/customers/connection-reset-by-peer/preferences")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .withHeader("Accept-Language", new EqualToPattern("en"))
        .willReturn(aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER))
    );

  }

  @AfterAll
  static void afterAll() {
    wiremockServer.shutdown();
  }

  @BeforeEach
  void beforeEach() {

    CustomerWSProperties configuration =
        CustomerWSProperties.builder()
            .url("http://localhost:" + wiremockServer.port() + "/customers")
            .build();

    final WebClient webClient = WebClient.builder()
        .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
        .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .filter(basicAuthentication("user", "pwd"))
        .build();
    toTest = new CustomerWSClient(configuration, webClient);
  }

  @Test
  @DisplayName("getCustomer should return a customer when successful call to customer web service with a full response")
  void getCustomer_shouldReturnCustomer_whenCallToCustomerWebServiceSuccessfulWithFullResponse() {
    // Given && When
    final GetCustomerWSResponse getCustomerWSResponse = toTest.getCustomer("full-customer").block();

    // Then
    assertThat(getCustomerWSResponse).isNotNull();
    assertThat(getCustomerWSResponse).usingRecursiveComparison().ignoringActualNullFields().isEqualTo(
        GetCustomerWSResponse.builder()
            .id("72f028e2-fbb8-48b3-b943-bf4daad961ed")
            .personalDetails(
                PersonalDetails.builder()
                    .cell(Cell.builder().number("0012125550179").build())
                    .email(Email.builder().address("elliotalderson@protonmail.com")._default(true)
                        .confirmed(NestedValue.builder().value("CHECKED").build()).build())
                    .build())
            .personalInformation(PersonalInformation.builder()
                .civility(NestedValue.builder().value("M").build())
                .firstName("Elliot")
                .lastName("Alderson")
                .birthdate(LocalDate.of(1986, 9, 17))
                .alive(true)
                .build())
            .cards(Cards.builder().cards(Arrays.asList(
                Card.builder().number("29090108600311527").type(NestedValue.builder().value("WEIRD_VALUE").build())
                    .ticketless(true).disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("ER28-0652").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("07239107/23/91").type(NestedValue.builder().value("FAMILY").build())
                    .ticketless(true).disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
            .services(Services.builder().list(Arrays.asList(
                Service.builder().name(NestedValue.builder().value("fda").build())
                    .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
                Service.builder().name(NestedValue.builder().value("loyalty").build())
                    .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
                Service.builder().name(NestedValue.builder().value("dematerialization").build())
                    .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
                Service.builder().name(NestedValue.builder().value("photo").build())
                    .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
            )).build())
            .photos(Photos.builder().file(File.builder()
                .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/72f028e2-fbb8-48b3-b943-bf4daad961ed/photos/file")
                .build()).build())
            .misc(Arrays.asList(
                Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                    .records(Collections.singletonList(
                        Record.builder().otherId("ER28-0652").type(NestedValue.builder().value("LOYALTY").build()).map(
                            Maps.newHashMap(ImmutableMap.<String, String>builder()
                                .put("loyalty_status", "B0B0B0")
                                .put("some_key", "some_value")
                                .put("how_are_you_today", "delusional")
                                .put("loyalty_number", "ER28-0652")
                                .put("old_product_code", "FIDELITE")
                                .put("loyalty_status_label", "PLATINIUM")
                                .put("validity_start", "2019-11-10")
                                .put("validity_end", "2020-11-09")
                                .put("status_d", "2019-11-10")
                                .put("disable_status", "000").build())).build()
                    )).build(),
                Misc.builder().type(NestedValue.builder().value("PASS").build()).count(2).hasMore(false)
                    .records(Arrays.asList(
                        Record.builder().otherId("07239107/23/91").type(NestedValue.builder().value("PASS").build()).map(
                            Maps.newHashMap(ImmutableMap.<String, String>builder()
                                .put("pass_validity_end", "2021-12-23")
                                .put("pass_validity_start", "2019-12-23")
                                .put("pass_number", "07239107/23/91")
                                .put("sous_type", "PASS_QUI_S_ACHETE")
                                .put("some_date_key", "2021-12-23")
                                .put("pass_label", "FAMILY PASS")
                                .put("some_reference", "UWVDJW")
                                .put("new_product_code", "FAMILY")
                                .put("some_other_key", "for_no_reason")
                                .put("old_pass_label", "Pass Famille")
                                .put("pass_is_active", "000").build())).build(),
                        Record.builder().otherId("29090113600311527").type(NestedValue.builder().value("PASS").build()).map(
                            Maps.newHashMap(ImmutableMap.<String, String>builder()
                                .put("pass_validity_end", "2019-12-23")
                                .put("pass_validity_start", "2018-12-23")
                                .put("pass_number", "29090113600311527")
                                .put("sous_type", "PASS_QUI_S_ACHETE")
                                .put("some_date_key", "2020-12-23")
                                .put("pass_label", "FAMILY PASS")
                                .put("some_reference", "ZZWWEE")
                                .put("new_product_code", "FAMILY")
                                .put("some_other_key", "for_no_reason")
                                .put("old_pass_label", "Pass Famille")
                                .put("pass_is_active", "000").build())).build()
                    )).build()
            ))
            .build());
  }

  @Test
  @DisplayName("getCustomer should return a customer when successful call to customer web service with a partial response")
  void getCustomer_shouldReturnCustomer_whenCallToCustomerWebServiceSuccessfulWthPartialResponse() {
    // Given && When
    final GetCustomerWSResponse getCustomerWSResponse = toTest.getCustomer("partial-customer").block();

    // Then
    assertThat(getCustomerWSResponse).isNotNull();
    assertThat(getCustomerWSResponse).usingRecursiveComparison().ignoringActualNullFields().isEqualTo(
        GetCustomerWSResponse.builder()
            .id("222748af-ba4b-4a58-91ce-817ab8454d33")
            .personalDetails(
                PersonalDetails.builder()
                    .email(Email.builder().address("root@themachine")._default(true)
                        .confirmed(NestedValue.builder().value("CHECKED").build()).build())
                    .build())
            .personalInformation(PersonalInformation.builder()
                .firstName("Samantha")
                .lastName("Groves")
                .build())
            .cards(Cards.builder().cards(
                Collections.singletonList(
                    Card.builder().number("001.548.25.MPPS").type(NestedValue.builder().value("LOYALTY").build())
                        .ticketless(false).disableStatus(NestedValue.builder().value("000").build()).build()
                )).build())
            .services(Services.builder().list(Arrays.asList(
                Service.builder().name(NestedValue.builder().value("objectID").build())
                    .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
                Service.builder().name(NestedValue.builder().value("loyalty").build())
                    .status(NestedValue.builder().value("_019875").build()).updatedTime("2019-11-10T00:00:00Z").build()
            )).build())
            .photos(Photos.builder().file(File.builder()
                .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/222748af-ba4b-4a58-91ce-817ab8454d33/photos/file")
                .build()).build())
            .misc(Collections.singletonList(
                Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                    .records(Collections.singletonList(
                        Record.builder().otherId("001.548.25.MPPS").type(NestedValue.builder().value("LOYALTY").build()).map(
                            Maps.newHashMap(ImmutableMap.<String, String>builder()
                                .put("loyalty_status", "_019875")
                                .put("some_key", "some_value")
                                .put("how_are_you_today", "cold")
                                .put("loyalty_number", "001.548.25.MPPS")
                                .put("old_product_code", "FIDELITE")
                                .put("loyalty_status_label", "EMERAUDE")
                                .put("validity_start", "2012-03-05")
                                .put("validity_end", "2013-03-05")
                                .put("status_d", "2019-11-10")
                                .put("disable_status", "000").build())).build()
                    )).build()
            ))
            .build());
  }

  @Test
  @DisplayName("getCustomer should raise a web service exception when response is 3xx")
  void getCustomer_shouldRaiseWebServiceException_whenCustomerWSResponseIs3xx() {
    // Given && When
    Throwable thrown = catchThrowable(() -> toTest.getCustomer("301").block());

    // Then
    assertThat(thrown)
        .isNotNull()
        .isInstanceOf(WebServiceException.class);
    WebServiceException ex = (WebServiceException) thrown;
    assertThat(ex.getHttpStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY);
    assertThat(ex.getError().getErrorDescription()).isEqualTo(
        "Unexpected response from the server while retrieving customer for customerId=301, response={\"email\":\"awsome@user.com\",\"firstName\":\"Awsome\",\"lastName\":\"User\"}");
  }

  @Test
  @DisplayName("getCustomer should raise a web service exception when response is 400")
  void getCustomer_shouldRaiseWebServiceException_whenCustomerWSResponseIs400() {
    // Given && When
    Throwable thrown = catchThrowable(() -> toTest.getCustomer("bad").block());

    // Then
    assertThat(thrown)
        .isNotNull()
        .isInstanceOf(WebServiceException.class);
    WebServiceException ex = (WebServiceException) thrown;
    assertThat(ex.getHttpStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ex.getError().getErrorDescription()).isEqualTo(
        "Unexpected response from the server while retrieving customer for customerId=bad, response={\"error\":\"Bad est un album de Mickael Jackson\"}");
  }

  @Test
  @DisplayName("getCustomer should return an empty customer when response is 404")
  void getCustomer_shouldRaiseWebServiceException_whenCustomerWSResponseIs404() {
    // Given && When
    final GetCustomerWSResponse getCustomerWSResponse = toTest.getCustomer("unknown-id").block();

    // Then
    assertThat(getCustomerWSResponse).isNull();
  }

  @Test
  @DisplayName("getCustomer should raise web service exception when response is not OK at all (CONNECTION_RESET_BY_PEER)")
  void getCustomer_shouldRaiseWebServiceException_whenCustomerWSResponseIsNotOKAtAll() {
    // Given && When
    Throwable thrown = catchThrowable(() -> toTest.getCustomer("connection-reset-by-peer").block());

    // Then
    assertThat(thrown)
        .isNotNull()
        .isInstanceOf(Exception.class);
    Exception ex = (Exception) thrown;
    //because depending on your OS, the message is not always the same...
    assertThat(ex.getMessage()).contains("Connection reset");
  }
}