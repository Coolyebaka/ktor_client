@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.rules.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.huntersdiary.android.feature.rules.domain.HuntingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.localRulesDataStore by preferencesDataStore(name = "local_rules")

class LocalRuleStorage(
    private val context: Context,
    private val json: Json,
) {
    suspend fun getRules(query: String?): List<HuntingRule> {
        return readRules()
            .filter { rule -> query.isNullOrBlank() || rule.matchesQuery(query) }
            .sortedBy { rule -> rule.title }
    }

    suspend fun getRuleById(id: String): HuntingRule? {
        return readRules().firstOrNull { rule -> rule.id == id }
    }

    suspend fun replaceRules(rules: List<HuntingRule>) {
        writeRules(rules)
    }

    suspend fun upsert(rule: HuntingRule) {
        val rules = readRules().filterNot { existing -> existing.id == rule.id } + rule
        writeRules(rules)
    }

    private suspend fun readRules(): List<HuntingRule> {
        return context.localRulesDataStore.data.map { preferences ->
            preferences[RULES_KEY]?.let(::decodeRules).orEmpty()
        }.first()
    }

    private suspend fun writeRules(rules: List<HuntingRule>) {
        context.localRulesDataStore.edit { preferences ->
            preferences[RULES_KEY] = json.encodeToString(rules.map { rule -> rule.toEntity() })
        }
    }

    private fun decodeRules(value: String): List<HuntingRule> {
        return runCatching { json.decodeFromString<List<LocalRuleEntity>>(value) }
            .getOrDefault(emptyList())
            .map { entity -> entity.toDomain() }
    }

    private fun HuntingRule.matchesQuery(query: String): Boolean {
        val normalizedQuery = query.trim()
        return title.contains(normalizedQuery, ignoreCase = true) ||
            target.contains(normalizedQuery, ignoreCase = true) ||
            season.contains(normalizedQuery, ignoreCase = true) ||
            region.contains(normalizedQuery, ignoreCase = true) ||
            text.contains(normalizedQuery, ignoreCase = true)
    }

    private fun HuntingRule.toEntity(): LocalRuleEntity {
        return LocalRuleEntity(
            id = id,
            title = title,
            target = target,
            season = season,
            region = region,
            text = text,
        )
    }

    private fun LocalRuleEntity.toDomain(): HuntingRule {
        return HuntingRule(
            id = id,
            title = title,
            target = target,
            season = season,
            region = region,
            text = text,
        )
    }

    private companion object {
        val RULES_KEY = stringPreferencesKey("rules")
    }
}

@Serializable
private data class LocalRuleEntity(
    val id: String,
    val title: String,
    val target: String,
    val season: String,
    val region: String,
    val text: String,
)
