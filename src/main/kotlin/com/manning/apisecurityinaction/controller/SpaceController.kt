package com.manning.apisecurityinaction.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.sql.ResultSet
import javax.servlet.http.HttpServletResponse


@RestController
class SpaceController(
    @Autowired val jdbcTemplate: JdbcTemplate,
    @Autowired val objectMapper: ObjectMapper,
) {

    @GetMapping(
        "/spaces",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Transactional
    fun createSpace(@RequestBody body: String): ResponseEntity<Map<String, Any>> {
        val json = objectMapper.readTree(body)
        val spaceName = json["name"].textValue()
        val owner = json["owner"].textValue()

        val spaceId = jdbcTemplate.query(
            "SELECT NEXT VALUE FOR space_id_seq"
        ) { resultSet: ResultSet, rowIndex: Int ->
            resultSet.getInt(1)
        }.first()
        jdbcTemplate.update(
            "INSERT INTO spaces(space_id, name, owner) VALUES(?, ?, ?);",
            spaceId, spaceName, owner
        )
        val uri = "/spaces/${spaceId}"
        val responseBody = mapOf("name" to spaceName, "uri" to uri)
        val headers = LinkedMultiValueMap<String, String>().apply {add("Location", uri)}
        return ResponseEntity(responseBody, headers, HttpStatus.CREATED)
    }
}