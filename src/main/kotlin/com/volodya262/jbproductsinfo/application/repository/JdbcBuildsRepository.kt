package com.volodya262.jbproductsinfo.application.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.BuildInProcessEvent
import com.volodya262.jbproductsinfo.domain.CurrentDateTimeProvider
import com.volodya262.jbproductsinfo.domain.ProductCode
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class JdbcBuildsRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
    private val objectMapper: ObjectMapper,
    private val currentDateTimeProvider: CurrentDateTimeProvider
) {
    fun getBuilds(): List<BuildInProcess> {
        val query =
            """
                SELECT
                    product_code,
                    build_full_number,
                    event_number,
                    data
                FROM
                    build_process_events
            """.trimIndent()

        val allEvents = jdbcTemplate.query(query, MapSqlParameterSource()) { rs, _ ->
            val productCode = rs.getString("product_code")!!
            val buildFullNumber = rs.getString("build_full_number")!!
            val json = rs.getString("data")!!
            val event = objectMapper.readValue<BuildInProcessEvent>(json, BuildInProcessEvent::class.java)
            return@query Triple(productCode, buildFullNumber, event)
        }

        return allEvents
            .groupBy { "${it.first}:${it.second}" }
            .values
            .map { triple ->
                val productCode = triple.first().first
                val buildFullNumber = triple.first().second
                val events = triple.map { it.third }
                BuildInProcess.fromEvents(productCode, buildFullNumber, events, currentDateTimeProvider)
            }
    }

    fun getBuild(productCode: ProductCode, buildFullNumber: String): BuildInProcess {
        val query =
            """
                SELECT
                    product_code,
                    build_full_number,
                    event_number,
                    data
                FROM
                    build_process_events
                WHERE
                    product_code = :product_code AND
                    build_full_number = :build_full_number
            """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("product_code", productCode)
            .addValue("build_full_number", buildFullNumber)

        val events = jdbcTemplate.query(query, params) { rs, _ ->
            val json = rs.getString("data")
            return@query objectMapper.readValue<BuildInProcessEvent>(json, BuildInProcessEvent::class.java)
        }

        return BuildInProcess.fromEvents(productCode, buildFullNumber, events, currentDateTimeProvider)
    }

    fun saveNew(buildsInProcess: List<BuildInProcess>) {
        transactionTemplate.execute {
            jdbcTemplate.batchUpdate(upsertBuildQuery, buildsInProcess.toUpsertBuildsParams())
            jdbcTemplate.batchUpdate(
                insertBuildProcessEventsQuery,
                buildsInProcess.toInsertBuildProcessEventParams(objectMapper)
            )
        }
    }

    fun saveNew(buildInProcess: BuildInProcess) {
        transactionTemplate.execute {
            jdbcTemplate.update(upsertBuildQuery, buildInProcess.toUpsertBuildParams())
            jdbcTemplate.batchUpdate(
                insertBuildProcessEventsQuery,
                buildInProcess.toInsertBuildProcessEventParams(objectMapper).toTypedArray()
            )
        }
    }

    fun save(buildsInProcess: List<BuildInProcess>) {
        jdbcTemplate.batchUpdate(
            insertBuildProcessEventsQuery,
            buildsInProcess.toInsertBuildProcessEventParams(objectMapper)
        )
    }

    fun save(buildInProcess: BuildInProcess) {
        jdbcTemplate.batchUpdate(
            insertBuildProcessEventsQuery,
            buildInProcess.toInsertBuildProcessEventParams(objectMapper).toTypedArray()
        )
    }

    fun deleteAll() {
        jdbcTemplate.update("DELETE FROM build_process_events", MapSqlParameterSource())
        jdbcTemplate.update("DELETE FROM build", MapSqlParameterSource())
    }
}

private val insertBuildProcessEventsQuery =
    """
        INSERT INTO build_process_events (product_code, build_full_number, event_number, data)
        VALUES (:product_code, :build_full_number, :event_number, :data::JSONB)
    """.trimIndent()

private fun BuildInProcess.toInsertBuildProcessEventParams(objectMapper: ObjectMapper) =
    this.eventsToStore.map { event ->
        MapSqlParameterSource()
            .addValue("product_code", this.productCode)
            .addValue("build_full_number", this.buildFullNumber)
            .addValue("event_number", event.eventNumber)
            .addValue("data", objectMapper.writeValueAsString(event))
    }

private fun List<BuildInProcess>.toInsertBuildProcessEventParams(objectMapper: ObjectMapper) =
    this.filter { it.eventsToStore.isNotEmpty() }
        .flatMap { it.toInsertBuildProcessEventParams(objectMapper) }
        .toTypedArray()

private val upsertBuildQuery =
    """
        INSERT INTO build (product_code, build_full_number, release_date)
        VALUES(:product_code, :build_full_number, :release_date)
        ON CONFLICT DO NOTHING
    """.trimIndent()

private fun BuildInProcess.toUpsertBuildParams() =
    MapSqlParameterSource()
        .addValue("product_code", this.productCode)
        .addValue("build_full_number", this.buildFullNumber)
        .addValue("release_date", this.releaseDate)

private fun List<BuildInProcess>.toUpsertBuildsParams() =
    this.map { it.toUpsertBuildParams() }.toTypedArray()
