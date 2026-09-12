package com.tessera.launcher.data.helper

import com.tessera.launcher.ui.components.NoteTask

enum class SmartGlanceActionType {
    CALENDAR,
    WEATHER,
    NOTES,
    NONE
}

data class SmartGlanceBriefing(
    val primaryText: String,
    val secondaryText: String? = null,
    val iconType: String, // "CALENDAR", "ALERT", "RAIN", "SUN", "CHECK", "INFO"
    val actionType: SmartGlanceActionType,
    val isUrgent: Boolean = false
)

/**
 * Motor de Síntese Contextual ("Now & Next" / Smart Glance).
 * Combina sinais de agenda, meteorologia e tarefas para produzir um briefing diário
 * relevante e conciso de forma 100% on-device.
 */
class SmartGlanceEngine {

    fun synthesizeBriefing(
        calendarEvent: CalendarEventInfo?,
        conflictInfo: CalendarConflictInfo?,
        weatherInfo: WeatherInfo?,
        notesTasks: List<NoteTask>
    ): SmartGlanceBriefing {
        // 1. Prioridade Máxima: Conflito de eventos na agenda
        if (conflictInfo != null) {
            return SmartGlanceBriefing(
                primaryText = "Conflito: ${conflictInfo.eventCount} eventos às ${conflictInfo.timeFormatted}",
                secondaryText = if (weatherInfo != null) "${weatherInfo.displayTemperature} • ${weatherInfo.condition}" else null,
                iconType = "ALERT",
                actionType = SmartGlanceActionType.CALENDAR,
                isUrgent = true
            )
        }

        // 2. Prioridade Alta: Evento iminente ou em andamento
        if (calendarEvent != null) {
            if (calendarEvent.isOngoing) {
                return SmartGlanceBriefing(
                    primaryText = "Agora: ${calendarEvent.title}",
                    secondaryText = if (weatherInfo != null) "${weatherInfo.displayTemperature} • ${weatherInfo.condition}" else null,
                    iconType = "CALENDAR",
                    actionType = SmartGlanceActionType.CALENDAR,
                    isUrgent = true
                )
            }

            val mins = calendarEvent.minutesUntil
            if (mins in 1..45) {
                return SmartGlanceBriefing(
                    primaryText = "${calendarEvent.title} em $mins min",
                    secondaryText = if (weatherInfo != null) "${weatherInfo.displayTemperature} • ${weatherInfo.condition}" else null,
                    iconType = "CALENDAR",
                    actionType = SmartGlanceActionType.CALENDAR,
                    isUrgent = true
                )
            }
        }

        // 3. Alerta Climático: Chuva ou Tempestade
        if (weatherInfo != null) {
            val isRainy = weatherInfo.weatherCode in 51..67 ||
                    weatherInfo.weatherCode in 80..82 ||
                    weatherInfo.weatherCode in 95..99 ||
                    weatherInfo.condition.contains("Chuva", ignoreCase = true) ||
                    weatherInfo.condition.contains("Tempestade", ignoreCase = true)

            if (isRainy) {
                return SmartGlanceBriefing(
                    primaryText = "${weatherInfo.displayTemperature} • Chuva prevista hoje",
                    secondaryText = if (calendarEvent != null) "${calendarEvent.title} às ${calendarEvent.timeFormatted}" else null,
                    iconType = "RAIN",
                    actionType = SmartGlanceActionType.WEATHER,
                    isUrgent = false
                )
            }
        }

        // 4. Lembretes e Tarefas Pendentes
        val pendingCount = notesTasks.count { !it.isDone }
        if (pendingCount > 0 && calendarEvent == null) {
            val weatherPrefix = if (weatherInfo != null) "${weatherInfo.displayTemperature} • " else ""
            return SmartGlanceBriefing(
                primaryText = "$weatherPrefix$pendingCount ${if (pendingCount == 1) "tarefa pendente" else "tarefas pendentes"}",
                secondaryText = notesTasks.firstOrNull { !it.isDone }?.text,
                iconType = "CHECK",
                actionType = SmartGlanceActionType.NOTES,
                isUrgent = false
            )
        }

        // 5. Próximo evento mais tarde hoje
        if (calendarEvent != null) {
            val weatherPrefix = if (weatherInfo != null) "${weatherInfo.displayTemperature} • " else ""
            return SmartGlanceBriefing(
                primaryText = "$weatherPrefix${calendarEvent.title} às ${calendarEvent.timeFormatted}",
                secondaryText = if (pendingCount > 0) "$pendingCount tarefas pendentes" else null,
                iconType = "CALENDAR",
                actionType = SmartGlanceActionType.CALENDAR,
                isUrgent = false
            )
        }

        // 6. Dia livre com clima
        if (weatherInfo != null) {
            return SmartGlanceBriefing(
                primaryText = "${weatherInfo.displayTemperature} • ${weatherInfo.condition} • Agenda livre",
                secondaryText = if (pendingCount > 0) "$pendingCount tarefas pendentes" else null,
                iconType = "SUN",
                actionType = SmartGlanceActionType.WEATHER,
                isUrgent = false
            )
        }

        // 7. Fallback neutro
        return SmartGlanceBriefing(
            primaryText = "Tudo tranquilo hoje",
            secondaryText = null,
            iconType = "INFO",
            actionType = SmartGlanceActionType.NONE,
            isUrgent = false
        )
    }
}
