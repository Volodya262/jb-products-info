package com.volodya262.jbproductsinfo.domain

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BuildInProcessTest {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private val currentDateTimeProviderMock: CurrentDateTimeProvider = mockk<CurrentDateTimeProvider>(relaxed = true)

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Test
    fun `it should change status`() {
        // arrange
        val expectedUpdatedAtDate = OffsetDateTime.now()
        every { currentDateTimeProviderMock.getOffsetDateTime() } returns expectedUpdatedAtDate
        val buildInProcess = BuildInProcess("CL", "111.222.333", currentDateTimeProviderMock)

        // act
        buildInProcess.toCreated("yandex.ru", LocalDate.parse("22.01.2023", formatter))

        // assert
        assertEquals("CL", buildInProcess.productCode)
        assertEquals("111.222.333", buildInProcess.buildFullNumber)
        assertEquals("yandex.ru", buildInProcess.downloadUrl)
        assertEquals("22.01.2023", buildInProcess.releaseDate!!.format(formatter))
        assertEquals(expectedUpdatedAtDate, buildInProcess.updatedAt)
        assertThat(buildInProcess.eventsToStore, hasSize(1))
    }

    @Test
    fun `it should save events to eventsToStore in correct order when changing status to created and processing`() {
        // arrange
        val buildInProcess = BuildInProcess("CL", "111.222.333", currentDateTimeProviderMock)

        // act
        buildInProcess.toCreated("yandex.ru", LocalDate.parse("22.01.2023", formatter))
        buildInProcess.toProcessing()

        // assert1
        assertTrue(buildInProcess.eventsToStore[0] is BuildInProcessCreatedEvent, "First event is created")
        assertTrue(buildInProcess.eventsToStore[1] is BuildInProcessProcessingEvent, "Second event is processing")
    }

    @Test
    fun `given aggregate with events it should move new events from eventsToStore to events after saving`() {
        // arrange
        val buildInProcess = BuildInProcess("CL", "111.222.333", currentDateTimeProviderMock)
        buildInProcess.toCreated("yandex.ru", LocalDate.parse("22.01.2023", formatter))
        buildInProcess.toProcessing()
        // act
        buildInProcess.applyEventsSaved()
        // assert
        assertThat(buildInProcess.eventsToStore, empty())
        assertTrue(buildInProcess.existingEvents[0] is BuildInProcessCreatedEvent, "First event is created")
        assertTrue(buildInProcess.existingEvents[1] is BuildInProcessProcessingEvent, "Second event is processing")
    }

    @Test
    fun `given aggregate with already saved events it should create new event with correct number`() {
        // arrange
        val buildInProcess = BuildInProcess("CL", "111.222.333", currentDateTimeProviderMock)
        buildInProcess.toCreated("yandex.ru", LocalDate.parse("22.01.2023", formatter))
        buildInProcess.toProcessing()
        buildInProcess.applyEventsSaved()

        // act
        buildInProcess.toProcessed(targetFileContents = "some content")

        // assert
        assertThat(buildInProcess.eventsToStore, hasSize(1))
        assertThat(buildInProcess.eventsToStore[0].eventNumber, `is`(2))
    }

    @Test
    fun `it should create from existing events`() {
        // arrange
        val event0 = BuildInProcessCreatedEvent(0, OffsetDateTime.now(), "ya.ru", LocalDate.now())
        val event1 = BuildInProcessProcessingEvent(1, OffsetDateTime.now())
        val event2 = BuildInProcessProcessedEvent(2, OffsetDateTime.now(), "important content")
        val list = listOf(event0, event1, event2)

        // act
        val res = BuildInProcess.fromEvents("CL", "111.222.333", list, currentDateTimeProviderMock)

        // assert
        assertThat(res.productCode, `is`("CL"))
        assertThat(res.buildFullNumber, `is`("111.222.333"))
        assertThat(res.status, `is`(BuildInProcessStatus.Processed))
        assertThat(res.targetFileContents, `is`("important content"))
    }

    @Test
    fun `it should place existing events in exitingEvents when creating from existing events`() {
        // arrange
        val event0 = BuildInProcessCreatedEvent(0, OffsetDateTime.now(), "ya.ru", LocalDate.now())
        val event1 = BuildInProcessProcessingEvent(1, OffsetDateTime.now())
        val event2 = BuildInProcessProcessedEvent(2, OffsetDateTime.now(), "important content")
        val list = listOf(event0, event1, event2)

        // act
        val res = BuildInProcess.fromEvents("CL", "111.222.333", list, currentDateTimeProviderMock)

        // assert
        assertThat(res.existingEvents, hasSize(3))
        assertThat(res.eventsToStore, empty())
    }

    @Test
    fun `it should use createdAt field from events to calculate updatedAt when creating from existing events`() {
        // arrange
        val date0 = OffsetDateTime.now().minusDays(90)
        val date1 = date0.plusHours(1)
        val date2 = date0.plusHours(1)

        val event0 = BuildInProcessCreatedEvent(0, date0, "ya.ru", LocalDate.now())
        val event1 = BuildInProcessProcessingEvent(1, date1)
        val event2 = BuildInProcessProcessedEvent(2, date2, "important content")
        val list = listOf(event0, event1, event2)

        // act
        val res = BuildInProcess.fromEvents("CL", "111.222.333", list, currentDateTimeProviderMock)

        // assert
        assertThat(res.updatedAt, `is`(date2))
    }
}
