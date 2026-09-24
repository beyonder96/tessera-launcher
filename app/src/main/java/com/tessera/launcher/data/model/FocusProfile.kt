package com.tessera.launcher.data.model

/**
 * Perfis de foco contextual para o Tessera Launcher.
 * Permite alternar entre modos de produtividade, desconexão ou uso padrão,
 * filtrando e organizando a gaveta de aplicativos com foco em bem-estar digital.
 */
enum class FocusProfile(
    val id: String,
    val displayName: String,
    val subtitle: String
) {
    OFF(
        id = "off",
        displayName = "Padrão",
        subtitle = "Todos os aplicativos visíveis normalmente"
    ),
    WORK(
        id = "work",
        displayName = "Trabalho",
        subtitle = "Foco profissional — sem jogos ou redes sociais recreativas"
    ),
    MINDFUL(
        id = "mindful",
        displayName = "Desconexão",
        subtitle = "Apenas utilitários essenciais e ferramentas sem distrações"
    );

    companion object {
        fun fromId(id: String?): FocusProfile {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: OFF
        }
    }
}
