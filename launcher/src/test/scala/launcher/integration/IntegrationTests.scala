package launcher.integration

import application.dto.{BalanceDTO, CreateAccountDTO, CreateMovementDTO, TransferMovementDTO}
import domain.model.MovementDescription.{CREDIT_TRANSFER_DESCRIPTION, DEBIT_TRANSFER_DESCRIPTION}
import domain.model.{Account, Movement, MovementType}
import launcher.FinancialApplication
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

import java.math.{BigDecimal, RoundingMode}
import java.util
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.IntStream

object IntegrationTests {
  val accountId1 = new AtomicReference[String]
  val accountId2 = new AtomicReference[String]
  val expectedAmount: BigDecimal = BigDecimal.TEN.setScale(2, RoundingMode.DOWN)
  val expectedZero: BigDecimal = BigDecimal.ZERO.setScale(2, RoundingMode.DOWN)
}

@SpringBootTest(
  properties = Array(
    "spring.r2dbc.url=r2dbc:h2:mem:///./testdb",
    "spring.liquibase.url=jdbc:h2:mem:./testdb;DB_CLOSE_DELAY=-1"
  )
)
@AutoConfigureWebTestClient
@ContextConfiguration(classes = Array(classOf[FinancialApplication]))
@TestMethodOrder(
  classOf[MethodOrderer.OrderAnnotation]
)
class IntegrationTests {

  @Autowired private val webClient: WebTestClient = null

  @Order(1)
  @Test def createAccountsWithSuccess(): Unit = {
    val accountId1id = webClient.post
      .uri("/api/v1/accounts")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(createAccountDTO))
      .exchange
      .expectStatus
      .isCreated
      .expectBody(classOf[Account])
      .returnResult
      .getResponseBody
      .id
    IntegrationTests.accountId1.set(accountId1id)

