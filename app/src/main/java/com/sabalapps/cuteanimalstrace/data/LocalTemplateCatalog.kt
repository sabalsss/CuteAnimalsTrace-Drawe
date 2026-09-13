package com.sabalapps.cuteanimalstrace.data

import com.sabalapps.cuteanimalstrace.R

/** Small offline catalog compiled into the APK. Add records here and artwork in res/drawable. */
object LocalTemplateCatalog {
    val templates: List<DrawingTemplate> = listOf(
        DrawingTemplate(
            id = "bunny", name = "Sleepy bunny", category = TemplateCategory.Bunnies,
            imageRes = R.drawable.template_bunny, difficulty = Difficulty.Easy, featured = true,
            description = "A gentle bunny with tall ears and a tiny nose. Start with the ears, then follow the round cheeks.",
        ),
        DrawingTemplate(
            id = "kitten", name = "Curious kitten", category = TemplateCategory.Cats,
            imageRes = R.drawable.template_cat, difficulty = Difficulty.Easy, featured = true,
            description = "A curious cat with pointed ears and little whiskers. Practice the simple face outline before adding the details.",
        ),
        DrawingTemplate(
            id = "puppy", name = "Happy puppy", category = TemplateCategory.Dogs,
            imageRes = R.drawable.template_dog, difficulty = Difficulty.Easy, featured = false,
            description = "A cheerful puppy with floppy ears. Use broad, rounded lines for the ears and a small curve for the smile.",
        ),
        DrawingTemplate(
            id = "panda", name = "Little panda", category = TemplateCategory.Pandas,
            imageRes = R.drawable.template_panda, difficulty = Difficulty.Medium, featured = true,
            description = "A round panda with soft ears and eye patches. Take your time with the shapes around the eyes.",
        ),
        DrawingTemplate(
            id = "fox", name = "Friendly fox", category = TemplateCategory.Foxes,
            imageRes = R.drawable.template_fox, difficulty = Difficulty.Medium, featured = true,
            description = "A friendly fox with pointed ears and sweeping cheeks. Follow the long curves toward its little nose.",
        ),
        DrawingTemplate(
            id = "bear", name = "Little bear", category = TemplateCategory.Bears,
            imageRes = R.drawable.template_bear, difficulty = Difficulty.Easy, featured = false,
            description = "A sweet bear with round ears and a button nose. A few simple curves bring this friendly face to life.",
        ),
        DrawingTemplate(
            id = "kawaii_cat", name = "Kawaii cat", category = TemplateCategory.Kawaii,
            imageRes = R.drawable.template_kawaii, difficulty = Difficulty.Detailed, featured = false,
            description = "A bright-eyed cat with a tiny heart. Trace the face first, then add the whiskers and heart with care.",
        ),
        DrawingTemplate(
            id = "baby_bunny", name = "Baby bunny", category = TemplateCategory.BabyAnimals,
            imageRes = R.drawable.template_bunny, difficulty = Difficulty.Easy, featured = false,
            description = "A small bunny face for a gentle first drawing. Keep the cheeks round and the ear lines light.",
        ),
        DrawingTemplate(
            id = "tabby", name = "Whiskered tabby", category = TemplateCategory.Cats,
            imageRes = R.drawable.template_tabby, difficulty = Difficulty.Detailed, featured = false,
            description = "A striped cat with playful whiskers. Add the forehead stripes after tracing the ears and face.",
        ),
        DrawingTemplate(
            id = "spotty_pup", name = "Spotty pup", category = TemplateCategory.Dogs,
            imageRes = R.drawable.template_spotty_dog, difficulty = Difficulty.Medium, featured = false,
            description = "A floppy-eared puppy with a patch around one eye. Draw the outer shape before adding the patch.",
        ),
        DrawingTemplate(
            id = "bunny_heart", name = "Bunny love", category = TemplateCategory.Bunnies,
            imageRes = R.drawable.template_bunny_heart, difficulty = Difficulty.Medium, featured = false,
            description = "A bunny with a little heart beside its cheek. Practice the long ears and the two rounded sides of the heart.",
        ),
        DrawingTemplate(
            id = "baby_bear", name = "Baby bear", category = TemplateCategory.BabyAnimals,
            imageRes = R.drawable.template_baby_bear, difficulty = Difficulty.Easy, featured = false,
            description = "A round little bear with a tiny tuft of fur. Follow the soft curves and finish with a small smile.",
        ),
    )

    val featuredTemplates: List<DrawingTemplate> = templates.filter { it.featured }

    fun findById(id: String?): DrawingTemplate? = templates.find { it.id == id }
}
