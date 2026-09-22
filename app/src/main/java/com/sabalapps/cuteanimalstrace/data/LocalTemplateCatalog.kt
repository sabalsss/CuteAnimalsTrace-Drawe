package com.sabalapps.cuteanimalstrace.data

import android.content.res.AssetManager
import android.util.JsonReader
import android.util.JsonToken
import androidx.annotation.WorkerThread
import java.io.IOException
import java.io.Reader

/** Manifest-backed metadata only. Images are decoded on demand by the UI. */
class LocalTemplateCatalog private constructor(val templates: List<DrawingTemplate>) {
    private val byId = templates.associateBy { it.id }
    val featuredTemplates = templates.filter { it.featured }
    fun findById(id: String?): DrawingTemplate? = byId[id]

    companion object {
        @WorkerThread
        fun load(assets: AssetManager): LocalTemplateCatalog =
            assets.open("templates_manifest.json").bufferedReader().use(::parse)

        fun parse(source: Reader): LocalTemplateCatalog {
            try {
                val templates = mutableListOf<DrawingTemplate>()
                JsonReader(source).use { reader ->
                    reader.beginArray()
                    while (reader.hasNext()) {
                        var id = ""
                        var name = ""
                        var category = ""
                        var difficulty = ""
                        var path = ""
                        var tracePath = ""
                        var previewPath = ""
                        var description = ""
                        var featured: Boolean? = null
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "id" -> id = reader.nextString()
                                "displayName" -> name = reader.nextString()
                                "category" -> category = reader.nextString()
                                "difficulty" -> difficulty = reader.nextString()
                                "imagePath" -> path = reader.nextString()
                                "traceImagePath" -> tracePath = reader.nextString()
                                "previewImagePath" -> previewPath = reader.nextString()
                                "shortDescription" -> description = reader.nextString()
                                "featured" -> featured = reader.nextBoolean()
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                        require(id.matches(Regex("[a-z0-9_]+"))) { "Invalid template ID: $id" }
                        require(name.isNotBlank() && description.isNotBlank()) { "Incomplete template: $id" }
                        // Older manifests used imagePath for the tracing asset.
                        val resolvedTracePath = tracePath.ifBlank { path }
                        require(resolvedTracePath.matches(Regex("templates/[a-z_]+/$id\\.webp"))) {
                            "Invalid trace asset path: $resolvedTracePath"
                        }
                        require(path.isEmpty() || path == resolvedTracePath) { "Conflicting legacy trace path" }
                        require(previewPath.isEmpty() || previewPath.matches(
                            Regex("templates_preview/[a-z_]+/${id}_preview\\.webp"))) {
                            "Invalid preview asset path: $previewPath"
                        }
                        val resolvedCategory = TemplateCategory.entries.firstOrNull {
                            it.name == category || it.label == category
                        } ?: error("Unknown category: $category")
                        val resolvedDifficulty = Difficulty.entries.firstOrNull { it.name == difficulty }
                            ?: error("Unknown difficulty: $difficulty")
                        val directory = when (resolvedCategory) {
                            TemplateCategory.BabyAnimals -> "baby_animals"
                            else -> resolvedCategory.name.lowercase()
                        }
                        require(resolvedTracePath == "templates/$directory/$id.webp") { "Trace category mismatch" }
                        require(previewPath.isEmpty() || previewPath == "templates_preview/$directory/${id}_preview.webp") {
                            "Preview category mismatch"
                        }
                        templates += DrawingTemplate(id, name, resolvedCategory, resolvedTracePath, resolvedDifficulty,
                            requireNotNull(featured) { "Missing featured flag: $id" }, description,
                            previewImagePath = previewPath.ifBlank { resolvedTracePath })
                    }
                    reader.endArray()
                    require(reader.peek() == JsonToken.END_DOCUMENT) { "Trailing manifest content" }
                }
                require(templates.isNotEmpty()) { "Empty template manifest" }
                require(templates.map { it.id }.distinct().size == templates.size) { "Duplicate template IDs" }
                return LocalTemplateCatalog(templates.toList())
            } catch (error: IllegalArgumentException) {
                throw IOException("Invalid template manifest", error)
            } catch (error: IllegalStateException) {
                throw IOException("Invalid template manifest", error)
            }
        }
    }
}
