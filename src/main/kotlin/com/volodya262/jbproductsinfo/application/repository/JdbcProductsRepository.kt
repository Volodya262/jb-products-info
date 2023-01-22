package com.volodya262.jbproductsinfo.application.repository

import com.volodya262.jbproductsinfo.domain.CurrentDateTimeProvider
import com.volodya262.jbproductsinfo.domain.LocalProduct
import com.volodya262.jbproductsinfo.domain.Product
import com.volodya262.jbproductsinfo.domain.ProductCode
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.util.TimeZone

@Component
class JdbcProductsRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
    private val currentDateTimeProvider: CurrentDateTimeProvider
) {
    fun updateLocalProducts(products: List<Product>) {
        upsertProducts(products)
        val updateDateTime = currentDateTimeProvider.getOffsetDateTime()
        insertHistoryCheck(products.map { it.productCode to updateDateTime })
    }

    private fun upsertProducts(products: List<Product>) {
        val productQuery =
            """
                INSERT INTO product (code, name)
                VALUES (:code, :name)
                ON CONFLICT DO NOTHING 
            """.trimIndent()

        val productParameters = products.map {
            MapSqlParameterSource()
                .addValue("code", it.productCode)
                .addValue("name", it.productName)
        }.toTypedArray()

        val alternativeCodesQuery =
            """
                INSERT INTO product_alternative_code (alternative_code, primary_code)
                VALUES (:alternative_code, :primary_code)
                ON CONFLICT DO NOTHING
            """.trimIndent()

        val alternativeCodesParameters = products.flatMap { product ->
            product.alternativeCodes.map { alternativeCode ->
                MapSqlParameterSource()
                    .addValue("primary_code", product.productCode)
                    .addValue("alternative_code", alternativeCode)
            }
        }.toTypedArray()

        transactionTemplate.execute {
            jdbcTemplate.batchUpdate(productQuery, productParameters)
            jdbcTemplate.batchUpdate(alternativeCodesQuery, alternativeCodesParameters)
        }
    }

    private fun insertHistoryCheck(productUpdates: List<Pair<ProductCode, OffsetDateTime>>) {
        val query =
            """
                INSERT INTO product_update_history
                VALUES (:product_code, :check_date)
            """.trimIndent()

        val params = productUpdates.map {
            MapSqlParameterSource()
                .addValue("product_code", it.first)
                .addValue("check_date", it.second)
        }

        jdbcTemplate.batchUpdate(query, params.toTypedArray())
    }

    fun getProducts(): List<LocalProduct> {
        val productsQuery =
            """
                WITH product_last_update AS
                         (SELECT product_code, MAX(check_date) as last_update
                          FROM product_update_history
                          GROUP BY product_code)
                SELECT code as product_code, name as product_name, last_update
                FROM product p JOIN product_last_update puh ON p.code = puh.product_code;
            """.trimIndent()

        val alternativeCodesQuery =
            """
                SELECT alternative_code, primary_code AS product_code
                FROM product_alternative_code
            """.trimIndent()

        val tempProducts = jdbcTemplate.query(productsQuery, MapSqlParameterSource()) { rs, _ ->
            ProductTemp(
                productCode = rs.getString("product_code")!!,
                productName = rs.getString("product_name")!!,
                lastUpdate = rs.getTimestamp("last_update")!!.toOffsetDateTime()
            )
        }

        val alternativeCodes = jdbcTemplate.query(alternativeCodesQuery, MapSqlParameterSource()) { rs, _ ->
            Pair(rs.getString("product_code")!!, rs.getString("alternative_code")!!)
        }
            .groupBy { it.first }
            .entries
            .associate { (key, value) ->
                key to value.map { it.second }.toSet()
            }

        return tempProducts.map {
            LocalProduct(
                productCode = it.productCode,
                productName = it.productName,
                lastUpdate = it.lastUpdate,
                alternativeCodes = alternativeCodes[it.productCode] ?: emptySet()
            )
        }
    }

    fun deleteAll() {
        jdbcTemplate.update("DELETE FROM product_update_history", MapSqlParameterSource())
        jdbcTemplate.update("DELETE FROM product_alternative_code", MapSqlParameterSource())
        jdbcTemplate.update("DELETE FROM product", MapSqlParameterSource())
    }
}

fun Timestamp.toOffsetDateTime(): OffsetDateTime =
    this.toInstant().atZone(TimeZone.getDefault().toZoneId())!!.toOffsetDateTime()

data class ProductTemp(
    val productCode: ProductCode,
    val productName: String,
//    val alternativeCodes: Set<String>,
    val lastUpdate: OffsetDateTime
)