    val accountId2Id = webClient.post
      .uri("/api/v1/accounts")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(createAccountDTO))
      .exchange
      .expectStatus
      .isCreated
      .expectBody(classOf[Account])
      .returnResult
      .getResponseBody
      .id
    IntegrationTests.accountId2.set(accountId2Id)
  }

  @Order(2)
  @Test def createDepositsWithSuccess(): Unit = {
    webClient.post
      .uri("/api/v1/movements/" + IntegrationTests.accountId1.get + "/deposit")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(createMovementDTO))
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Movement])

    webClient.post
      .uri("/api/v1/movements/" + IntegrationTests.accountId2.get + "/deposit")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(createMovementDTO))
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Movement])
      .returnResult()
  }

  @Order(3)
  @Test def createWithdrawsWithSuccess(): Unit = {
    webClient.post
      .uri("/api/v1/movements/" + IntegrationTests.accountId2.get + "/withdraw")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(createMovementDTO))
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Movement])

    webClient.post
      .uri("/api/v1/movements/" + IntegrationTests.accountId1.get + "/withdraw")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(createMovementDTO))
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Movement])
      .returnResult()
  }

  @Order(4)
  @Test def retrieveCreditMovements(): Unit = {
    val account1Movements = webClient.get
      .uri("/api/v1/movements/" + IntegrationTests.accountId1.get + "/credit")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Array[Movement]])
      .returnResult
      .getResponseBody

    val account2Movements = webClient.get
      .uri("/api/v1/movements/" + IntegrationTests.accountId2.get + "/credit")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Array[Movement]])
      .returnResult
      .getResponseBody

    Assertions.assertEquals(1, account1Movements.length)
    Assertions.assertEquals(1, account2Movements.length)
    Assertions.assertEquals(
      IntegrationTests.expectedAmount,
      account1Movements(0).amount
    )
    Assertions.assertEquals(
      IntegrationTests.expectedAmount,
      account2Movements(0).amount
    )
  }

  @Order(5)
  @Test def retrieveDebitMovements(): Unit = {
    val account1Movements = webClient.get
      .uri("/api/v1/movements/" + IntegrationTests.accountId1.get + "/debit")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Array[Movement]])
      .returnResult
      .getResponseBody

    val account2Movements = webClient.get
      .uri("/api/v1/movements/" + IntegrationTests.accountId2.get + "/debit")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Array[Movement]])
      .returnResult
      .getResponseBody

    Assertions.assertEquals(1, account1Movements.length)
    Assertions.assertEquals(1, account2Movements.length)
    Assertions.assertEquals(
      IntegrationTests.expectedAmount,
      account1Movements(0).amount
    )
    Assertions.assertEquals(
      IntegrationTests.expectedAmount,
      account2Movements(0).amount
    )
  }

  @Order(6)
  @Test def assertAccountsBalances(): Unit = {
    val account1Balance = webClient.get
      .uri("/api/v1/accounts/" + IntegrationTests.accountId1.get + "/balance")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[BalanceDTO])
      .returnResult
      .getResponseBody

    val account2Balance = webClient.get
      .uri("/api/v1/accounts/" + IntegrationTests.accountId2.get + "/balance")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[BalanceDTO])
      .returnResult
      .getResponseBody

    Assertions.assertEquals(
      IntegrationTests.expectedZero,
      account1Balance.balance
    )
    Assertions.assertEquals(
      IntegrationTests.expectedZero,
      account2Balance.balance
    )
  }

  @Order(7)
  @Test def assertTransfersWorkWithSuccess(): Unit = {
    // deposit 10 bucks in each account
    createDepositsWithSuccess()
    val transferMovement = new TransferMovementDTO(
      IntegrationTests.accountId2.get,
      IntegrationTests.expectedAmount
    )

    webClient.post
      .uri("/api/v1/movements/" + IntegrationTests.accountId1.get + "/transfer")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(transferMovement))
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Movement])

    val account1Balance = webClient.get
      .uri("/api/v1/accounts/" + IntegrationTests.accountId1.get + "/balance")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[BalanceDTO])
      .returnResult
      .getResponseBody

    val account2Balance = webClient.get
      .uri("/api/v1/accounts/" + IntegrationTests.accountId2.get + "/balance")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[BalanceDTO])
      .returnResult
      .getResponseBody

    Assertions.assertEquals(
      IntegrationTests.expectedAmount.add(IntegrationTests.expectedAmount),
      account2Balance.balance
    )
    Assertions.assertEquals(
      IntegrationTests.expectedZero,
      account1Balance.balance
    )

    val account2CreditMovements = webClient.get
      .uri("/api/v1/movements/" + IntegrationTests.accountId2.get + "/credit")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Array[Movement]])
      .returnResult
      .getResponseBody

    val account1DebitMovements = webClient.get
      .uri("/api/v1/movements/" + IntegrationTests.accountId1.get + "/debit")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Array[Movement]])
      .returnResult
      .getResponseBody

    val firstAccount2CreditMovement = account2CreditMovements(0)
    val firstAccount1DebitMovement = account1DebitMovements(0)

    Assertions.assertEquals(
      firstAccount1DebitMovement.amount,
      firstAccount2CreditMovement.amount
    )
    Assertions.assertEquals(
      firstAccount1DebitMovement.description,
      String.format(DEBIT_TRANSFER_DESCRIPTION, IntegrationTests.accountId2.get)
    )
    Assertions.assertEquals(
      firstAccount2CreditMovement.description,
      String.format(
        CREDIT_TRANSFER_DESCRIPTION,
        IntegrationTests.accountId1.get
      )
    )
  }

  @Order(8)
  @Test def assertTransfersWorkWithSuccessWithConcurrentParallelProcessing()
      : Unit = {
    IntStream
      .rangeClosed(0, 10)
      .parallel
      .forEach((i: Int) => {
        try assertTransfersWorkWithSuccess()
        catch {
          case ignored: Throwable =>
        }
      })

    val account1Movements = webClient.get
      .uri(
        "/api/v1/movements/" + IntegrationTests.accountId1.get + "?start=2000-10-31&end=3000-10-31&page=0&pageSize=100"
      )
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Array[Movement]])
      .returnResult
      .getResponseBody

    val account1Balance = webClient.get
      .uri("/api/v1/accounts/" + IntegrationTests.accountId1.get + "/balance")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[BalanceDTO])
      .returnResult
      .getResponseBody

    // based on the order of tests and the number of the intStream range transfer processes. The balance of account 1 must be zero.
    val account1BalanceOfMovements = retrieveTotalBalanceFromMovements(
      account1Movements
    )
    Assertions.assertEquals(account1Balance.balance, account1BalanceOfMovements)
    Assertions.assertEquals(
      IntegrationTests.expectedZero,
      account1BalanceOfMovements
    )

    val account2Movements = webClient.get
      .uri(
        "/api/v1/movements/" + IntegrationTests.accountId2.get + "?start=2000-10-31&end=3000-10-31&page=0&pageSize=100"
      )
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[Array[Movement]])
      .returnResult
      .getResponseBody

    val account2Balance = webClient.get
      .uri("/api/v1/accounts/" + IntegrationTests.accountId2.get + "/balance")
      .exchange
      .expectStatus
      .is2xxSuccessful
      .expectBody(classOf[BalanceDTO])
      .returnResult
      .getResponseBody

    val account2BalanceOfMovements = retrieveTotalBalanceFromMovements(
      account2Movements
    )
    Assertions.assertEquals(account2Balance.balance, account2BalanceOfMovements)
    // based on the order of tests and the number of the intStream range transfer processes. The balance of account 2 must be 240.
    val expectedTotal = BigDecimal.valueOf(240).setScale(2, RoundingMode.FLOOR)
    Assertions.assertEquals(expectedTotal, account2BalanceOfMovements)
  }

  private def retrieveTotalBalanceFromMovements(movements: Array[Movement]) =
    util.Arrays
      .stream(movements)
      .map((movement: Movement) => {
        if (movement.`type` == MovementType.CREDIT) movement.amount
        else movement.amount.negate
      })
      .reduce(BigDecimal.ZERO, _ add _)
      .setScale(2, RoundingMode.DOWN)

  private def createMovementDTO = new CreateMovementDTO(
    IntegrationTests.expectedAmount
  )

  private def createAccountDTO =
    new CreateAccountDTO(UUID.randomUUID.toString, BigDecimal.ZERO)
}
